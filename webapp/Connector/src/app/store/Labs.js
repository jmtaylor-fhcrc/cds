/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.app.store.Labs', {

    extend : 'Ext.data.Store',

    model : 'Connector.app.model.Labs',

    cache : [],

    loadSlice : function(slice) {

        var cells = slice.cells, row;
        var labSet = [], labs;
        for (var c=0; c < cells.length; c++) {
            row = cells[c][0];
            labs = row.positions[row.positions.length-1][0];
            if (row.value > 0) {
                labSet.push(labs.name);
            }
        }
        labs = labSet.join(';');

        if (labSet.length > 0) {
            var queryConfig = {
                schemaName: 'CDS',
                queryName: 'labs',
                success: this.onLoadQuery,
                scope: this
            };

            if (labs.length > 0) {
                queryConfig.filterArray = [ LABKEY.Filter.create('Id', labs, LABKEY.Filter.Types.IN) ]
            }

            LABKEY.Query.selectRows(queryConfig);
        }
        else {
            this.onLoadQuery({rows: []});
        }
    },

    onLoadQuery : function(queryResult) {
        var rows = queryResult.rows;
        for (var r=0; r < rows.length; r++) {
            rows[r].internalId = r;
        }
        this.loadRawData(rows);
    }
});
