Ext.define('Connector.controller.Connector', {
    extend: 'LABKEY.app.controller.View',

    /**
     * A set of 'shortcuts' that are lazily initialized that allow for this class to quickly access known sub-components.
     * An example is a ref of 'center' will provide a method on this class of 'getCenter()' which will hand back the matching
     * selection from the Component Query.
     * Component Query:  http://docs.sencha.com/ext-js/4-0/#!/api/Ext.ComponentQuery
     */
    refs : [{
        selector : 'app-main > panel[region=center]',
        ref : 'center'
    },{
        selector : 'app-main > panel[region=north]',
        ref : 'north'
    },{
        selector : 'app-main > panel[region=west]',
        ref : 'west'
    },{
        selector : 'app-main > panel[region=east]',
        ref : 'east'
    }],

    init : function() {

        // Listen for when views are added to the center view and register that components xtype
        this.control('app-main > #primarytabpanel', {
            // See http://docs.sencha.com/ext-js/4-0/#!/api/Ext.tab.Panel-method-add
            add : function (tp, comp) {
                this._addTab(comp.xtype);
            }
        });

        // Since the Connector.panel.Header does not have its own controller this controller is provided.
        // this.requestCollapse = false;
        this.control('connectorheader', {
            // See http://docs.sencha.com/ext-js/4-0/#!/api/Ext.tab.Panel-event-afterrender
            afterrender : function(c) {
                this.hdr = c;
            },
            // See Connector.panel.Header event 'headerclick'.
            headerclick : function() {
                this.changeView('summary');
            }
        });

        /**
         * This map keys of known 'xtype's of views that will be managed by the application. The map values are
         * the associated functions for either showing or hiding that view 'type'. If these are not provided then a
         * default show/hide method is provided.
         */
        this.actions = {
            hide : {
                'filtersave' : {fn: this.hideFilterSaveView, scope: this},
                'groupsave'  : {fn: this.hideGroupSaveView, scope: this}
            },
            show : {
                'filtersave' : {fn: this.showFilterSaveView, scope: this},
                'groupsave'  : {fn: this.showGroupSaveView, scope: this}
            }
        };

        this.callParent();
    },

    /**
     * @private
     * Adds a tab to the tab mapping for the center region.
     * @param xtype
     */
    _addTab : function(xtype) {
        this.tabMap[xtype] = this.getCenter().items.length;
    },

    /**
     * @private
     * Ensures the east region is shown and the active tab is set.
     * @param xtype
     */
    _showEastView : function(xtype, context) {
        if (!this.viewMap[xtype]) {
            this.viewMap[xtype] = this.createView(xtype, context);
        }

        this.getEast().add(this.viewMap[xtype]);
        this.getEast().setActiveTab(this.viewMap[xtype]);
    },

    showNotFound : function() {
        if (!this.viewMap['notfound']) {
            this.viewMap['notfound'] = Ext.create('Connector.view.NotFound', {});
            this.getCenter().add(this.viewMap['notfound']); // adds to tab map
        }
        this.showView('notfound');
    },

    showFilterSaveView : function(xtype, cb) {
        this._showEastView(xtype);
    },

    hideFilterSaveView : function(xtype, cb) {
        this.getEast().setActiveTab(0);
    },

    showGroupSaveView : function(xtype, cb) {
        this._showEastView(xtype);
    },

    hideGroupSaveView : function(xtype, cb) {
        this.getEast().setActiveTab(0);
    }
});

Ext.define('Connector.view.NotFound', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.notfound',

    ui: 'custom',

    style: 'padding: 20px; background-color: transparent;',

    html: '<h1 style="font-size: 200%;">404: View Not Found</h1><div style="font-size: 200%;">These aren\'t the subjects you\'re looking for. Move along.</div>'
});