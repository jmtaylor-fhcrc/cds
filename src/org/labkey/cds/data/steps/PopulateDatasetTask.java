package org.labkey.cds.data.steps;

import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.pipeline.PipelineJobException;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.util.DateUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PopulateDatasetTask extends AbstractPopulateTask
{
    private static final String SOURCE_SCHEMA = "sourceSchema";
    private static final String SOURCE_QUERY = "sourceQuery";
    private static final String TARGET_DATASET = "targetDataset";


    @Override
    public List<String> getRequiredSettings()
    {
        return Arrays.asList(SOURCE_SCHEMA, SOURCE_QUERY, TARGET_DATASET);
    }


    @Override
    protected void populate(Logger logger) throws PipelineJobException
    {
        DefaultSchema projectSchema = DefaultSchema.get(user, project);
        String datasetName = settings.get(TARGET_DATASET);

        QuerySchema sourceSchema = projectSchema.getSchema(settings.get(SOURCE_SCHEMA));

        if (null == sourceSchema)
        {
            throw new PipelineJobException("You provided an invalid \"" + SOURCE_SCHEMA + "\" parameter.");
        }

        TableInfo sourceTable = sourceSchema.getTable(settings.get(SOURCE_QUERY));

        if (null == sourceTable)
        {
            throw new PipelineJobException("You provided an invalid \"" + SOURCE_QUERY + "\" parameter.");
        }

        TableInfo targetDataset = projectSchema.getSchema("study").getTable(datasetName);

        if (null == targetDataset)
        {
            throw new PipelineJobException("A dataset with the name \"" + datasetName + "\" does not exist.");
        }

        SQLFragment sql;
        BatchValidationException errors = new BatchValidationException();
        long start;
        long finish;

        long fullStart = System.currentTimeMillis();
        int insertCount = 0;

        QuerySchema targetSchema;
        QueryUpdateService targetService;

        // Assumes all child containers are studies
        for (Container container : project.getChildren())
        {
            sql = new SQLFragment("SELECT * FROM ").append(sourceTable).append(" WHERE prot = ?");
            sql.add(container.getName());

            Map<String, Object>[] subjects = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (subjects.length > 0)
            {
                try
                {
                    targetSchema = DefaultSchema.get(user, container).getSchema("study");

                    if (null == targetSchema)
                        throw new PipelineJobException("Unable to find study schema for " + container.getPath());

                    targetDataset = targetSchema.getTable(datasetName);
                    targetService = targetDataset.getUpdateService();

                    if (null == targetService)
                        throw new PipelineJobException("Unable to find update service for " + targetSchema.getName() + "." + targetDataset.getName() + " in " + container.getPath());

                    sql = new SQLFragment("SELECT * FROM ").append(targetDataset).append(" LIMIT 1");
                    Map<String, Object>[] rows = new SqlSelector(targetDataset.getSchema(), sql).getMapArray();
                    if (rows.length > 0)
                    {
                        logger.info("Starting truncate of " + datasetName + " dataset. (" + container.getName() + ")");
                        start = System.currentTimeMillis();
                        targetService.truncateRows(user, container, null, null);
                        finish = System.currentTimeMillis();
                        logger.info("Truncate took " + DateUtil.formatDuration(finish - start) + ".");
                    }

                    logger.info("Inserting " + subjects.length + " rows into \"" + targetDataset.getName() + "\" dataset. (" + container.getName() + ")");
                    start = System.currentTimeMillis();
                    targetDataset.getUpdateService().insertRows(user, container, Arrays.asList(subjects), errors, null, null);
                    finish = System.currentTimeMillis();
                    logger.info("Insert took " + DateUtil.formatDuration(finish - start) + ".");
                    insertCount += subjects.length;

                    if (errors.hasErrors())
                    {
                        for (ValidationException error : errors.getRowErrors())
                        {
                            logger.warn(error.getMessage());
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        long fullFinish = System.currentTimeMillis();

        logger.info("PopulateDatasetTask for Dataset \"" + datasetName + "\" took " + DateUtil.formatDuration(fullFinish - fullStart) + ". Inserted " + insertCount + " rows.");
    }
}