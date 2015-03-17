/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.cds;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.QueryDefinition;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.study.Dataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads summary of facts from a study dataset in the current container into the fact table.
 * Tries to map columns from the dataset into the dimension cols of the fact table using lookups and column name conventions
 * Essentially executes a group by of ParticipantId, Study (source study), Assay, Lab & Antigen
 * If not supplied in a column the Assay will be set to the dataset name. Other columns will be null
 */
public class FactLoader
{
    private Dataset _sourceDataset;
    protected TableInfo _sourceTableInfo;
    protected final Container _container;
    protected User _user;
    protected ColumnMapper[] _colsToMap;
    protected int _rowsInserted = -1;

    public FactLoader(UserSchema studySchema, Dataset sourceDataset, User user, Container c)
    {
        _sourceDataset = sourceDataset;
        _sourceTableInfo = studySchema.getTable(sourceDataset.getName());
        _container = c;
        _user = user;
        CDSUserSchema cdsSchema = new CDSUserSchema(user, c);
        UserSchema coreSchema = QueryService.get().getUserSchema(user, c, "core");

        /*
         * ColumnMappers find the correct columns in the dataset to map into cube columns and can map a const if not found.
         * They also ensure that the values from the dataset have corresponding rows in the appropriate
         * tables in the star schema.
         */
        _colsToMap = new ColumnMapper[] {
            new ColumnMapper("ParticipantId", JdbcType.VARCHAR, null, null, "SubjectID", "ParticipantId"),
            new ColumnMapper("Day", JdbcType.INTEGER, null, null, "Day"),
            new ProtocolDayMapper(),
            //The study column is the same as the container column and available directly in the table
            //For this reason we just use the container to find the lookup into the studyproperties table
            new ColumnMapper("Study", JdbcType.GUID, coreSchema.getTable("Container"), null, "Container", "Folder"),
            new ColumnMapper("Assay", JdbcType.VARCHAR, cdsSchema.getTable("Assays"), _sourceTableInfo.getName(), "Assay"),
            new ColumnMapper("Lab", JdbcType.VARCHAR, cdsSchema.getTable("Labs"), null, "Lab"),
            new ColumnMapper("Antigen", JdbcType.VARCHAR, cdsSchema.getTable("Antigens"), null, "Antigen", "VirusName", "Virus"),
            new ColumnMapper("Container", JdbcType.GUID, null, c.getId())
        };
    }


    public ColumnMapper[] getMappings()
    {
        return _colsToMap;
    }

    public String getGroupBySql()
    {
        List<String> selectCols = new ArrayList<>();
        List<String> groupByCols = new ArrayList<>();

        for (ColumnMapper col : _colsToMap)
        {
            selectCols.add(col.getColumnAliasSql());
            if (null != col.getGroupByName())
                groupByCols.add(col.getGroupByName());
        }

        String sql = "SELECT " + StringUtils.join(selectCols, ", ") + " FROM study.\"" + _sourceTableInfo.getName() + "\" GROUP BY " + StringUtils.join(groupByCols, ", ");

        return sql;
    }


    SQLFragment getGroupBySqlTranslated()
    {
        UserSchema schema = QueryService.get().getUserSchema(_user, _container, "study");
        QuerySettings settings = new TempQuerySettings(getGroupBySql());

        QueryView queryView = new QueryView(schema, settings, null);

        return queryView.getTable().getFromSQL("x");
    }


    public SQLFragment getPopulateSql()
    {
        List<String> selectCols = new ArrayList<>();
        for (ColumnMapper col : _colsToMap)
            selectCols.add(col.getSelectName());

        String selectColsStr = StringUtils.join(selectCols, ", ");

        return new SQLFragment("INSERT INTO cds.facts (" + selectColsStr + ")  \nSELECT " + selectColsStr + " FROM \n").append(getGroupBySqlTranslated());
    }


    public int populateCube()
    {
         for (ColumnMapper columnMapper : _colsToMap)
            columnMapper.ensureKeys();

        SQLFragment sql = getPopulateSql();
        _rowsInserted = new SqlExecutor(CDSSchema.getInstance().getSchema()).execute(sql);

        return _rowsInserted;
    }

    public boolean isLoadComplete()
    {
        return _rowsInserted == -1;
    }

    public int getRowsInserted()
    {
        return _rowsInserted;
    }

    public Dataset getSourceDataset()
    {
        return _sourceDataset;
    }


    public class ColumnMapper
    {
        private JdbcType _type;
        private ColumnInfo _sourceColumn;
        private String _constValue;
        private String _selectName;
        private TableInfo _lookupTarget;
        private int _rowsInserted = -1;

        ColumnMapper(String selectName, JdbcType type, TableInfo lookupTarget, @Nullable String constValue, String... altNames)
        {
            _type = type;
            _sourceColumn = findMappingColumn(lookupTarget, altNames);
            _selectName = selectName;
            _constValue = constValue;
            _lookupTarget = lookupTarget;
        }

        protected String getColumnAliasSql()
        {
            String sql;

            if (null != _sourceColumn)
                sql = _sourceColumn.getName() + " AS " + _selectName;
            else if (null != _constValue)
                sql = _sourceTableInfo.getSqlDialect().getStringHandler().quoteStringLiteral(_constValue) + " AS " + _selectName;
            else
                sql = "CAST(NULL AS " + _sourceTableInfo.getSqlDialect().sqlTypeNameFromJdbcType(_type) + ")" + " AS " + _selectName;

            return sql;
        }

        @Nullable
        private ColumnInfo findLookupColumn(TableInfo target)
        {
            if (target == null)
                return null;

            String targetSchemaName = target.getSchema().getName();
            String targetTableName = target.getName();
            for (ColumnInfo col : _sourceTableInfo.getColumns())
            {
                if (col.isLookup())
                {
                    TableInfo colTarget = col.getFkTableInfo();
                    //Not sure -- does TableInfo.equals work??
                    if (null != colTarget && colTarget.getSchema().getName().equalsIgnoreCase(targetSchemaName) && colTarget.getName().equalsIgnoreCase(targetTableName))
                        return col;
                }
            }

            return null;
        }

        @Nullable
        private ColumnInfo findMappingColumn(TableInfo target, String... altNames)
        {
            ColumnInfo col = findLookupColumn(target);

            if (null != col)
                return col;

            if (null != altNames && altNames.length > 0)
                for (String s : altNames)
                    if (null != _sourceTableInfo.getColumn(s))
                        return _sourceTableInfo.getColumn(s);

            return null;
        }

        protected String getGroupByName()
        {
            return null == _sourceColumn ? null : _sourceColumn.getName();
        }

        public String getSelectName()
        {
            return _selectName;
        }

        public ColumnInfo getSourceColumn()
        {
            return _sourceColumn;
        }

        public String getConstValue()
        {
            return _constValue;
        }

        public SQLFragment getEnsureKeysSql()
        {
            if (null == _lookupTarget)
                return null;

            List<String> pkCols = _lookupTarget.getPkColumnNames();
            SQLFragment sqlFragment = null;
            String pkName;

            if (pkCols.size() == 1)
                pkName = pkCols.get(0);
            else if (_selectName.equalsIgnoreCase("ProtocolDay"))
                pkName = "RowId";
            else if (pkCols.size() == 2 && pkCols.get(0).equalsIgnoreCase("container"))
                pkName = pkCols.get(1);
            else
                throw new IllegalStateException("Expected one pk field in table " + _lookupTarget);

            if (null == _sourceColumn && null != _constValue)
            {
                /*
                INSERT INTO cds.Assays ( container, id)
                        SELECT 'c994c269-4924-102f-afd2-4b5bd87e1a4c', 'neut'  WHERE 'neut' NOT IN (SELECT id from cds.Assays WHERE container = 'c994c269-4924-102f-afd2-4b5bd87e1a4c')
                */

                String sql = "INSERT INTO cds." + _lookupTarget.getName() + " (container, " + pkName + ") SELECT ?, ? WHERE ? NOT IN (SELECT " + pkName + " from cds." + _lookupTarget.getName() + " WHERE container=?)";
                sqlFragment = new SQLFragment(sql, _container, _constValue, _constValue, _container);
            }
            else if (null != _sourceColumn && _lookupTarget.getSchema().getName().equalsIgnoreCase("cds")) //TODO: Fix up missing keys in study schema
            {
                /*
                INSERT INTO cds.Assays ( container, id)
                        SELECT 'c994c269-4924-102f-afd2-4b5bd87e1a4c', Assay FROM (SELECT DISTINCT Assay FROM study.neut WHERE Assay NOT IN (SELECT id FROM cds.Assays)) x
                 */

                //Translate LK sql into db sql then wrap in an insert statement.
                String lkSelect = "SELECT DISTINCT " + _sourceColumn.getName() + " FROM study.\"" + _sourceTableInfo.getName()  + "\" WHERE " + _sourceColumn.getName() + " NOT IN (SELECT " + pkName + " FROM cds." + _lookupTarget.getName() + ")";
                UserSchema schema = QueryService.get().getUserSchema(_user, _container, "study");
                QuerySettings settings = new TempQuerySettings(lkSelect);

                QueryView queryView = new QueryView(schema, settings, null);

                SQLFragment source =  queryView.getTable().getFromSQL("x");

                sqlFragment = new SQLFragment("INSERT INTO cds." + _lookupTarget.getName() + " (container, " + pkName + ") SELECT ? , " + _sourceColumn.getName() + " FROM ", _container);
                sqlFragment.append(source);
            }

            return sqlFragment;
        }

        public int ensureKeys()
        {
            SQLFragment ensureKeysSql = getEnsureKeysSql();
            if (null != ensureKeysSql)
                _rowsInserted = new SqlExecutor(CDSSchema.getInstance().getSchema()).execute(ensureKeysSql);
            else
                _rowsInserted = 0;

            return _rowsInserted;
        }

        public int getRowsInserted()
        {
            return _rowsInserted;
        }
    }

    public class ProtocolDayMapper extends ColumnMapper
    {
        public ProtocolDayMapper()
        {
            super("ProtocolDay", JdbcType.INTEGER, null, null, "ProtocolDay");
        }

        @Override
        protected String getColumnAliasSql()
        {

            return "SubjectVisit.Visit.ProtocolDay AS ProtocolDay";
        }

        @Override
        protected String getGroupByName()
        {
            return "SubjectVisit.Visit.ProtocolDay";
        }
    }

    /**
     * This class may be used ot create a QuerySettings from a given SQL statement,
     * schema name, and container.
     */
    protected class TempQuerySettings extends QuerySettings
    {
        private String _sql;

        public TempQuerySettings(String sql)
        {
            super("query");
            _sql = sql;
            setQueryName("sql");
        }

        @Override
        protected QueryDefinition createQueryDef(UserSchema schema)
        {
            QueryDefinition qdef;
            qdef = QueryService.get().createQueryDef(schema.getUser(), _container, schema, getQueryName());
            qdef.setSql(_sql);
            return qdef;
        }
    }
}
