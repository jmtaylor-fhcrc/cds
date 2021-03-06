/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define("Connector.view.Header", {
    extend: 'Ext.panel.Panel',

    alias: 'widget.connectorheader',

    layout: 'hbox',

    height: 56,

    cls: 'connectorheader',

    ui: 'custom',

    defaults: {
        ui: 'custom'
    },

    initComponent : function() {

        var toolBarItems = [];

        if (LABKEY.user.isSignedIn) {
            toolBarItems = [{
                xtype: 'box',
                itemId: 'feedback',
                margin: '2 40 0 0',
                autoEl: {
                    tag: 'a',
                    cls: 'logout',
                    html: 'Give feedback'
                },
                listeners: {
                    click: function(evt, el) {
                        Connector.panel.Feedback.displayWindow(el);
                    },
                    element: 'el',
                    scope: this
                }
            },{
                xtype: 'box',
                itemId: 'help',
                margin: '2 40 0 0',
                autoEl: {
                    tag: 'a',
                    cls: 'logout',
                    html: 'Help'
                },
                listeners: {
                    click: function(evt, el) {
                        Connector.panel.HelpCenter.displayWindow(el);
                    },
                    element: 'el',
                    scope: this
                }
            },{
                xtype: 'box',
                itemId: 'logout',
                margin: '2 40 0 0',
                autoEl: {
                    tag: 'a',
                    cls: 'logout',
                    html: 'Logout'
                },
                listeners: {
                    click : function() {
                        Ext.Ajax.request({
                            url : LABKEY.ActionURL.buildURL('login', 'logoutAPI.api'),
                            method: 'POST',
                            success: function(response) {
                                this.fireEvent('userLogout');
                                if (Ext.decode(response.responseText).success) {
                                    LABKEY.user.isSignedIn = false;
                                    window.location.reload();
                                }
                            },
                            failure: Ext.emptyFn,
                            scope: this
                        });
                    },
                    element: 'el',
                    scope: this
                }
            }];
        }

        this.items = [{
            xtype: 'box',
            itemId: 'logo',
            cls: 'logo',
            flex: 4,
            tpl: [
                '<img src="{imgSrc}" width="56" height="56">',
                '<h2>CAVD <span>DataSpace</span></h2>'
            ],
            data: {
                imgSrc: LABKEY.contextPath + '/Connector/images/logo.png'
            }
        },{
            xtype: 'panel',
            layout: 'hbox',
            itemId: 'search',
            margin: '18 5 0 0',
            items: toolBarItems
        }];

        this.callParent();
    }
});
