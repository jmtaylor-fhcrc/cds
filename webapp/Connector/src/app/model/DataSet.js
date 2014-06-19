/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.app.model.DataSet', {

    extend : 'Ext.data.Model',

    // idProperty: 'Label',

    fields: [
        {name: 'Label'},
        {name: 'Name'},
        {name: 'KeyPropertyName'},
        {name: 'CategoryId'}
    ],

    countsByStudy : undefined,

    constructor: function() {
    	this.callParent(arguments);

    	this.countsByStudy = {};
    },

    hasDataFromStudy : function(study) {
    	return this.countsByStudy[study] > 0;
    },

    queryDataFromStudy: function(id, callback) {
		LABKEY.Query.executeSql({
			schemaName: 'study',
			sql: "SELECT COUNT(*) AS n FROM \""+this.get('Name').value+"\" WHERE " + Connector.studyContext.subjectColumn + ".Study.Label = '"+id+"'",
			success: function(data) {
				var count = data.rows[0].n;
				this.countsByStudy[id] = count;
				callback && callback(count > 0);
			},
			scope: this
		});
    },

    // Query variables etc for an assay named assayName. data param can be an existing object to append to, or if omitted the
    // data variable will be created. The callback will receive the updated data object once queries are complete.
    dataForAssayByName : function(assayName, data, callback) {
        data = data || {};
        data.variables = data.variables || {
            key: [],
            other: []
        };

        var id = this.get('Label');

        var store = this.store.dataSetStores[id.value];

        var count = store.count();
        var countForAssay = 0;
        store.each(function(record) {
            if (record.get("Assay") == assayName) {
                ++countForAssay;
            }
        })

        if (countForAssay > 0) {
            LABKEY.Ajax.request({
                url : LABKEY.ActionURL.buildURL("visualization", "getMeasures"),
                method : 'GET',
                params : {
                    allColumns: true,
                    filters: [LABKEY.Query.Visualization.Filter.create({schemaName: "study", queryName: id.value})]
                },
                success: function(response){
                    response = LABKEY.Utils.decode(response.response);
                    Ext.each(response.measures, function(measure) {
                        if (measure.isKeyVariable) {
                            data.variables.key.push(measure);
                        } else {
                            data.variables.other.push(measure);
                        }
                    })
                    callback(data);
                }
            });
        } else {
            callback(data);
        }

        return countForAssay > 0;
    }
});
