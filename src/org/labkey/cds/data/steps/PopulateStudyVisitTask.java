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
import java.util.Map;

public class PopulateStudyVisitTask extends AbstractPopulateTask
{
    @Override
    protected void populate(Logger logger) throws PipelineJobException
    {
        SQLFragment sql;
        BatchValidationException errors = new BatchValidationException();

        // populate cohorts
        QuerySchema sourceSchema;
        TableInfo sourceTable;

        QuerySchema targetSchema;
        TableInfo targetTable;

        logger.info("Starting visit management.");
        long start = System.currentTimeMillis();
        for (Container container : project.getChildren())
        {
            sourceSchema = DefaultSchema.get(user, container).getSchema("study");

            if (sourceSchema == null)
                throw new PipelineJobException("Unable to find study schema for folder " + container.getPath());

            targetSchema = sourceSchema;

            sourceTable = sourceSchema.getTable("ds_cohort");
            targetTable = targetSchema.getTable("cohort");

            QueryUpdateService targetService = targetTable.getUpdateService();

            if (targetService == null)
                throw new PipelineJobException("Unable to find update service for study.cohort in folder " + container.getPath());

            //
            // Delete Cohorts
            //
            sql = new SQLFragment("SELECT rowid FROM ").append(targetTable).append(" WHERE container = ?");
            sql.add(container.getEntityId());

            Map<String, Object>[] rows = new SqlSelector(targetTable.getSchema(), sql).getMapArray();
            if (rows.length > 0)
            {
                try
                {
                    targetService.deleteRows(user, container, Arrays.asList(rows), null, null);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }

            if (errors.hasErrors())
            {
                for (ValidationException error : errors.getRowErrors())
                {
                    logger.error(error.getMessage());
                }
                return;
            }

            //
            // Insert Cohorts
            //
            sql = new SQLFragment("SELECT * FROM ").append(sourceTable).append(" WHERE prot = ?");
            sql.add(container.getName());

            rows = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (rows.length > 0)
            {
                try
                {
                    targetService.insertRows(user, container, Arrays.asList(rows), errors, null, null);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }

            if (errors.hasErrors())
            {
                for (ValidationException error : errors.getRowErrors())
                {
                    logger.error(error.getMessage());
                }
                return;
            }

            //
            // Update Visits
            //
            sourceTable = sourceSchema.getTable("ds_visit");
            targetTable = sourceSchema.getTable("visit");
            targetService = targetTable.getUpdateService();

            if (targetService == null)
                throw new PipelineJobException("Unable to find update service for study.visit in folder " + container.getPath());

            sql = new SQLFragment("SELECT * FROM ").append(sourceTable).append(" WHERE prot = ?");
            sql.add(container.getName());

            rows = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (rows.length > 0)
            {
                try
                {
                    targetService.insertRows(user, container, Arrays.asList(rows), errors, null, null);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        long finish = System.currentTimeMillis();

        logger.info("Visit management took " + DateUtil.formatDuration(finish - start) + ".");
    }
}
