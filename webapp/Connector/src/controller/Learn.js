/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.controller.Learn', {

    extend: 'Connector.controller.AbstractViewController',

    views: ['Learn'],

    init : function() {

        this.control('learn', {
            //
            // When a dimension is selected the following event is fired. This is used in coordination
            // with this.updateLock to ensure that an infinite loop does not occur
            //
            selectdimension: this.onSelectDimension
        });

        this.control('infobutton', {
            click : function(e) {
                var detail     = e.record.data;
                detail.simple  = true;
                var key        = e.dimension.getName().split('.')[0];
                this.onDetail(key, detail);
            }
        });

        this.control('detailstatus', {
            itemclick : function(view, rec, node, idx) {
                var r = view.getStore().getAt(idx+1);
                var detail = r.data;
                var key = detail.hierarchy.split('.')[0];
                this.onDetail(key, detail);
            }
        });

        //
        // Hook into the primary controller hide/show actions for learn views
        //
//        this.getViewManager().registerShowAction('learn', this.showAction, this);

        this.callParent();
    },

    showAction : function(xtype, context) {
        var vm = this.getViewManager();
        var center = vm.getCenter();

        if (!vm.viewMap[xtype] || !vm.tabMap[xtype]) {
            vm.viewMap[xtype] = vm.createView(xtype, context);
            center.add(vm.viewMap[xtype]);
        }

        var pre = center.getActiveTab();
        var postIdx = vm.tabMap[xtype];

        this.innerTransition = false;

        if (pre) {
            var preType = pre.getXType();

            if (preType == 'learn') {
                //
                // This is a transition within the learn view
                //
                this.innerTransition = true;
//                this.updateView(xtype, context);
            }
            else {
                pre.getEl().fadeOut({
                    callback: function() {
                        center.setActiveTab(postIdx);
                        vm.fadeInView(xtype);
                        Ext.defer(function() { pre.getEl().fadeIn(); }, 200, pre);
                    }
                });
            }
        }
        else {
            center.setActiveTab(postIdx);
            vm.fadeInView(xtype);
        }
        vm.showStatusView('filterstatus');
    },

    createView : function(xtype, config, context) {

        var type = '';
        var c;
        if (Ext.isArray(config)) {
            c = { ctx: config };
        }
        else {
            c = config || {};
        }

        if (xtype == 'learn') {
            type = 'Connector.view.Learn';

            Ext.applyIf(c, {
                ui: 'custom',
                state: this.getStateManager()
            });

            var v = Ext.create(type, c);

            v.on('afterrender', function(v) { this.bindLearnView(v, c.ctx); }, this);

            return v;
        }
    },

    bindLearnView : function(v, context) {
        //
        // Bind the dimensions to the view
        //
        this.getStateManager().onMDXReady(function(mdx) {

            var dims = mdx.getDimensions();

            v.setDimensions(dims);

            var select = function() {
                v.getHeader().getHeaderView().selectDimension();
            };

            var defer = false;
            //
            // Set the active dimension
            //
            if (context && context.length > 0) {
                var dim = mdx.getDimension(context);
                if (dim) {
                    for (var d=0; d < dims.length; d++) {
                        if (dims[d].uniqueName == dim.uniqueName) {
                            this.updateLock = true;
                            v.selectDimension(dims[d]);
                            this.updateLock = false;
                            break;
                        }
                    }
                }
                else {
                    defer = true;
                }
            }
            else {
                defer = true;
            }

            if (defer) {
                Ext.defer(select, 200, this);
            }
        });
    },

    updateView : function(xtype, context) {
        if (xtype == 'learn') {
            this.updateLearnView(context);
        }
    },

    updateLearnView : function(context) {
        this.getStateManager().onMDXReady(function(mdx) {
            var v = this.getViewManager().getViewInstance('learn');
            if (v) {
                var dim;
                if (context.length > 0) {
                    dim = mdx.getDimension(context[0]);
                }
                if (dim) {
                    //
                    // Only update the active dimension when the dimension
                    // can be found in the context
                    //
                    this.updateLock = true;
                    v.selectDimension(dim, this.innerTransition);
                    this.innerTransition = false;
                    this.updateLock = false;
                }
            }
            else {
                console.warn('Unable to find view instance (learn)');
            }
        }, this);
    },

    onSelectDimension : function(dimension) {
        if (!this.updateLock) {
            this.getViewManager().changeView('learn', 'learn/' + dimension.get('name'), 'Learn About: ' + dimension.get('pluralName'));
        }
    },

    /**
     * This is called when the app internally requests that a details view be shown (e.g. click a 'view info' button)
     */
    onDetail : function(key, detail) {

        var context = key;
        var action = 'learn';

        if (detail && Ext.isString(detail.value) && detail.value.length > 0) {
            context += '/' + detail.value;
        }

        this.getViewManager().changeView(action, action + '/' + context);
    }
});