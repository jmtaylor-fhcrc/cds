package org.labkey.cds.data;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.UpdateableTableInfo;
import org.labkey.api.etl.CopyConfig;
import org.labkey.api.etl.DataIteratorBuilder;
import org.labkey.api.etl.DataIteratorContext;
import org.labkey.api.etl.Pump;
import org.labkey.api.etl.ResultSetDataIterator;
import org.labkey.api.etl.StandardETL;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CDSImportCopyConfig extends CopyConfig
{
    QueryUpdateService.InsertOption option = QueryUpdateService.InsertOption.IMPORT;

    CDSImportCopyConfig(String sourceSchema, String source, String targetSchema, String target)
    {
        super(sourceSchema, source, targetSchema, target);
    }

    public DataIteratorBuilder selectFromSource(Container container, User user, DataIteratorContext context,
                                         @Nullable File dir, Logger log) throws SQLException, IOException
    {
        QuerySchema sourceSchema = DefaultSchema.get(user, container, this.getSourceSchema());
        if (null == sourceSchema)
        {
            context.getErrors().addRowError(new ValidationException("Could not find source schema: " + this.getSourceSchema()));
            return null;
        }
        String sql = getSourceQuery().startsWith("SELECT") ? getSourceQuery() : "SELECT * FROM " + getSourceQuery();
        ResultSet rs = QueryService.get().select(sourceSchema, sql);
        return new DataIteratorBuilder.Wrapper(ResultSetDataIterator.wrap(rs, context));
    }

    public int copyFrom(Container c, User u, DataIteratorContext context, DataIteratorBuilder from)
            throws IOException, BatchValidationException
    {
        assert this.getTargetSchema().getParts().size()==1;
        context.setInsertOption(option);
        DbSchema targetSchema = DbSchema.get(this.getTargetSchema().getName());
        TableInfo targetTableInfo = targetSchema.getTable(getTargetQuery());
        return copy(context, from, targetTableInfo, c, u);
    }

    // Like DataIteratorUtil.copy, but with cancel support
    static int copy(final DataIteratorContext context, DataIteratorBuilder from, TableInfo to, Container c, User user)
            throws IOException, BatchValidationException
    {
        StandardETL etl = StandardETL.forInsert(to, from, c, user, context);
        DataIteratorBuilder insert = ((UpdateableTableInfo)to).persistRows(etl, context);
        Pump pump = new Pump(insert, context);
//        pump.setProgress(new ListImportProgress()
//        {
//            @Override
//            public void setTotalRows(int rows)
//            {
//
//            }
//
//            @Override
//            public void setCurrentRow(int currentRow)
//            {
//                if (dl.checkInterrupted())
//                    throw new CancelledException();
//            }
//        });
        pump.run();
        return pump.getRowCount();
    }
}