Ext.define('Connector.view.PlotPane', {

    extend: 'Connector.view.InfoPane',

    padding: '10',

    showSort: false,

    showOperator: false,

    maxHeight: 400,

    initComponent : function() {
        this.callParent();
    },

    getMiddleContent : function(model) {
        var filter = model.get('filter');
        var measures = filter.get('plotMeasures');
        var scales = filter.get('plotScales');

        var content = [{
            xtype: 'box',
            autoEl: {
                tag: 'div',
                html: 'This filter includes only subjects with data for the following variables.'
            }
        }];

        if (Ext.isArray(measures)) {
            // assume length 3 array of (x, y, color)?
            if (measures[1] && measures[1].measure) {
                // Y
                content.push({
                    xtype: 'box',
                    cls: 'smallstandout soft spacer',
                    autoEl: {
                        tag: 'div',
                        html: 'Y'
                    }
                });
                content.push({
                    xtype: 'box',
                    autoEl: {
                        tag: 'div',
                        html: measures[1].measure.queryLabel
                    }
                });
                content.push({
                    xtype: 'box',
                    autoEl: {
                        tag: 'div',
                        html: measures[1].measure.label
                    }
                });
            }
            if (measures[0] && measures[0].measure) {
                // X
                content.push({
                    xtype: 'box',
                    cls: 'smallstandout soft spacer',
                    autoEl: {
                        tag: 'div',
                        html: 'X'
                    }
                });
                content.push({
                    xtype: 'box',
                    autoEl: {
                        tag: 'div',
                        html: measures[0].measure.queryLabel
                    }
                });
                content.push({
                    xtype: 'box',
                    autoEl: {
                        tag: 'div',
                        html: measures[0].measure.label
                    }
                });
            }
        }
        return content;
    },

    getToolbarConfig : function(model) {
        return {
            xtype: 'toolbar',
            dock: 'bottom',
            ui: 'footer',
            items: ['->',
                {
                    text: 'clear plot',
                    cls: 'infoplotaction', // tests
                    handler: this.onUpdate,
                    scope: this
                },{
                    text: 'cancel',
                    cls: 'infoplotcancel', // tests
                    handler: function() { this.hide(); },
                    scope: this
                }
            ]
        }
    },

    onUpdate : function() {
        this.getModel().clearFilter();
        this.hide();
    }
});