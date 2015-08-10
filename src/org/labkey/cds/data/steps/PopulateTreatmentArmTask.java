package org.labkey.cds.data.steps;

import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerFilterable;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.pipeline.PipelineJobException;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.ValidationException;
import org.labkey.api.util.DateUtil;

import java.util.Arrays;
import java.util.Map;

public class PopulateTreatmentArmTask extends AbstractPopulateTask
{
    @Override
    protected void populate(Logger logger) throws PipelineJobException
    {
        SQLFragment sql;
        BatchValidationException errors = new BatchValidationException();

        QuerySchema cdsSchema;

        TableInfo sourceTable;
        TableInfo targetTable;

        QueryUpdateService targetService;

        logger.info("Starting populate treatment arms.");
        long start = System.currentTimeMillis();
        for (Container container : project.getChildren())
        {
            cdsSchema = DefaultSchema.get(user, container).getSchema("cds");

            if (cdsSchema == null)
                throw new PipelineJobException("Unable to find cds schema for folder " + container.getPath());

            sourceTable = cdsSchema.getTable("ds_treatmentarm");
            targetTable = cdsSchema.getTable("treatmentarm");

            targetService = targetTable.getUpdateService();

            if (targetService == null)
                throw new PipelineJobException("Unable to find update service for cds.treatmentarm in " + container.getPath());

            // Delete Treatment Arms
            try
            {
                targetService.truncateRows(user, container, null, null);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }

            if (errors.hasErrors())
            {
                for (ValidationException error : errors.getRowErrors())
                {
                    logger.error(error.getMessage());
                }
                return;
            }

            // Insert Treatment Arms
            sql = new SQLFragment("SELECT * FROM ").append(sourceTable).append(" WHERE prot = ?");
            sql.add(container.getName());

            Map<String, Object>[] insertRows = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (insertRows.length > 0)
            {
                try
                {
                    targetTable.getUpdateService().insertRows(user, container, Arrays.asList(insertRows), errors, null, null);
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

            // Insert Treatment Arm Subject Mappings
            sourceTable = cdsSchema.getTable("ds_treatmentarmsubject");
            targetTable = cdsSchema.getTable("treatmentarmsubjectmap");

            targetService = targetTable.getUpdateService();

            if (targetService == null)
                throw new PipelineJobException("Unable to find update service for cds.treatmentarmsubjectmap in " + container.getPath());

            ((ContainerFilterable) sourceTable).setContainerFilter(new ContainerFilter.CurrentAndSubfolders(user));

            sql = new SQLFragment("SELECT * FROM ").append(sourceTable).append(" WHERE prot = ?");
            sql.add(container.getName());

            insertRows = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (insertRows.length > 0)
            {
                try
                {
                    targetService.insertRows(user, container, Arrays.asList(insertRows), errors, null, null);
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

            // Insert Visit Tag Alignment
            targetTable = cdsSchema.getTable("visittagalignment");
            targetService = targetTable.getUpdateService();
            if (targetService == null)
                throw new PipelineJobException("Unable to find update service for cds.visittagalignment in " + container.getPath());

            sourceTable = cdsSchema.getTable("ds_visittagalignment");
            ((ContainerFilterable) sourceTable).setContainerFilter(new ContainerFilter.CurrentAndSubfolders(user));
            sql = new SQLFragment("SELECT * FROM ").append(sourceTable);

            insertRows = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (insertRows.length > 0)
            {
                try
                {
                    targetService.insertRows(user, container, Arrays.asList(insertRows), errors, null, null);
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
        }
        long finish = System.currentTimeMillis();

        logger.info("Populate treatment arms took " + DateUtil.formatDuration(finish - start) + ".");

        start = System.currentTimeMillis();
        for (Container container : project.getChildren())
        {
            cdsSchema = DefaultSchema.get(user, container).getSchema("cds");

            if (cdsSchema == null)
                throw new PipelineJobException("Unable to find cds schema for folder " + container.getPath());

            sourceTable = cdsSchema.getTable("ds_subjectproduct");
            targetTable = cdsSchema.getTable("SubjectProductMap");

            targetService = targetTable.getUpdateService();

            if (targetService == null)
                throw new PipelineJobException("Unable to find update service for cds.SubjectProductMap in " + container.getPath());

            // Insert Subject Product Mapping
            sql = new SQLFragment("SELECT * FROM ").append(sourceTable).append(" WHERE prot = ?");
            sql.add(container.getName());

            Map<String, Object>[] insertRows = new SqlSelector(sourceTable.getSchema(), sql).getMapArray();
            if (insertRows.length > 0)
            {
                try
                {
                    targetService.insertRows(user, container, Arrays.asList(insertRows), errors, null, null);
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
        }
        finish = System.currentTimeMillis();

        logger.info("Populate SubjectProductMap took " + DateUtil.formatDuration(finish - start) + ".");
    }
}