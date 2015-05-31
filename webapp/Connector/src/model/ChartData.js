/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.model.ChartData', {

    extend : 'Ext.data.Model',

    fields : [
        {name : 'measures', defaultValue: [null, null, null]}, // Array [x, y, color]
        {name : 'measureToColumn', defaultValue: {}},
        {name : 'columnAliases', defaultValue: []},

        /* from the selectRows call to the temp query generated by the getData call */
        {name : 'schemaName'},
        {name : 'queryName'},
        {name : 'rows', defaultValue: []},

        /* generated properties based on the processing of the above rows */
        {name : 'properties', defaultValue: {}},
        {name : 'percentOverlap', defaultValue: 1},
        {name : 'containerAlignmentDayMap', defaultValue: {}},
        {name : 'visitMap', defaultValue: {}},
        {name : 'xNullMap', defaultValue: {}},
        {name : 'yNullMap', defaultValue: {}},
        {name : 'xDomain', defaultValue: [0,0]},
        {name : 'yDomain', defaultValue: [0,0]},
        {name : 'percentXNulls', defaultValue: 0},
        {name : 'percentYNulls', defaultValue: 0}
    ],

    statics: {
        isContinuousMeasure : function(measure) {
            var type = measure.type;
            return type === 'INTEGER' || type === 'DOUBLE' || type === 'TIMESTAMP' || type === 'FLOAT' || type === 'REAL';
        },

        /**
         * handle scenario where we are plotting either the same variable, with different antigen subsets,
         * from the same source or different variables from the same source and result will be pivoted by
         * the getData API
         * @param xMeasure
         * @param yMeasure
         * @returns {boolean}
         */
        requiresPivot : function(xMeasure, yMeasure) {
            return xMeasure != null && yMeasure != null
                    && Connector.model.ChartData.isContinuousMeasure(xMeasure)
                    && Connector.model.ChartData.isContinuousMeasure(yMeasure)
                    && xMeasure.options && xMeasure.options.antigen
                    && yMeasure.options && yMeasure.options.antigen
                    && xMeasure.schemaName == yMeasure.schemaName
                    && xMeasure.queryName == yMeasure.queryName
                    && xMeasure.variableType == null && yMeasure.variableType == null;
        }
    },

    constructor : function(config) {
        this.callParent([config]);

        this.processSelectRows();
    },

    getMeasure : function(index) {
        return this.get('measures')[index];
    },

    getMeasureToColumnMap : function() {
        return this.get('measureToColumn');
    },

    getColumnAliases : function() {
        return this.get('columnAliases');
    },

    getDataRows : function() {
        return this.get('rows');
    },

    getProperties : function() {
        return this.get('properties');
    },

    getPercentOverlap : function() {
        return this.get('percentOverlap');
    },

    getContainerAlignmentDayMap : function() {
        return this.get('containerAlignmentDayMap');
    },

    getVisitMap : function() {
        return this.get('visitMap');
    },

    getXNullMap : function() {
        return this.get('xNullMap');
    },

    getYNullMap : function() {
        return this.get('yNullMap');
    },

    getXDomain : function() {
        return this.get('xDomain');
    },

    getYDomain : function() {
        return this.get('yDomain');
    },

    getPercentXNulls : function() {
        return this.get('percentXNulls');
    },

    getPercentYNulls : function() {
        return this.get('percentYNulls');
    },

    processSelectRows : function() {
        var x = this.getMeasure(0), y = this.getMeasure(1), color = this.getMeasure(2),
                mTC = this.getMeasureToColumnMap(),
                xa, ya, ca, _xid, _yid, _cid,
                containerColName = "Visit_Folder", containerAlignmentDayMap = {},
                visitColName = mTC['Visit'], visitMap = {};

        var subjectNoun = Connector.studyContext.subjectColumn;
        var subjectCol = mTC[subjectNoun];

        if (color) {
            _cid = mTC[color.alias] || mTC[color.name];
        }

        this.processPivotedData(x, y, subjectCol, _cid);

        if (x) {
            _xid = x.interval || mTC["xAxis"] || mTC[x.alias] || mTC[x.name];
            xa = {
                schema : x.schemaName,
                query  : x.queryName,
                name   : x.name,
                alias  : x.alias,
                colName: _xid, // Stash colName so we can query the getData temp table in the brushend handler.
                label  : x.label,
                type   : x.type,
                isNumeric : x.type === 'INTEGER' || x.type === 'DOUBLE' || x.type === 'FLOAT' || x.type === 'REAL',
                isContinuous: Connector.model.ChartData.isContinuousMeasure(x)
            };
        }
        else {
            xa = {
                schema  : null,
                query   : null,
                name    : null,
                alias   : null,
                colName : null,
                label   : "",
                isNumeric: false
            }
        }

        _yid = mTC["yAxis"] || mTC[y.alias] || mTC[y.name];
        ya = {
            schema : y.schemaName,
            query  : y.queryName,
            name   : y.name,
            alias  : y.alias,
            colName: _yid, // Stash colName so we can query the getData temp table in the brushend handler.
            label  : y.label,
            type   : y.type,
            isNumeric : y.type === 'INTEGER' || y.type === 'DOUBLE' || y.type === 'FLOAT' || y.type === 'REAL',
            isContinuous: Connector.model.ChartData.isContinuousMeasure(y)
        };

        if (color) {
            ca = {
                schema : color.schemaName,
                query  : color.queryName,
                name   : color.name,
                alias  : color.alias,
                colName: _cid, // Stash colName so we can query the getData temp table in the brushend handler.
                label  : color.label,
                type   : color.type
            };
        }
        else {
            ca = {
                schema  : null,
                query   : null,
                name    : null,
                alias   : null,
                colName : null,
                label   : "",
                isNumeric: false
            }
        }

        var map = [], xNullMap = [], yNullMap = [], r,
                rows = this.getDataRows(),
                len = rows.length,
                validCount = 0,
                xDomain = [null,null], yDomain = [null,null];

        var xVal, yVal, colorVal, xIsNum, yIsNum, negX = false, negY = false, xAntigen, yAntigen;
        for (r = 0; r < len; r++) {

            // build study and visit map
            if (containerColName && rows[r][containerColName]) {
                containerAlignmentDayMap[rows[r][containerColName].value] = 0;
            }
            if (visitColName && rows[r][visitColName]) {
                visitMap[rows[r][visitColName].value] = true;
            }

            if (x) {
                xVal = this._getValue(x, _xid, rows[r]);
                xAntigen = rows[r][_xid].antigen;
                if (!xAntigen && x.options.antigen && rows[r][mTC[x.options.antigen.name]]) {
                    xAntigen = rows[r][mTC[x.options.antigen.name]].value;
                }
                if(Ext.typeOf(xVal) === "number") {
                    if(xDomain[0] == null || xVal < xDomain[0])
                        xDomain[0] = xVal;
                    if(xDomain[1] == null || xVal > xDomain[1])
                        xDomain[1] = xVal;
                }
            }
            else {
                xVal = "";
            }

            yVal = this._getValue(y, _yid, rows[r]);
            if(Ext.typeOf(yVal) === "number") {
                if(yDomain[0] == null || yVal < yDomain[0])
                    yDomain[0] = yVal;
                if(yDomain[1] == null || yVal > yDomain[1])
                    yDomain[1] = yVal;
            }
            yAntigen = rows[r][_yid].antigen;
            if (!yAntigen && y.options.antigen && rows[r][mTC[y.options.antigen.name]]) {
                yAntigen = rows[r][mTC[y.options.antigen.name]].value;
            }

            if (color) {
                colorVal = this._getValue(color, _cid, rows[r]);
            }
            else {
                colorVal = null;
            }

            // allow any pair that does not contain a negative value.
            // NaN, null, and undefined are non-negative values.

            // validate x
            if (xa && xa.isNumeric) {
                xIsNum = !(Ext.isNumber(x) && x < 1);
                if (!negX && !xIsNum) {
                    negX = true;
                }
            }

            // validate y
            if (ya.isNumeric) {
                yIsNum = !(Ext.isNumber(y) && y < 1);
                if (!negY && !yIsNum) {
                    negY = true;
                }
            }

            var entry = {
                x: xVal,
                y: yVal,
                color: colorVal,
                subjectId: rows[r][subjectCol],
                xname: (xa ? xa.label : null) + (xAntigen ? " (" + xAntigen + ")" : ''),
                yname: ya.label + (yAntigen ? " (" + yAntigen + ")" : ''),
                colorname: ca.label
            };

            if(xVal == null) {
                xNullMap.push(entry);
            }
            else if(yVal == null) {
                yNullMap.push(entry);
            }
            else if ((xa && xa.isNumeric) || (!xa.isNumeric && xVal !== undefined && xVal !== null)) {
                map.push(entry);
            }

            if ((!x || this.isValidValue(x, xVal)) && this.isValidValue(y, yVal)) {
                validCount ++;
            }
        }

        this.set('visitMap', visitMap);
        this.set('containerAlignmentDayMap', containerAlignmentDayMap);
        this.set('percentOverlap', validCount / len);
        this.set('percentXNulls', Ext.util.Format.round((xNullMap.length / len) * 100, 2));
        this.set('percentYNulls', Ext.util.Format.round((yNullMap.length / len) * 100, 2));

        this.set('xNullMap', xNullMap);
        this.set('yNullMap', yNullMap);
        this.set('xDomain', xDomain);
        this.set('yDomain', yDomain);
        this.set('rows', map);
        this.set('properties', {
            subjectColumn: subjectCol, // We need the subject column as it appears in the temp query for the brushend handler
            xaxis: xa,
            yaxis: ya,
            color: ca,
            setXLinear: negX,
            setYLinear: negY
        });
    },

    /*
     * When we are plotting subsets of antigens from the same source against each other, we pivot the data
     * so we need to unpivot it here for each combination of x-axis antigen by y-axis antigen
     */
    processPivotedData : function(x, y, subjectCol, colorCol) {

        if (Connector.model.ChartData.requiresPivot(x, y)) {
            var mTC = this.getMeasureToColumnMap();
            var xantigen = x.options.antigen;
            var yantigen = y.options.antigen;

            if (xantigen != null && xantigen.values.length > 0 && yantigen != null && yantigen.values.length > 0)
            {
                var xColAlias = x.alias;
                var yColAlias = y.alias;

                // get the mapping of the column aliases in the data object in an easier format to reference
                var columnAliasMap = {};
                Ext.each(this.getColumnAliases(), function(col) {
                    if (col.pivotValue) {
                        columnAliasMap[col.alias + "::" + col.pivotValue] = col.columnName;
                    }
                });

                // create an array of antigen pairs (of the data object column aliases)
                var antigenColumnAliasPairs = [];
                for (i = 0; i < xantigen.values.length; i++)
                {
                    var xAntigenVal = xantigen.values[i];
                    for (var j = 0; j < yantigen.values.length; j++)
                    {
                        var yAntigenVal = yantigen.values[j];
                        antigenColumnAliasPairs.push({
                            xAlias: columnAliasMap[xColAlias + "::" + xAntigenVal],
                            xAntigen: xAntigenVal,
                            yAlias: columnAliasMap[yColAlias + "::" + yAntigenVal],
                            yAntigen: yAntigenVal
                        });
                    }
                }

                // special case for having the same measure on both axis
                if (xColAlias == yColAlias)
                {
                    xColAlias += '-x';
                    yColAlias += '-y';

                    // remap the column names to what we will set them to
                    mTC['xAxis'] = xColAlias;
                    mTC['yAxis'] = yColAlias;
                }
                else
                {
                    // remap the column names to what we will set them to
                    mTC[xColAlias] = xColAlias;
                    mTC[yColAlias] = yColAlias;
                }

                // create the new data.rows array with a row for each ptid/visit/antigenPair
                var newRowsArr = [];
                var rows = this.getDataRows();
                for (var i = 0; i < rows.length; i++)
                {
                    var row = rows[i];

                    Ext.each(antigenColumnAliasPairs, function(pair) {
                        var dataRow = {};
                        dataRow[subjectCol] = row[subjectCol];

                        if (colorCol) {
                            dataRow[colorCol] = row[colorCol];
                        }

                        // issue 20589: skip null-null points produced by pivot
                        // issue 21601: skip null values if x/y has filters
                        if ((!x.hasFilters || row[pair.xAlias].value != null) &&
                            (!y.hasFilters || row[pair.yAlias].value != null) &&
                            (row[pair.xAlias].value != null || row[pair.yAlias].value != null))
                        {
                            dataRow[xColAlias] = row[pair.xAlias];
                            dataRow[xColAlias].antigen = pair.xAntigen;
                            dataRow[yColAlias] = row[pair.yAlias];
                            dataRow[yColAlias].antigen = pair.yAntigen;

                            newRowsArr.push(dataRow);
                        }
                    });
                }

                this.set('rows', newRowsArr);
            }
        }
    },

    _getValue : function(measure, colName, row) {
        var val, type = measure.type;

        if (type === 'INTEGER') {
            val = parseInt(row[colName].value);
            return this.isValidNumber(val) ? val : null;
        }
        else if (type === 'DOUBLE' || type === 'FLOAT' || type === 'REAL') {
            val = parseFloat(row[colName].value);
            return this.isValidNumber(val) ? val : null;
        }
        else if (type === 'TIMESTAMP') {
            val = row[colName].value;
            return val !== undefined && val !== null ? new Date(val) : null;
        }

        // Assume categorical.
        val = row[colName].displayValue ? row[colName].displayValue : row[colName].value;
        return (val !== undefined) ? val : null;
    },

    isValidNumber : function(number) {
        return !(number === undefined || isNaN(number) || number === null);
    },

    isValidValue : function(measure, value) {
        var type = measure.type;
        if (type === 'INTEGER' || type === 'DOUBLE' || type === 'FLOAT' || type === 'REAL') {
            return this.isValidNumber(value);
        }

        return !(value === undefined || value === null);
    }
});