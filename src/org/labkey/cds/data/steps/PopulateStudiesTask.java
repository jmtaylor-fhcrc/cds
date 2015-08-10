package org.labkey.cds.data.steps;

import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.FolderType;
import org.labkey.api.module.FolderTypeManager;
import org.labkey.api.pipeline.PipelineJobException;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QueryDefinition;
import org.labkey.api.query.QueryException;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.study.StudyService;
import org.labkey.api.study.TimepointType;
import org.labkey.api.util.DateUtil;
import org.labkey.cds.CDSSchema;
import org.labkey.cds.CDSSimpleTable;
import org.labkey.cds.CDSUserSchema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PopulateStudiesTask extends AbstractPopulateTask
{
    protected void populate(Logger logger) throws PipelineJobException
    {
        // Retrieve the set of studies available in the 'import_' schema
        Set<String> importStudies = readImportStudies(project, user, logger);

        // Clean-up old studies and ensure containers for all importStudies
        cleanContainers(importStudies, project, user, logger);
        ensureContainers(importStudies, project, user, logger);

        // Get the coalesced metadata for the studies (including container)
        Map<String, Map<String, Object>> studies = getStudies(project, user, logger);
        List<Map<String, Object>> rows = new ArrayList<>();
        BatchValidationException errors = new BatchValidationException();

        // Import Study metadata
        for (String studyName : studies.keySet())
        {
            if (studies.containsKey(studyName))
            {
                Container c = ContainerManager.getChild(project, studyName);

                try
                {
                    rows.clear();
                    rows.add(studies.get(studyName));

                    TableInfo studyTable = new CDSSimpleTable(new CDSUserSchema(user, c), CDSSchema.getInstance().getSchema().getTable("Study"));
                    QueryUpdateService qud = studyTable.getUpdateService();

                    if (null != qud)
                    {
                        qud.insertRows(user, c, rows, errors, null, null);

                        if (errors.hasErrors())
                        {
                            for (ValidationException error : errors.getRowErrors())
                            {
                                logger.warn(error.getMessage());
                            }
                        }
                    }
                    else
                        throw new PipelineJobException("Unable to find update service for " + studyTable.getName());
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            else
            {
                logger.error("Unable to find metadata for study: " + studyName);
            }
        }
    }


    private void cleanContainers(Set<String> importStudies, Container project, User user, Logger logger)
    {
        int deleted = 0;
        long start = System.currentTimeMillis();

        // Iterate the children to remove any studies that are no longer imported
        for (Container c : project.getChildren())
        {
            if (!importStudies.contains(c.getName()))
            {
                logger.info("Deleting container for study (" + c.getName() + ") as it is no longer imported");
                ContainerManager.delete(c, user);
                deleted++;
            }
        }

        long finish = System.currentTimeMillis();
        logger.info("Deleted " + deleted + " studies in " + DateUtil.formatDuration(finish - start) + ".");
    }


    private void ensureContainers(Set<String> importStudies, Container project, User user, Logger logger)
    {
        long start = System.currentTimeMillis();
        int created = 0;
        FolderType studyFolderType = FolderTypeManager.get().getFolderType("Study");

        // Iterate the studies and create a container for any that do not have one
        for (String studyName : importStudies)
        {
            Container c = ContainerManager.getChild(project, studyName);

            if (c == null)
            {
                logger.info("Creating container for study (" + studyName + ")");
                c = ContainerManager.createContainer(project, studyName, null, null, Container.TYPE.normal, user);
                c.setFolderType(studyFolderType, user);
                StudyService.get().createStudy(c, user, studyName, TimepointType.VISIT, false);
                created++;
            }
            else
            {
                logger.info("Container already exists for study (" + studyName + ")");
            }
        }

        long finish = System.currentTimeMillis();
        logger.info("Created " + created + " studies in " + DateUtil.formatDuration(finish - start) + ".");
    }


    private Set<String> readImportStudies(Container container, User user, Logger logger) throws PipelineJobException
    {
        Set<String> studies = new HashSet<>();
        QuerySchema schema = DefaultSchema.get(user, container).getSchema("cds");

        if (null == schema)
            throw new PipelineJobException("Unable to find cds schema.");

        TableInfo importStudy = schema.getTable("import_study");

        if (null == importStudy)
            throw new PipelineJobException("Unable to find cds.import_study table.");

        SQLFragment sql = new SQLFragment("SELECT prot FROM ").append(importStudy);
        Map<String, Object>[] importStudies = new SqlSelector(importStudy.getSchema(), sql).getMapArray();

        for (Map<String, Object> study : importStudies)
        {
            studies.add((String) study.get("prot"));
        }

        return studies;
    }


    private Map<String, Map<String, Object>> getStudies(Container project, User user, Logger logger)
    {
        QueryService queryService = QueryService.get();
        QueryDefinition qd = queryService.getQueryDef(user, project, "cds", "ds_study");

        ArrayList<QueryException> qerrors = new ArrayList<>();
        TableInfo tiImportStudy = qd.getTable(qerrors, true);

        if (!qerrors.isEmpty())
        {
            // TODO: Process errors to logger?
            return Collections.emptyMap();
        }
        else if (null == tiImportStudy)
        {
            logger.error("Unable to find source query for studies.");
            return Collections.emptyMap();
        }

        ((ContainerFilterable) tiImportStudy).setContainerFilter(new ContainerFilter.CurrentAndSubfolders(user));

        // Get all the studies
        SQLFragment sql = new SQLFragment("SELECT * FROM ").append(tiImportStudy);
        Map<String, Object>[] importStudies = new SqlSelector(tiImportStudy.getSchema(), sql).getMapArray();

        Map<String, Map<String, Object>> importStudiesMap = new HashMap<>();
        for (Map<String, Object> map : importStudies)
        {
            importStudiesMap.put((String) map.get("study_name"), map);
        }

        return importStudiesMap;
    }
}