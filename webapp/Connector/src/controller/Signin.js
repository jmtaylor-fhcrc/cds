/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.controller.Signin', {

    extend: 'Connector.controller.AbstractViewController',

    views: ['Signin', 'TermsOfUse'],

    createView : function(xtype, context) {
        var c = { ctx: context };
        var type;
        var v;

        switch (xtype) {
        case 'signin':
            type = 'Connector.view.Signin';

            Ext.applyIf(c, {
                ui: 'custom',
                state: this.getStateManager()
            });

            v = Ext.create(type, c);

            v.on('userSignedIn', function() {
                // Start loading
                this.application.olap.load();
                window.location.reload();
            }, this);
            break;
        case 'terms':
            var terms = Ext.create('Connector.view.TermsOfUse', {});

            var header = Ext.create('Connector.view.PageHeader', {
                data: {
                    label : "<h1>Full Terms of Use Agreement: HIV Collaborative DataSpace</h1>",
                    buttons : {
                        back: true
                    },
                    scope : this
                }
            });

            var pageView = Ext.create('Connector.view.Page', {
                // scrollContent: true,
                contentViews: [terms],
                header: header,
                pageID: 'terms'
            });

            v = pageView;

            break;
        }

        return v;
    },

    updateView : function(xtype, context) { },

    getDefaultView : function() {
        return 'signin';
    },

    showAction : function(xtype, context) {
    	//
	},

    init : function() {

        this.control('homeheader', {
            boxready: this.resolveStatistics
        });

        this.callParent();
    },

    resolveStatistics : function(view) {
        var statDisplay = view.getComponent('statdisplay');
        if (statDisplay) {

            Statistics.resolve(function(stats) {
                statDisplay.update({
                    nstudy: stats.primaryCount,
                    ndatapts: stats.dataCount
                });
            }, this);
        }
    }
});
