Ext.define('Connector.controller.Home', {

    extend : 'Connector.controller.AbstractViewController',

    stores : [],

    views : ['Home'],

    init : function() {

        this.control('grouplistview', {
            itemclick: function(v, grp) {
                var filters = grp.get('filters');
                if (Ext.isString(filters))
                    filters = LABKEY.app.controller.Filter.filtersFromJSON(filters);
                else
                    filters = filters.filters;
                var group = Ext.create('Connector.model.FilterGroup', {
                    name: grp.get('label'),
                    filters: filters
                });

                this.getStateManager().addFilters([group]);
            }
        });

        this.callParent();
    },

    createView : function(xtype, config, context) {
        var v;

        if (xtype == 'home') {
            v = Ext.create('Connector.view.Home', {});
        }

        return v;
    },

    updateView : function(xtype, context) {}
});
