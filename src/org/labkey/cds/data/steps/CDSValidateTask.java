package org.labkey.cds.data.steps;

import org.apache.log4j.Logger;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.pipeline.PipelineJobException;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;

import java.util.Map;

/**
 * Created by Joe Christianson on 7/17/2015.
 */
public class CDSValidateTask extends AbstractPopulateTask
{
    @Override
    protected void populate(Logger logger) throws PipelineJobException
    {
        validateDatasets(logger);
    }

    private void validateDatasets(Logger logger) throws PipelineJobException
    {
        logger.info("Validating Subjects in Datasets");
        QuerySchema sourceSchema = DefaultSchema.get(user, project).getSchema("cds");
        if (sourceSchema == null)
            throw new PipelineJobException("Unable to find cds schema for folder " + project.getPath());
        TableInfo validationTable = sourceSchema.getTable("ds_validateDatasetSubjects");
        SQLFragment sql = new SQLFragment("SELECT * FROM ").append(validationTable, "Validator");
        Map<String, Object>[] rows = new SqlSelector(validationTable.getSchema(), sql).getMapArray();
        if (rows.length > 0)
        {
            StringBuilder error = new StringBuilder("Validation Failed!");
            for(Map<String, Object> row : rows)
            {
                error.append("\n\tSubject ").append(row.get("id")).append(" in study ").append(row.get("study")).append(" was not found in the StudySubject table.");
            }
            logger.error(error);
        }
    }
}