/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.controller.Chart', {

    extend : 'Connector.controller.AbstractViewController',

    views  : ['Compare', 'Scatter', 'Time'],

    init : function() {

        this.control('#yaxisselector', {
            requestvariable: function(view, model) {
                var plot = view.up('plot');
                if (plot) {
                    plot.showYMeasureSelection(view.getEl());
                }
            }
        });

        this.control('#xaxisselector', {
            requestvariable: function(view, model) {
                var plot = view.up('plot');
                if (plot) {
                    plot.showXMeasureSelection(view.getEl());
                }
            }
        });

        this.control('#colorselector', {
            requestvariable: function(view, model) {
                var plot = view.up('plot');
                if (plot) {
                    plot.showColorSelection(view.getEl());
                }
            }
        });

        this.control('#plotshowdata', {
            click: function(btn) {
                var plot = btn.up('plot');
                if (plot) {
                    plot.showPlotDataGrid(btn.getEl());
                }
            }
        });

        this.control('plot', {
            axisselect: function(plot, axis, selection) {
                if (axis === 'y') {
                    Ext.getCmp('yaxisselector').getModel().updateVariable(selection);
                }
                else if (axis === 'x') {
                    Ext.getCmp('xaxisselector').getModel().updateVariable(selection);
                }
                else if (axis === 'color') {
                    Ext.getCmp('colorselector').getModel().updateVariable(selection);
                }
            }
        });

        this.control('axisselectdisplay > panel > panel > button#gotoassaypage', {
            click: function(btn) {
                var win = btn.up('window');
                if (win) win.hide();

                // issue 20664: find the assay label from the first dataset row
                if (btn.source && btn.source.assaysLookup) {
                    LABKEY.Query.selectRows({
                        schemaName: 'study',
                        queryName: btn.source.get('queryName'),
                        columns: btn.source.assaysLookup.name + '/Label',
                        maxRows: 1,
                        scope: this,
                        success: function(data) {
                            if (data.rows.length == 1) {
                                this.getViewManager().changeView('learn', 'learn', ['assay', data.rows[0][btn.source.assaysLookup.name + '/Label']]);
                            }
                        }
                    });
                }
            }
        });

        this.callParent();
    },

    createView : function(xtype, config, context) {

        if (xtype == 'plot')
        {
            var state = this.getStateManager();
            var v = Ext.create('Connector.view.Scatter', {
                control: this.getController('Data'),
                visitTagStore : this.getStore('VisitTag'),
                ui  : 'custom',
                state : state
            });

            state.clearSelections();
            state.on('filterchange', v.onFilterChange, v);
            state.on('plotselectionremoved', v.onPlotSelectionRemoved, v);
            state.on('selectionchange', v.onSelectionChange, v);
            this.getViewManager().on('afterchangeview', v.onViewChange, v);

            return v;
        }
        else if (xtype == 'timeview')
        {
            var state = this.getStateManager();
            var v = Ext.create('Connector.view.Time', {
                ui  : 'custom',
                state : state
            });

            return v;
        }
        else if (xtype == 'compareview')
        {
            var state = this.getStateManager();
            var v = Ext.create('Connector.view.Compare', {
                ui  : 'custom',
                state : state
            });

            return v;
        }
    },

    updateView : function(xtype, context) {
        if (xtype === 'plot') {
            this.getStateManager().clearSelections();
        }
    },

    getDefaultView : function() {
        return 'plot';
    }
});
