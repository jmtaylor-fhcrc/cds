Ext.define('Connector.controller.FilterStatus', {

    extend: 'Connector.controller.AbstractViewController',

    requires: ['Ext.panel.Panel'],

    stores: ['FilterStatus'],

    views: ['DetailStatus', 'FilterSave', 'FilterStatus'],

    init : function() {

        this.control('app-main > #eastview > #navfilter', {
            afterrender : function(navfilter) {
                navfilter.add(this.createFilterStatus(), this.createFilterDetail());
            }
        });

        this.control('selectionstatus > dataview', {
            itemclick : this.onSelectionClick
        });

        this.control('selectionview', {
            operatorchange : function(config) {
                this.getStateManager().setFilterOperator(config.filterId, config.value);
            }
        });

        this.control('selectionpanel > container > selectionview', {
            removefilter : function(filterId, hName, uname) {
                this.getStateManager().removeSelection(filterId, hName, uname);
            }
        });

        this.control('filterpanel > container > selectionview', {
            removefilter : function(filterId, hName, uname) {
                this.getStateManager().removeFilter(filterId, hName, uname);
            }
        });

        this.control('filtersave > form > toolbar > #cancelsave', {
            click : this.onFilterCancel
        });

        this.control('filterpanel > container > #clear', {
            click : this.onFilterClear
        });

        this.control('filterpanel > toolbar > #sClear', {
            click : this.onSelectionClear
        });

        this.control('#overlap', {
            click : this.runSelectToFilterAnimation
        });

        this.callParent();
    },

    getStateStore : function() {
        var store = this.getStore('FilterStatus');
        store.state = this.getStateManager();
        return store;
    },

    createFilterDetail : function() {
        var store = this.getStateStore();
        store.load();

        var view = Ext.create('Connector.view.DetailStatus', {
            store : store
        });

        var state = this.getStateManager();
        state.on('filtercount', view.onFilterChange, view);
        state.on('filterchange', view.onFilterChange, view);
        state.on('selectionchange', view.onFilterChange, view);

        return view;
    },

    createFilterStatus : function() {
        var store = this.getStateStore();
        var state = this.getStateManager();
        store.load();

        var view = Ext.create('Connector.view.FilterStatus', {
            store: store,
            selections: state.getSelections(),
            filters: state.getFilters()
        });

        state.on('filtercount', view.onFilterChange, view);
        state.on('filterchange', view.onFilterChange, view);
        state.on('selectionchange', view.onSelectionChange, view);

        return view;
    },

    runSelectToFilterAnimation : function(b) {
        this.getStateManager().moveSelectionToFilter();
//        var p = b.up('panel');
//        var me = this;
//        p.dockedItems.items[0].getEl().fadeOut();
//        Ext.get(Ext.query('.header', p.getEl().id)[0]).fadeOut({
//            listeners : {
//                afteranimate : function(){
//                    p.getEl().slideOut('t', {
//                        duration  : 250,
//                        listeners : {
//                            afteranimate : function() {
//                                p.dockedItems.items[0].getEl().fadeIn();
//                                me.getStateManager().moveSelectionToFilter();
//                            },
//                            scope : p
//                        }
//                    });
//                },
//                scope: p
//            }
//        });
    },

    createView : function(xtype, context) {

        var v; // the view instance to be created

        if (xtype == 'filterstatus') {
            v = this.createFilterStatus();
        }

        if (xtype == 'filtersave') {
            v = Ext.create('Connector.view.FilterSave', {
                flex : 1
            });
        }

        return v;
    },

    updateView : function(xtype, context) { },

    onFilterCancel : function() {
        this.getViewManager().hideView('filtersave');
    },

    onFilterClear : function() {
        var state = this.getStateManager();
        if (state.hasFilters()) {
            state.clearFilters();
        }
    },

    onFilterSave : function() {
        this.getViewManager().showView('filtersave');
    },

    onGroupSaved : function(grp, filters) {

        var group = Ext.create('Connector.model.FilterGroup', {
            name : grp.category.label,
            filters : filters
        });

        this.getStateManager().setFilters([group]);
        this.getViewManager().hideView('groupsave');
    },

    onSelectionClick : function() {
        this.getStateManager().clearSelections();
    },

    onSelectionClear : function() {
        this.getStateManager().clearSelections();
    }
});
