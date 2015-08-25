/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.panel.Feedback', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.feedback',

    cls: 'feedback variable-selector',

    border: false,

    statics: {
        displayWindow : function(animateTarget) {
            var win = Ext.create('Ext.window.Window', {
                ui: 'axiswindow',
                border: false,
                modal: true,
                resizable: false,
                draggable: false,
                header: false,
                layout: {
                    type: 'fit'
                },
                items: [{
                    xtype: 'feedback',
                    listeners: {
                        hide: function() {
                            win.hide(animateTarget);
                        },
                        scope: this
                    }
                }],
                width: 520
            });

            win.show(animateTarget);
        }
    },

    initComponent : function() {
        this.items = [
            this.getHeader(),
            this.getForm(),
            this.getFooter()
        ];
        this.callParent();
    },

    getForm : function() {

        if (!this.feedbackForm) {
            this.feedbackForm = Ext.create('Ext.form.Panel', {
                ui: 'custom',
                bodyPadding: 5,
                border: false,
                flex: 1,
                items: [{
                    xtype: 'textfield',
                    name: 'title',
                    emptyText: 'Title',
                    width: '100%',
                    validateOnBlur: false,
                    allowBlank: false
                },{
                    xtype: 'textareafield',
                    name: 'comment',
                    width: '100%',
                    emptyText: 'Describe what you\'re seeing...',
                    validateOnBlur: false,
                    allowBlank: false
                },{
                    //If this box is checked the current page url is printed in the comments
                    xtype: 'checkbox',
                    boxLabel: 'Check this box if its the screen you are currently on',
                    name: 'url',
                    checked: false,
                    inputValue: window.location.href
                }]
            });
        }

        return this.feedbackForm;
    },

    getHeader : function() {
        if (!this.headerPanel) {
            var initialData = {
                title: this.headerTitle,
                showCount: false,
                border: true
            };

            var tpl = new Ext.XTemplate(
                '<div class="main-title">Provide Feedback</div>',
                    '<div class="sub-title">',
                    '<span>Give us feedback on what we could improve</span>',
                '</div>'
            );
            this.headerPanel = Ext.create('Ext.panel.Panel', {
                cls: 'header',
                border: false,
                tpl: tpl,
                data: initialData
            });
        }

        return this.headerPanel;
    },

    postFeedback : function(title, comments, url) {

        var config = {
            url: LABKEY.ActionURL.buildURL('issues', 'insert.view'),
            method: 'POST',
            params: {
                issueId: 0,
                action: 'org.labkey.issue.IssuesController$InsertAction',
                title: title,
                priority: 3,
                comment: comments
            }
        };

        if (url) {
            config.params.comment += '\n\n' + url;
        }

        Ext.Ajax.request(config);
    },

    getFooter : function() {
        if (!this.footerPanel) {
            this.footerPanel = Ext.create('Ext.panel.Panel', {
                bodyCls: 'footer',
                border: false,
                layout: {
                    type: 'hbox',
                    pack: 'end'
                },
                items: [{
                    itemId: 'cancel-link',
                    xtype: 'button',
                    ui: 'rounded-inverted-accent-text',
                    hidden: false,
                    text: 'Cancel',
                    listeners: {
                        click: function(evt, el) {
                            this.hide();
                        },
                        element: 'el',
                        scope: this
                    },
                    handler: function() {
                        this.fireEvent('cancel');
                    },
                    scope: this
                },{
                    itemId: 'done-button',
                    xtype: 'button',
                    text: 'Done',
                    listeners: {
                        click: function(evt, el) {
                            var form = this.getForm();

                            //checks whether both the title and comment sections are printed out
                            if (form.isValid()) {
                                var values = form.getValues();
                                this.postFeedback(values.title, values.comment, values.url);
                                this.hide();
                                alert('Form Sent');
                            }

                            //prints an alert otherwise
                            else {
                                alert('You need to specify the title and the comment!');
                            }

                        },
                        element: 'el',
                        scope: this
                    },
                    scope: this
                }]
            });
        }

        return this.footerPanel;
    }
});