/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.controller.State', {
    extend: 'LABKEY.app.controller.State',

    defaultTitle: 'CAVD DataSpace',

    subjectName: 'Subject',

    appVersion: '0.5',

    supportColumnServices: true,

    useMergeFilters: true,

    isService: true,

    init : function() {
        this.callParent();

        this.onMDXReady(function(mdx) {
            Connector.model.Filter.loadSubjectContainer(mdx);
        });
    },

    checkReady : function() {
        Connector.getService('Query').onQueryReady(function() {
            this.fireReady();
            this.helpTest();
        }, this);
    },

    // Helper to let the test know that everything should be loaded
    helpTest : function() {
        Ext.getBody().addCls('appready');
    },

    initColumnListeners : function() {

        this.control('variableselector', {
            selectionmade: function(selected) {
                if (!Ext.isArray(selected)) {
                    selected = [selected];
                }

                Ext.each(selected, function (selection) {
                    if (selection.$className == 'Connector.model.Measure') {
                        this.addSessionColumn(selection.raw);
                    }
                    else if (Ext.isObject(selection)) {
                        this.addSessionColumn(selection);
                    }
                }, this);
            }
        });
    },

    getTitle : function(viewname) {
        return 'Connector: ' + viewname;
    },

    requestFilterUndo : function() {
        var index = this.getPreviousState();
        if (index > -1) {
            this.loadFilters(index);
        }
        else {
            console.warn('FAILED TO UNDO. NOT ABLE TO FIND STATE');
        }
    },

    getFilterModelName : function() {
        return 'Connector.model.Filter';
    },

    inverseSelection : function() {
        var selections = this.getSelections(),
            data;

        // Only handle one selection
        if (selections.length > 0) {
            data = selections[0].getData();
        }

        if (data && !Ext.isEmpty(data.gridFilter)) {
            var sqlFilters = [null, null, null, null];
            var oldFilter = data.gridFilter[0];

            // Only support inverse of equal or equals one of right now
            switch (oldFilter.getFilterType()) {
                case LABKEY.Filter.Types.EQUALS_ONE_OF:
                    sqlFilters[0] = LABKEY.Filter.create(oldFilter.getColumnName(), oldFilter.getValue(), LABKEY.Filter.Types.EQUALS_NONE_OF);
                    break;
                case LABKEY.Filter.Types.EQUAL:
                    sqlFilters[0] = LABKEY.Filter.create(oldFilter.getColumnName(), oldFilter.getValue(), LABKEY.Filter.Types.NOT_EQUAL);
                    break;
                default:
                    sqlFilters = data.gridFilter;
            }

            var filter = Ext.create('Connector.model.Filter', {
                gridFilter: sqlFilters,
                plotMeasures: data.plotMeasures,
                hierarchy: data.hierarchy,
                isPlot: data.isPlot,
                isGrid: data.isGrid,
                operator: data.operator,
                filterSource: data.filterSource,
                isWhereFilter: data.isWhereFilter,
                showInverseFilter: data.showInverseFilter
            });

            this.addSelection(filter, true, false, true);
        }
    }
});