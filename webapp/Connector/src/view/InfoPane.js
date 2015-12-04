/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.InfoPane', {

    extend: 'Ext.container.Container',

    alias: 'widget.infopane',

    ui: 'custom',

    layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'start'
    },

    margin: 0,

    padding: 0,

    flex: 1,

    cls: 'infopane',

    autoScroll: true,

    showTitle: true,

    showSort: true,

    isShowOperator: true,

    displayTitle: '',

    mutated: false,

    initComponent : function() {

        // This is the set of keys for selected records to determine if the filter has been mutated
        this.initialKeys = [];

        var btnId = Ext.id();
        var model = this.getModel();

        // If the model does not provide a title, use the panes default displayTitle
        if (Ext.isString(model.get('title')) && model.get('title').length == 0) {
            model.set('title', this.displayTitle);
        }

        this.items = [];

        if (this.showTitle) {
            this.items.push({
                xtype: 'box',
                tpl: new Ext.XTemplate(
                    '<h2>{title:htmlEncode}</h2>'
                ),
                data: model.data,
                listeners: {
                    afterrender: function(box) {
                        this.getModel().on('change', function(m) {
                            this.update(m.data);
                        }, box);
                    },
                    scope: this
                }
            });
        }

        if (this.showSort) {
            this.items.push({
                xtype: 'container',
                ui: 'custom',
                layout: { type: 'hbox' },
                items: [{
                    xtype: 'box',
                    tpl: new Ext.XTemplate(
                            '<div class="sorter">',
                                '<span class="sorter-label">Sorted by:</span>',
                                '<span class="sorter-content">{hierarchyLabel:htmlEncode}</span>',
                            '</div>'
                    ),
                    data: model.data,
                    listeners: {
                        afterrender: function(box) {
                            var model = this.getModel();
                            box.update(model.data);

                            model.on('change', function(m) {
                                this.update(m.data);
                            }, box);
                        },
                        scope: this
                    },
                    flex: 10
                },{
                    id: btnId,
                    xtype: 'imgbutton',
                    itemId: 'infosortdropdown',
                    cls: 'sortDropdown ipdropdown', // tests
                    style: 'float: right;',
                    vector: 21,
                    width: 21,
                    margin: '20 0 0 20',
                    menu: {
                        xtype: 'menu',
                        autoShow: true,
                        itemId: 'infosortedmenu',
                        showSeparator: false,
                        width: 200,
                        ui: 'custom',
                        cls: 'infosortmenu',
                        btn: btnId,
                        listeners: {
                            afterrender: this.bindSortMenu,
                            scope: this
                        }
                    },
                    listeners: {
                        afterrender : function(b) {
                            b.showMenu(); b.hideMenu(); // allows the menu to layout/render

                            // we don't want to show the dropdown if there is only one item to select (current one)
                            if (!b.hidden && Ext.isDefined(b.menu) && b.menu.items.items.length < 2) {
                                b.hide();
                            }
                        },
                        scope: this
                    }
                }]
            });
        }

        var middleContent = {
            xtype: 'container',
            itemId: 'middle',
            flex: 10,
            autoScroll: true,
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'start'
            },
            items: []
        };

        if (this.isShowOperator && model.isShowOperator()) {
            middleContent.items.push({
                itemId: 'operatorlabel',
                xtype: 'box',
                tpl: new Ext.XTemplate(
                    '<div style="margin-top: 20px;">',
                        '<span class="label">Subjects can fall into multiple types.</span>',
                    '</div>'
                ),
                data: {}
            });
            middleContent.items.push({
                itemId: 'operator',
                xtype: 'radiogroup',
                columns: 1,
                allowBlank: false,
                validateOnBlur: false,
                items: [
                    {
                        boxLabel: 'Subjects related to any (OR)',
                        name: 'operator',
                        inputValue: LABKEY.app.model.Filter.OperatorTypes.OR,
                        checked: model.isOR()
                    },{
                        boxLabel: 'Subjects related to all (AND)',
                        name: 'operator',
                        inputValue: LABKEY.app.model.Filter.OperatorTypes.AND,
                        checked: model.isAND()
                    }
                ],
                listeners: {
                    change: this.onOperatorChange,
                    scope: this
                }
            });
        }

        var contents = this.getMiddleContent(model);
        Ext.each(contents, function(content) {
            middleContent.items.push(content);
        }, this);

        this.items.push(middleContent);

        //
        // Toolbar configuration
        //
        this.items.push(this.getToolbarConfig(model));

        this.callParent();
        this.bindModel();

        var state = Connector.getState();
        state.on('selectionchange', function() { this.hide(); }, this, {single: true});
        state.on('filterchange', function() { this.hide(); }, this, {single: true});
    },

    getMiddleContent : function(model) {
        var isSelectionMode = Connector.getState().getSelections().length > 0;
        var memberGrid = Ext.create('Ext.grid.Panel', {
            xtype: 'grid',
            itemId: 'membergrid',
            store: this.getMemberStore(),
            viewConfig : { stripeRows : false },

            /* Selection configuration */
            selType: 'checkboxmodel',
            selModel: {
                checkSelector: 'td.x-grid-cell-row-checker'
            },
            multiSelect: true,

            /* Column configuration */
            enableColumnHide: false,
            enableColumnResize: false,
            columns: [{
                xtype: 'templatecolumn',
                header: 'All',
                dataIndex: 'name',
                flex: 1,
                sortable: false,
                menuDisabled: true,
                tpl: new Ext.XTemplate(
                    '<div title="{name:htmlEncode}">{name:htmlEncode}',
                    '<tpl if="hasDetails === true">',
                        '<a class="expando" href="{detailLink}">',
                            '<span class="icontext">learn about</span>',
                            '<img src="' + Connector.resourceContext.path + '/images/cleardot.gif" class="iconone">',
                        '</a>',
                    '</tpl>',
                    '</div>'
                )
            }],

            /* Grouping configuration */
            requires: ['Ext.grid.feature.Grouping'],
            features: [{
                ftype: 'grouping',
                collapsible: false,
                groupHeaderTpl: new Ext.XTemplate(
                        '{name:this.renderHeader}', // 'name' is actually the value of the groupField
                        {
                            renderHeader: function(v) {
                                if (isSelectionMode){
                                    return v ? 'Has data in current selection' : 'No data in current selection';
                                }
                                else {
                                    return v ? 'Has data in active filters' : 'No data in active filters';
                                }
                            }
                        }
                )
            }],

            /* Styling configuration */
            border: false,
            ui: 'custom',
            cls : 'measuresgrid infopanegrid',

            listeners: {
                viewready : function(grid) {
                    this.gridready = true;
                },
                selectionchange : function(selModel, selections) {

                    // compare keys to determine mutation
                    var keys = [];
                    Ext.each(selections, function(model) {
                        keys.push(model.internalId);
                    });
                    keys.sort();
                    this.mutated = !Ext.Array.equals(this.initialKeys, keys);

                    this.filterBtn.setDisabled(selections.length == 0);
                },
                selectioncomplete: function() {

                    this.mutated = false;
                    this.initialKeys = [];

                    Ext.each(this.getGrid().getSelectionModel().getSelection(), function(model) {
                        this.initialKeys.push(model.internalId);
                    }, this);

                    this.initialKeys.sort();
                },
                scope: this
            }
        });

        // plugin to handle loading mask for this grid
        memberGrid.addPlugin({
            ptype: 'loadingmask',
            configs: [{
                element: memberGrid,
                beginEvent: 'render',
                endEvent: 'selectioncomplete'
            }]
        });

        return [memberGrid];
    },

    getToolbarConfig : function(model) {
        return {
            xtype: 'toolbar',
            dock: 'bottom',
            ui: 'lightfooter',
            items: ['->',
                {
                    text: model.isFilterBased() ? 'Update' : 'Filter',
                    cls: 'filterinfoaction', // tests
                    handler: this.onUpdate,
                    listeners: {
                        afterrender: function(btn) {
                            this.filterBtn = btn;
                            this.getModel().on('change', function(model) {
                                btn.setText(model.isFilterBased() ? 'Update' : 'Filter');
                            }, btn);
                        },
                        scope: this
                    },
                    scope: this
                },{
                    text: 'Cancel',
                    cls: 'filterinfocancel', // tests
                    handler: function() { this.hide(); },
                    scope: this
                }
            ]
        };
    },

    bindModel : function() {
        var model = this.getModel();

        // bind view to model
        model.on('ready', this.onModelReady, this);
        if (model.isReady()) {
            this.onModelReady();
        }

        // bind model to view
        this.on('filtercomplete', model.onCompleteFilter, model);
    },

    unbindModel : function() {
        var model = this.getModel();

        // bind view to model
        model.un('ready', this.onModelReady, this);

        // bind model to view
        this.un('filtercomplete', model.onCompleteFilter, model);
    },

    onModelReady : function() {
        if (this.gridready) {
            this.updateSelections();
        }
        else {
            this.getGrid().on('viewready', this.updateSelections, this);
        }
    },

    onUpdate : function() {
        var grid = this.getGrid();

        if (grid) {
            // only create/update filter if the state has been mutated
            if (this.mutated) {
                this.fireEvent('filtercomplete', grid.getSelectionModel().getSelection(), grid.getStore().getCount());
            }
            this.hide();
        }
    },

    updateSelections : function() {

        var grid = this.getGrid(),
            sm = grid.getSelectionModel(),
            store = grid.getStore(),
            storeCount = store.getCount(),
            selItems = this.getModel().get('selectedItems'),
            members = [], idx;

        if (sm.hasSelection()) {
            sm.deselectAll();
        }

        Ext.each(selItems, function(uniqueName) {
            idx = store.findExact('uniqueName', uniqueName);
            if (idx > -1) {
                members.push(store.getAt(idx));
            }
        });

        if (members.length > 0) {

            if (members.length == storeCount) {
                sm.selectAll(true);
            }
            else {
                sm.select(members, false, true);
            }

            var anodes = Ext.DomQuery.select('a.expando');
            var nodes = Ext.DomQuery.select('a.expando *');
            anodes = anodes.concat(nodes);
            Ext.each(anodes, function(node) {
                Ext.get(node).on('click', function(evt) {
                    evt.stopPropagation();
                });
            });
        }

        //
        // Configure default operator
        //
        var model = this.getModel();
        var hierarchy = model.get('hierarchy');

        if (model.isShowOperator()) {
            if (model.isREQ()) {
                this.hideOperator();
            }
            else {
                this.showOperator();
            }
        }

        grid.fireEvent('selectioncomplete', this);
    },

    onOperatorChange : function(radio, newValue) {
        this.getModel().changeOperator(newValue.operator);
    },

    showOperator : function() {
        this.getComponent('middle').getComponent('operator').show();
        this.getComponent('middle').getComponent('operatorlabel').show();
    },

    hideOperator : function() {
        this.getComponent('middle').getComponent('operator').hide();
        this.getComponent('middle').getComponent('operatorlabel').hide();
    },

    setMenuContent : function(menu, model) {
        menu.removeAll();

        var items = model.get('hierarchyItems');

        Ext.each(items, function(item) {
            menu.add(item);
        });
    },

    bindSortMenu : function(menu) {
        this.setMenuContent(menu, this.getModel());

        this.getModel().on('change', function(model) {
            this.setMenuContent(menu, model);
        }, this);

        menu.on('click', this.onSortSelect, this);
    },

    onSortSelect : function(menu, item) {
        var _item = Ext.clone(item);
        if (_item.isLevel) {
            this.getModel().configure(null, null, _item.uniqueName);
        }
        else {
            this.getModel().configure(null, _item.uniqueName);
        }
    },

    getGrid : function() {
        return this.getComponent('middle').getComponent('membergrid');
    },

    getModel : function() {
        return this.model;
    },

    getMemberStore : function() {
        return this.getModel().get('memberStore');
    }
});
