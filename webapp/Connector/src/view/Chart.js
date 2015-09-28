/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.Chart', {

    extend: 'Ext.panel.Panel',

    requires: ['Connector.model.ChartData'],

    alias: 'widget.plot',

    cls: 'chartview',

    layout: 'border',

    ui: 'custom',

    canShowHidden: false,

    binRowLimit: 5000,

    showPointsAsBin: false,

    xGutterHeight: 90,

    yGutterWidth: 125,

    xGutterName: 'xGutterPlot',

    yGutterName: 'yGutterPlot',

    studyAxisWidthOffset: 150,

    minStudyAxisHeight: 75,

    disableAutoMsg: false,

    initiatedBrushing: '',

    statics: {
        // Template used for contents of VisitTag tooltips
        studyAxisTipTpl: new Ext.XTemplate(
            '<tpl if="isAggregate">',
                '<tpl for="groups">',
                    '<div style="margin: 0 20px; text-indent: -20px">',
                        '<span style="font-weight: bold;">{label:htmlEncode}: </span>',
                        '{tags:this.renderTags}',
                    '</div>',
                '</tpl>',
            '<tpl else>',
                '<tpl for="groups">',
                    '<div style="margin: 0 20px; text-indent: -20px">',
                        '<span style="font-weight: bold;">{label:htmlEncode}: </span>',
                        '<tpl if="isVaccination">',
                            '{desc:htmlEncode}',
                            '<br/>',
                            '{tags:this.renderTags}',
                        '<tpl else>',
                            '{tags:this.renderTags}',
                        '</tpl>',
                    '</div>',
                '</tpl>',
            '</tpl>',
            {
                renderTags: function(tags) {
                    return Ext.htmlEncode(tags.join(', '));
                }
            }
        )
    },

    constructor : function(config) {

        if (LABKEY.devMode) {
            PLOT = this;
        }

        Ext.apply(config, {
            isActiveView: true,
            refreshRequired: true,
            initialized: false
        });

        Ext.applyIf(config, {
            measures: [],
            hasStudyAxisData: false
        });

        var params = LABKEY.ActionURL.getParameters();
        if (Ext.isDefined(params['maxRows'])) {
            var num = parseInt(params['maxRows']);
            if (Ext.isNumber(num)) {
                this.binRowLimit = num;
            }
        }
        if (Ext.isDefined(params['_disableAutoMsg'])) {
            this.disableAutoMsg = true;
        }

        this._ready = false;
        Connector.getState().onReady(function() {
            this._ready = true;
            this.fireEvent('onready', this);
        }, this);

        this.callParent([config]);

        this.addEvents('onready', 'userplotchange');

        this.labelTextColor = ChartUtils.colors.HEATSCALE1;
        this.labelTextHltColor = ChartUtils.colors.WHITE;
        this.labelBkgdColor = ChartUtils.colors.WHITE;
        this.labelBkgdHltColor = ChartUtils.colors.SELECTED;
    },

    initComponent : function() {

        this.items = [
            this.getNorth(),
            this.getCenter(),
            this.getSouth()
        ];

        this.callParent();

        this.attachInternalListeners();

        // plugin to handle loading mask for the plot region
        this.addPlugin({
            ptype: 'loadingmask',
            beginConfig: {
                component: this,
                events: ['showload']
            },
            endConfig: {
                component: this,
                events: ['hideload']
            }
        });

        this.addPlugin({
            ptype: 'messaging',
            calculateY : function(cmp, box, msg) {
                return box.y - 10;
            }
        });

        this.on('beforehide', this.hideVisibleWindow);

        this.applyIEPolyfills();
    },

    applyIEPolyfills: function() {
        if (!Ext.isFunction(CustomEvent)) {
            // Code from: https://developer.mozilla.org/en-US/docs/Web/API/CustomEvent
            var CustomEvent = function(event, options) {
                options = options || { bubbles: false, cancelable: false, detail: undefined };
                var e = document.createEvent('CustomEvent');
                e.initCustomEvent(event, options.bubbles, options.cancelable, options.detail);
                return e;
            };

            CustomEvent.prototype = window.Event.prototype;

            window.CustomEvent = CustomEvent;
        }
    },

    getNoPlotMsg : function() {
        if (!this.noplotmsg) {
            this.noplotmsg = Ext.create('Ext.Component', {
                renderTo: this.body,
                cls: 'noplotmsg',
                hidden: true,
                autoEl: {
                    tag: 'div',
                    children: [{
                        tag: 'h1',
                        cls: 'line1',
                        html: 'Choose a "y" variable and up to two more to plot at a time.'
                    },{
                        tag: 'h1',
                        cls: 'line2',
                        html: 'Make selections on the plot to subgroup and filter.'
                    },{
                        tag: 'h1',
                        cls: 'line3',
                        html: 'Use subgroups for further comparison.'
                    }]
                }
            });
        }

        return this.noplotmsg;
    },

    getEmptyPlotMsg : function() {
        if (!this.emptyplotmsg) {
            this.emptyplotmsg = Ext.create('Ext.Component', {
                renderTo: this.body,
                cls: 'emptyplotmsg',
                hidden: true,
                autoEl: {
                    tag: 'div',
                    children: [{
                        tag: 'h1',
                        cls: 'line1',
                        html: 'There are no data for the selected variable(s) in the current filters.'
                    }]
                }
            });
        }

        return this.emptyplotmsg;
    },

    onReady : function(callback, scope) {
        if (this._ready === true) {
            callback.call(scope);
        }
        else {
            this.on('onready', function() { callback.call(scope); }, this, {single: true});
        }
    },

    getNorth : function() {
        return {
            xtype: 'panel',
            region: 'north',
            border: false, frame: false,
            layout: {
                type: 'hbox'
            },
            items: [{
                xtype: 'container',
                margin: '16 0 0 24',
                layout: {
                    type: 'hbox',
                    pack: 'start'
                },
                items: [this.getYSelector()]
            },{
                xtype: 'container',
                flex: 1,
                margin: '16 0 0 0',
                layout: {
                    type: 'hbox',
                    pack: 'center'
                },
                items: [
                    this.getHeatmapModeIndicator(),
                    this.getMedianModeIndicator()
                ]
            },{
                xtype: 'container',
                margin: '16 24 0 0',
                layout: {
                    type: 'hbox',
                    pack: 'end'
                },
                items: [this.getColorSelector()]
            }]
        };
    },

    getHeatmapModeIndicator : function() {
        if (!this.heatmapIndicator) {
            this.heatmapIndicator = Ext.create('Ext.Component', {
                hidden: true,
                cls: 'plotmodeon',
                html: 'Heatmap on',
                width: 110,
                listeners: {
                    scope: this,
                    afterrender : function(c) {
                        c.getEl().on('mouseover', function() { this.showWhyBinning(); }, this);
                        c.getEl().on('mouseout', function() { this.fireEvent('hideheatmapmsg', this); }, this);
                    }
                }
            });
        }

        return this.heatmapIndicator;
    },

    getMedianModeIndicator : function() {
        if (!this.medianIndicator) {
            this.medianIndicator = Ext.create('Ext.Component', {
                hidden: true,
                cls: 'plotmodeon',
                html: 'Median values',
                width: 115,
                listeners: {
                    scope: this,
                    afterrender : function(c) {
                        c.getEl().on('mouseover', function() { this.showWhyMedian(); }, this);
                        c.getEl().on('mouseout', function() { this.fireEvent('hidemedianmsg', this); }, this);
                    }
                }
            });
        }

        return this.medianIndicator;
    },

    getYSelector : function() {
        if (!this.ySelector) {
            this.ySelector = Ext.create('Connector.view.Variable', {
                id: 'yvarselector',
                btnCls: 'yaxisbtn',
                model: Ext.create('Connector.model.Variable', {type: 'y'}),
                listeners: {
                    requestvariable: this.onShowVariableSelection,
                    scope: this
                }
            });
        }

        return this.ySelector;
    },

    getXSelector : function() {
        if (!this.xSelector) {
            this.xSelector = Ext.create('Connector.view.Variable', {
                id: 'xvarselector',
                btnCls: 'xaxisbtn',
                model: Ext.create('Connector.model.Variable', {type: 'x'}),
                listeners: {
                    requestvariable: this.onShowVariableSelection,
                    scope: this
                }
            });
        }

        return this.xSelector;
    },

    getColorSelector : function() {
        if (!this.colorSelector) {
            this.colorSelector = Ext.create('Connector.panel.ColorSelector', {
                id: 'colorvarselector',
                btnCls: 'colorbtn',
                model: Ext.create('Connector.model.Variable', {type: 'color'}),
                listeners: {
                    afterrender : function(c) {
                        c.getEl().on('mouseover', function() { this.showWhyBinning(); }, this);
                        c.getEl().on('mouseout', function() { this.fireEvent('hideheatmapmsg', this); }, this);
                    },
                    requestvariable: this.onShowVariableSelection,
                    scope: this
                }
            });
        }

        return this.colorSelector;
    },

    getCenter : function() {
        if (!this.centerContainer) {
            this.centerContainer = Ext.create('Ext.container.Container', {
                region: 'center',
                layout: {
                    type: 'vbox',
                    align: 'stretch',
                    pack: 'start'
                },
                items: [{
                    xtype: 'panel',
                    border: false,
                    flex: 10,
                    cls: 'plot',
                    style: {'background-color': '#FFFFFF'},
                    listeners: {
                        afterrender: {
                            fn: function(box) {
                                this.plotEl = box.getEl();
                            },
                            single: true,
                            scope: this
                        }
                    }
                },this.getStudyAxisPanel()]
            });
        }
        return this.centerContainer;
    },

    getStudyAxisPanel : function() {
        if (!this.studyAxisPanel) {
            this.studyAxisPanel = Ext.create('Ext.panel.Panel', {
                border: false,
                overflowX: 'hidden',
                overflowY: 'auto',
                frame: false,
                items: [{
                    xtype: 'box',
                    tpl: new Ext.XTemplate('<div id="study-axis"></div>'),
                    data: {}
                }]
            });
        }

        return this.studyAxisPanel;
    },

    getSouth : function() {
        return {
            xtype: 'panel',
            region: 'south',
            border: false, frame: false,
            items: [{
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    pack: 'center'
                },
                width: '100%',
                padding: '8px 0',
                items: [
                    this.getXSelector(),
                    {
                        // FOR TESTING USE (add "_showPlotData" param to URL to show button)
                        id: 'plotshowdata',
                        xtype: 'button',
                        text: 'view data',
                        style: 'left: 20px !important; top: 15px !important;',
                        hidden: LABKEY.ActionURL.getParameter('_showPlotData') ? false : true
                    }
                ]
            }]
        };
    },

    onShowVariableSelection : function() {
        this.fireEvent('hideload', this);
    },

    attachInternalListeners : function() {

        this.showTask = new Ext.util.DelayedTask(function() {
            this.onReady(this.onShowGraph, this);
        }, this);
        this.resizeTask = new Ext.util.DelayedTask(function() {
            this.onReady(this.handleResize, this);
        }, this);

        this.hideHeatmapModeTask = new Ext.util.DelayedTask(function() {
            this.fireEvent('hideheatmapmsg', this);
        }, this);
        this.hideMedianModeTask = new Ext.util.DelayedTask(function() {
            this.fireEvent('hidemedianmsg', this);
        }, this);

        this.on('resize', function() {
            this.plotEl.update('');
            this.getStudyAxisPanel().setVisible(false);
            this.getNoPlotMsg().hide();
            this.getEmptyPlotMsg().hide();

            this.resizeTask.delay(300);
        }, this);
    },

    getPlotElement : function() {
        if (this.plot) {
            var el = Ext.query('#' + this.plot.renderTo);
            if (el.length > 0) {
                el = el[0];
            }
            return el;
        }
    },

    getPlotSize : function(box) {
        var size = {};

        if (this.requireStudyAxis && this.hasStudyAxisData) {
            size.width = box.width - this.studyAxisWidthOffset;
        }
        else if (this.requireYGutter) {
            size.width = box.width - this.yGutterWidth;
        }
        else {
            size.width = box.width;
        }

        if (this.requireXGutter) {
            size.height = box.height - this.xGutterHeight;
        }
        else {
            size.height = box.height;
        }

        return size;
    },

    handleResize : function() {

        if (!this.isActiveView) {
            return;
        }

        if (this.ywin && this.ywin.isVisible()) {
            this.updateSelectorWindow(this.ywin);
        }

        if (this.xwin && this.xwin.isVisible()) {
            this.updateSelectorWindow(this.xwin);
        }

        if (this.colorwin && this.colorwin.isVisible()) {
            this.updateSelectorWindow(this.colorwin);
        }

        this.redrawPlot();
        this.resizeMessage();

        if (Ext.isFunction(this.highlightSelectedFn)) {
            this.highlightSelectedFn();
        }
    },

    redrawPlot : function() {
        if (Ext.isDefined(this.lastInitPlotParams)) {
            if (this.lastInitPlotParams.noplot) {
                this.noPlot(this.lastInitPlotParams.emptyPlot);
            }
            else {
                this.initPlot(this.lastInitPlotParams.chartData, this.lastInitPlotParams.studyAxisInfo);
            }
        }
    },

    resizePlotMsg : function(msgCmp, plotBox) {
        if (msgCmp) {
            var el = msgCmp.getEl(),
                top = (plotBox.height / 2) - 15,
                left = (plotBox.width / 2) - (msgCmp.getWidth() / 2);

            el.setStyle('margin-top', top + 'px');
            el.setStyle('margin-left', left + 'px');
        }
    },

    getNoPlotLayer : function() {
        return new LABKEY.vis.Layer({
            geom: new LABKEY.vis.Geom.Point({}),
            aes: {
                yLeft: function(row) { return row.y; }
            }
        });
    },

    mouseOverPoints : function(event, data, layerSel, point, layerScope, plotName) {
        if (!layerScope.isBrushed) {
            this.highlightPlotData(null, [data.subjectId]);
            this.pointHoverText(point, data, plotName);
        }
    },

    mouseOutPoints : function(event, data, layerSel, point, layerScope) {
        if (!layerScope.isBrushed) {
            this.clearHighlightedData();
            this.highlightSelected();
        }

        this.fireEvent('hidepointmsg');
    },

    mouseOverBins : function(event, data, layerSel, bin, layerScope, plotName) {
        if (!layerScope.isBrushed) {
            var subjectIds = [];
            data.forEach(function(b) {
                subjectIds.push(b.data.subjectId);
            });

            this.highlightPlotData(null, subjectIds);
        }
    },

    mouseOutBins : function(event, data, layerSel, bin, layerScope) {
        if (!layerScope.isBrushed) {
            this.clearHighlightedData();
            this.highlightSelected();
        }
    },

    pointHoverText : function(point, data, plotName) {
        var config, val, content = '', colon = ': ', linebreak = ',<br/>';

        if (data.xname) {
            val = Ext.typeOf(data.x) == 'date' ? ChartUtils.tickFormat.date(data.x) : data.x;
            content += Ext.htmlEncode(data.xname) + colon + val;
        }
        content += (content.length > 0 ? linebreak : '') + Ext.htmlEncode(data.yname) + colon + data.y;
        if (data.colorname) {
            content += linebreak + Ext.htmlEncode(data.colorname) + colon + data.color;
        }

        config = {
            bubbleWidth: 250,
            target: point,
            placement: plotName===this.yGutterName?'right':'top',
            xOffset: plotName===this.yGutterName?0:-125,
            yOffset: plotName===this.yGutterName?-25:0,
            arrowOffset: plotName===this.yGutterName?0:110,
            title: 'Subject: ' + data.subjectId,
            content: content
        };

        ChartUtils.showCallout(config, 'hidepointmsg', this);
    },

    getLayerAes : function(layerScope, plotName) {

        var mouseOver = this.showPointsAsBin ? this.mouseOverBins : this.mouseOverPoints,
            mouseOut = this.showPointsAsBin ? this.mouseOutBins : this.mouseOutPoints;

        return {
            mouseOverFn: Ext.bind(mouseOver, this, [layerScope, plotName], true),
            mouseOutFn: Ext.bind(mouseOut, this, [layerScope], true)
        };
    },

    getBinLayer : function(layerScope, plotNullPoints, plotName) {
        return new LABKEY.vis.Layer({
            geom: new LABKEY.vis.Geom.Bin({
                shape: 'square',
                colorDomain: [0,50], // issue 23469: Dataspace gutter plot bin shading doesn't match main plot bin shading
                colorRange: [ChartUtils.colors.UNSELECTED, ChartUtils.colors.BLACK],
                size: 10, // for squares you want a bigger size
                plotNullPoints: plotNullPoints
            }),
            aes: this.getLayerAes.call(this, layerScope, plotName)
        });
    },

    getPointLayer : function(layerScope, plotNullPoints, plotName) {
        return new LABKEY.vis.Layer({
            geom: new LABKEY.vis.Geom.Point({
                size: 3,
                plotNullPoints: plotNullPoints,
                position: plotNullPoints ? 'jitter' : undefined,
                opacity: 0.5
            }),
            aes: this.getLayerAes.call(this, layerScope, plotName)
        });
    },

    getBoxLayer : function(layerScope) {
        var aes = this.getLayerAes.call(this, layerScope),
            me = this;

        aes.boxMouseOverFn = function(event, box, data) {
            var content = '', config;

            Ext.each(['Q1', 'Q2', 'Q3'], function(type) {
                content += '<p><span style="font-weight: bold;">' + type + '</span> ' + data.summary[type] + '</p>';
            });

            config = {
                bubbleWidth: 120,
                target: box,
                placement: 'left',
                yOffset: box.getBBox().height / 2 - 55,
                arrowOffset: 35,
                title: data.name,
                content: content
            };

            ChartUtils.showCallout(config, 'hideboxplotmsg', me);
        };

        aes.boxMouseOutFn = function(event, box, data) {
            me.fireEvent('hideboxplotmsg');
        };

        return new LABKEY.vis.Layer({
            geom: new LABKEY.vis.Geom.DataspaceBoxPlot({
                binSize : 3,
                binRowLimit : this.binRowLimit
            }),
            aes: aes
        });
    },

    getBasePlotConfig : function() {
        return {
            renderTo: this.plotEl.id,
            rendererType: 'd3',
            throwErrors: true,
            clipRect: false,
            legendPos : 'none',
            gridLineWidth: 1.25,
            gridLinesVisible: 'both',
            borderWidth: 2,
            gridColor : ChartUtils.colors.WHITE,
            bgColor: ChartUtils.colors.WHITE,
            tickColor: ChartUtils.colors.WHITE,
            tickTextColor: this.labelTextColor // $heat-scale-1
        };
    },

    getMainPlotConfig : function(data, aes, scales, yAxisMargin) {
        var size = this.getPlotSize(this.plotEl.getSize());

        return Ext.apply(this.getBasePlotConfig(), {
            margins : {
                top: 25,
                left: yAxisMargin + (this.requireYGutter ? 0 : 24),
                right: 50,
                bottom: 53
            },
            width : size.width,
            height : size.height,
            data : data,
            aes : aes,
            scales : scales,
            gridLineColor : ChartUtils.colors.SECONDARY,
            borderColor : ChartUtils.colors.HEATSCALE3
        });
    },

    getGutterPlotConfig : function(margins, height, width, data, aes, scales, labels) {

        if (this.measures[2]) {
            aes.color = function(row) {return row.color};
            aes.shape = function(row) {return row.color};

            scales.color = {
                scaleType: 'discrete',
                range: LABKEY.vis.Scale.DataspaceColor()
            };
            scales.shape = {
                scaleType: 'discrete',
                range: LABKEY.vis.Scale.DataspaceShape()
            };
        }

        return Ext.apply(this.getBasePlotConfig(), {
            margins : margins,
            width : width,
            height : height,
            data : data,
            aes : aes,
            scales : scales,
            labels : labels,
            tickLength : 0,
            gridColor : ChartUtils.colors.GRIDBKGD,
            gridLineColor : ChartUtils.colors.GRIDLINE,
            borderColor : ChartUtils.colors.WHITE
        });
    },

    getScaleConfigs : function(noplot, properties, chartData, studyAxisInfo, layerScope) {
        var scales = {}, domain;

        if (noplot) {
            scales.x = {
                scaleType: 'continuous',
                domain: [0, 0],
                tickFormat: ChartUtils.tickFormat.empty
            };

            scales.yLeft = {
                scaleType: 'continuous',
                domain: [0, 0],
                tickFormat: ChartUtils.tickFormat.empty
            };
        }
        else {
            if (Ext.isDefined(properties.xaxis) && !properties.xaxis.isDimension && properties.xaxis.isContinuous) {

                // Issue 24395: Fill out domain for brushing if no data in main plot and one gutter plot.
                if(this.requireYGutter && chartData.getXDomain()[0]==null && chartData.getXDomain()[1]==null)
                    domain = [0,1];
                else
                    domain = chartData.getXDomain();

                scales.x = {
                    scaleType: 'continuous',
                    domain: domain
                };

                if (properties.xaxis.isNumeric) {
                    scales.x.tickFormat = ChartUtils.tickFormat.numeric;
                }
                else if (properties.xaxis.type === 'TIMESTAMP') {
                    scales.x.tickFormat = ChartUtils.tickFormat.date;
                }
            }
            else {
                scales.x = {
                    scaleType: 'discrete',
                    sortFn: function(a, b) {
                        // sort empty category to the right side
                        if (a == ChartUtils.emptyTxt) {
                            return 1;
                        }
                        else if (b == ChartUtils.emptyTxt) {
                            return -1;
                        }
                        return LABKEY.app.model.Filter.sorters.natural(a, b);
                    },
                    tickCls: 'xaxis-tick-text',
                    tickRectCls: 'xaxis-tick-rect',
                    tickClick: Ext.bind(this.xAxisClick, this, [layerScope], true),
                    tickMouseOver: Ext.bind(this.xAxisMouseOver, this, [layerScope], true),
                    tickMouseOut: Ext.bind(this.xAxisMouseOut, this, [layerScope], true),
                    tickRectWidthOffset: 30,
                    tickRectHeightOffset: 30,
                    fontSize: 9
                };
            }

            // Issue 24395: Fill out domain for brushing if no data in main plot and one gutter plot.
            if(this.requireXGutter && chartData.getYDomain()[0]==null && chartData.getYDomain()[1]==null)
                domain = [0,1];
            else
                domain = chartData.getYDomain();

            scales.yLeft = {
                scaleType: 'continuous',
                tickFormat: ChartUtils.tickFormat.numeric,
                tickDigits: 7,
                domain: domain
            };

            if (this.measures[2]) {
                scales.color = {
                    scaleType: 'discrete',
                    range: LABKEY.vis.Scale.DataspaceColor()
                };
                scales.shape = {
                    scaleType: 'discrete',
                    range: LABKEY.vis.Scale.DataspaceShape()
                };
            }
        }

        return scales;
    },

    getAesConfigs : function() {
        var aes = {
            x: function(row) {return row.x;},
            yLeft: function(row) {return row.y}
        };

        if (this.measures[2]) {
            aes.color = function(row) {return row.color};
            aes.shape = function(row) {return row.color};
        }

        return aes;
    },

    getPlotLayer : function(noplot, properties, layerScope) {
        if (!noplot) {
            if (Ext.isDefined(properties.xaxis) && !properties.xaxis.isDimension && properties.xaxis.isContinuous) {
                // Scatter. Binned if over max row limit.
                return this.showPointsAsBin ? this.getBinLayer(layerScope, false) : this.getPointLayer(layerScope, false);
            }
            else {
                // Box plot (aka 1D).
                return this.getBoxLayer(layerScope);
            }
        }

        return this.getNoPlotLayer();
    },

    /**
     * @param chartData
     * @param {object} [studyAxisInfo]
     * @param {boolean} [noplot=false]
     * @param {boolean} [emptyPlot=false]
     */
    initPlot : function(chartData, studyAxisInfo, noplot, emptyPlot) {

        if (this.isHidden()) {
            // welp, that was a huge waste..
            // Consider: This could wrapped up in something like this.continue()
            // where if we do not continue, we will set refresh
            this.refreshRequired = true;
            return;
        }

        var allDataRows, properties, yAxisMargin = 60,
            layerScope = {plot: null, isBrushed: false},
            scaleConfig, aesConfig,
            plotConfig, gutterXPlotConfig, gutterYPlotConfig;

        noplot = Ext.isBoolean(noplot) ? noplot : false;

        // Issue 23731: hold onto last set of properties for resize/redraw
        this.lastInitPlotParams = {
            chartData: chartData,
            studyAxisInfo: studyAxisInfo,
            noplot: noplot,
            emptyPlot: emptyPlot
        };

        // get the data rows for the chart
        if (chartData instanceof Connector.model.ChartData) {
            allDataRows = chartData.getDataRows();
            properties = chartData.getProperties();
        }
        else {
            allDataRows = {
                main: chartData,
                totalCount: chartData.length
            };
        }

        this.requireXGutter = Ext.isDefined(allDataRows) && Ext.isDefined(allDataRows.undefinedY) && (allDataRows.undefinedY.length>0);
        this.requireYGutter = Ext.isDefined(allDataRows) && Ext.isDefined(allDataRows.undefinedX) && (allDataRows.undefinedX.length>0);

        this.plotEl.update('');
        this.resizePlotContainers(studyAxisInfo ? studyAxisInfo.getData().length : 0);

        if (this.plot) {
            this.plot.clearGrid();
            this.plot = null;
        }

        this.logRowCount(allDataRows);

        this.showPointsAsBin = allDataRows ? allDataRows.totalCount > this.binRowLimit : false;
        ChartUtils.BRUSH_DELAY = this.showPointsAsBin ? 0 : (allDataRows.totalCount > 2500 ? 20 : 0);
        this.toggleHeatmapMode();

        this.showAsMedian = chartData instanceof Connector.model.ChartData ? chartData.usesMedian() : false;
        this.toggleMedianMode();

        var me = this;
        this.highlightSelectedFn = function() {
            if (me.plot && !layerScope.isBrushed) {
                me.highlightLabels.call(me, me.plot, me.getCategoricalSelectionValues(), me.labelTextHltColor, me.labelBkgdHltColor, true);
                me.highlightSelected.call(me);
            }
        };

        this.selectionInProgress = null;

        scaleConfig = this.getScaleConfigs(noplot, properties, chartData, studyAxisInfo, layerScope);
        aesConfig = this.getAesConfigs();
        plotConfig = this.getMainPlotConfig(allDataRows.main, aesConfig, scaleConfig, yAxisMargin);

        if (!noplot) {
            // set the scale type to linear or log, validating that we don't allow log with negative values
            var xScaleType = this.setScaleType(plotConfig.scales.x, 'x', properties);
            var yScaleType = this.setScaleType(plotConfig.scales.yLeft, 'y', properties);

            this.clickTask = new Ext.util.DelayedTask(function(node, view, name, target, multi) {
                if (layerScope.isBrushed) {
                    this.clearAllBrushing.call(view, layerScope);
                }
                this.runXAxisSelectAnimation(node, view, name, target, multi);
            }, this);

            if (Ext.isFunction(this.highlightSelectedFn)) {
                this.highlightSelectedFn();
            }

            // configure gutters
            if (this.requireXGutter) {
                gutterXPlotConfig = this.generateXGutter(plotConfig, chartData, allDataRows, yAxisMargin, properties, layerScope);
                Ext.apply(gutterXPlotConfig.scales.xTop, {trans : xScaleType});
                Ext.apply(gutterXPlotConfig.scales.xTop, {domain : chartData.getXDomain(studyAxisInfo)});
            }

            if (this.requireYGutter) {
                gutterYPlotConfig = this.generateYGutter(plotConfig, chartData, allDataRows, properties, layerScope);
                Ext.apply(gutterYPlotConfig.scales.yRight, {trans : yScaleType});
            }
        }

        if (!noplot && this.requireYGutter) {

            // render the gutter
            if (!this.renderGutter(this.yGutterName, gutterYPlotConfig, layerScope)) {
                return;
            }

            // If using color variables sync color and shape with yGutter plot if it exists
            if (this.measures[2]) {
                var plotLayer = this.yGutterPlot.layers[0];
                if (Ext.isDefined(plotLayer.geom.colorScale)) {
                    plotConfig.scales.color = plotLayer.geom.colorScale;
                }
                if (Ext.isDefined(plotLayer.geom.shapeScale)) {
                    plotConfig.scales.shape = plotLayer.geom.shapeScale;
                }
            }
        }

        this.plot = new LABKEY.vis.Plot(plotConfig);

        layerScope.plot = this.plot; // hoisted for mouseover/mouseout event listeners

        if (this.plot) {
            this.plot.addLayer(this.getPlotLayer(noplot, properties, layerScope));
            try {
                this.hidePlotMsg();
                this.plot.render();
                if (!noplot && this.measures[2]) {
                    this.getColorSelector().setLegend(this.plot.getLegendData());
                }
            }
            catch(err) {
                this.showMessage(err.message, true);
                this.fireEvent('hideload', this);
                this.plot = null;
                this.plotEl.update('');
                this.noPlot(false);
                console.error(err);
                console.error(err.stack);
                return;
            }
        }

        if (!noplot && this.requireXGutter) {

            // If using color variables sync color and shape with yGutter plot if it exists
            if (this.measures[2] && this.plot) {
                var plotLayer = this.plot.layers[0];
                if (Ext.isDefined(plotLayer.geom.colorScale)) {
                    gutterXPlotConfig.scales.color = plotLayer.geom.colorScale;
                }
                if (Ext.isDefined(plotLayer.geom.shapeScale)) {
                    gutterXPlotConfig.scales.shape = plotLayer.geom.shapeScale;
                }
            }

            // render the gutter
            if (!this.renderGutter(this.xGutterName, gutterXPlotConfig, layerScope)) {
                return;
            }
        }

        this.clearAllBrushing = function() {
            this.plot.clearBrush();
            if (this.xGutterPlot)
                this.xGutterPlot.clearBrush();
            if (this.yGutterPlot)
                this.yGutterPlot.clearBrush();

            layerScope.isBrushed = false;
            this.clearHighlightedData();
            this.highlightSelected();
        };

        this.isBrushed = function() {
            return layerScope.isBrushed;
        };

        if (!noplot) {
            this.plot.setBrushing(this.bindBrushing(layerScope, properties, 'main', this.xGutterPlot, this.yGutterPlot));
            if (this.xGutterPlot) {
                this.xGutterPlot.setBrushing(this.bindBrushing(layerScope, properties, 'xTop', this.xGutterPlot, this.yGutterPlot));
            }
            if (this.yGutterPlot) {
                this.yGutterPlot.setBrushing(this.bindBrushing(layerScope, properties, 'yRight', this.xGutterPlot, this.yGutterPlot));
            }
        }

        if (Ext.isDefined(studyAxisInfo)) {
            this.initStudyAxis(studyAxisInfo);
        }

        this.handleDensePlotBrushEvent();

        this.fireEvent('hideload', this);
    },

    handleDensePlotBrushEvent : function() {
        var selector;

        // Allow brushing in dense plot by creating and passing a new click event to the brush layer
        if (Ext.isDefined(this.plot.renderer)) {
            selector = this.showPointsAsBin ? '.vis-bin' : '.point';
            this.plot.renderer.canvas.selectAll(selector).on('mousedown', function() {
                var brushNode = d3.select(this.parentElement.parentElement.getElementsByClassName('brush')[0]).node();
                var newClickEvent = new CustomEvent('mousedown');
                newClickEvent.pageX = d3.event.pageX;
                newClickEvent.clientX = d3.event.clientX;
                newClickEvent.pageY = d3.event.pageY;
                newClickEvent.clientY = d3.event.clientY;
                brushNode.dispatchEvent(newClickEvent);
            });
        }
    },

    bindBrushing : function(layerScope, properties, dimension, xGutterPlot, yGutterPlot) {
        var onBrush = this.showPointsAsBin ? ChartUtils.brushBins : ChartUtils.brushPoints,
            brushFn,
            brushingExtent,
            _dimension = dimension;

        // determine brush handling based on dimension
        if (dimension === 'main') {
            _dimension = !properties.xaxis.isDimension && properties.xaxis.isContinuous ? 'both' : 'y';
            brushFn = function(event, layerData, extent, plot) {
                if (this.initiatedBrushing != 'main') {
                    return;
                }

                brushingExtent = Ext.clone(extent);

                onBrush.call(this, event, layerData, extent, plot);

                var xIsNull = extent[0][0] === null && extent[1][0] === null,
                    yIsNull = extent[0][1] === null && extent[1][1] === null;

                if (yIsNull && !xIsNull && xGutterPlot) {
                    xGutterPlot.setBrushExtent(brushingExtent);
                    if (yGutterPlot)
                        yGutterPlot.clearBrush();
                }
                else if (xIsNull && !yIsNull && yGutterPlot) {
                    yGutterPlot.setBrushExtent(brushingExtent);
                    if (xGutterPlot)
                        xGutterPlot.clearBrush();
                }
                else if (!xIsNull && !yIsNull) {
                    if (xGutterPlot)
                        xGutterPlot.clearBrush();
                    if (yGutterPlot)
                        yGutterPlot.clearBrush();
                }
            };
        }
        else {
            // gutter brushing
            brushFn = function(event, layerData, extent, plot) {
                if (this.initiatedBrushing != dimension)
                    return;

                brushingExtent = Ext.clone(extent);

                onBrush.call(this, event, layerData, extent, plot);
                this.plot.setBrushExtent(brushingExtent);

                if ((dimension == 'xTop' || dimension == 'x') && yGutterPlot) {
                    yGutterPlot.clearBrush();
                }
                if ((dimension == 'yRight' || dimension == 'y') && xGutterPlot) {
                    xGutterPlot.clearBrush();
                }
            };
        }

        return {
            fillColor: ChartUtils.colors.SELECTEDBG,
            strokeColor: ChartUtils.colors.SELECTED,
            dimension: _dimension,
            brushstart: Ext.bind(ChartUtils.brushStart, this, [layerScope, dimension]),
            brush: Ext.bind(brushFn, this),
            brushend: Ext.bind(ChartUtils.brushEnd, this, [this.measures, properties, dimension], true),
            brushclear: Ext.bind(ChartUtils.brushClear, this, [layerScope, dimension])
        };
    },

    renderGutter : function(plotName, gutterPlotConfig, layerScope) {

        var success = true;
        var gutterPlot = new LABKEY.vis.Plot(gutterPlotConfig);

        layerScope[plotName] = gutterPlot;

        if (gutterPlot) {
            if (this.showPointsAsBin) {
                gutterPlot.addLayer(this.getBinLayer(layerScope, true, plotName));
            }
            else {
                gutterPlot.addLayer(this.getPointLayer(layerScope, true, plotName));
            }

            try {
                gutterPlot.render();
            }
            catch(err) {
                this.showMessage(err.message, true);
                this.fireEvent('hideload', this);
                this[plotName] = null;
                console.error(err);
                console.error(err.stack);
                success = false;
            }
        }
        else {
            success = false;
        }

        this[plotName] = gutterPlot;

        return success;
    },

    generateXGutter : function(plotConfig, chartData, allDataRows, yAxisMargin) {
        var gutterXMargins = {
            top: 0,
            left: this.requireYGutter ? this.yGutterWidth + yAxisMargin : yAxisMargin + 24,
            right: plotConfig.margins.right,
            bottom: 0
        };

        var me = this;
        var gutterXLabels = {
            y: {
                value: 'Undefined y value',
                fontSize: 11,
                position: 8,
                rotate: 0,
                maxCharPerLine: 9,
                lineWrapAlign: 'end',
                cls: 'xGutter-label',
                bkgdColor: ChartUtils.colors.GRIDBKGD,
                bkgdWidth: 70,
                listeners: {
                    mouseover: function() {
                        me._showWhyXGutter(chartData.getDataRows());
                    },
                    mouseout: function() {
                        me._closeWhyGutter();
                    }
                }
            }
        };
        var gutterXWidth = plotConfig.width + (this.requireYGutter ? this.yGutterWidth : 0);

        var gutterXAes = {
            xTop: function(row) {return row.x;},
            y: function(row) {return row.y;}
        };
        var gutterXScales = {
            xTop: {
                scaleType: 'continuous',
                tickFormat: ChartUtils.tickFormat.empty
            },
            yLeft: {
                scaleType: 'discrete',
                tickFormat: ChartUtils.tickFormat.empty
            }
        };

        return Ext.apply(this.getGutterPlotConfig(gutterXMargins, this.xGutterHeight, gutterXWidth, allDataRows.undefinedY, gutterXAes, gutterXScales, gutterXLabels),
                {gridLinesVisible: 'y'});
    },

    generateYGutter : function(plotConfig, chartData, allDataRows) {
        var gutterYMargins = {
            top: plotConfig.margins.top,
            left: 24,
            right: 0,
            bottom: plotConfig.margins.bottom
        };

        var me = this;
        var gutterYLabels = {
            x: {
                value: 'Undefined x value',
                fontSize: 11,
                position: 45,
                cls: 'yGutter-label',
                maxCharPerLine: 10,
                lineWrapAlign: 'start',
                bkgdColor: ChartUtils.colors.GRIDBKGD,
                bkgdHeight: 100,
                bkgdWidth: this.yGutterWidth - 15,
                listeners: {
                    mouseover: function() {
                        me._showWhyYGutter(chartData.getDataRows());
                    },
                    mouseout: function() {
                        me._closeWhyGutter();
                    }
                }
            }
        };

        var gutterYAes = {
            x: function(row) {return row.x;},
            yRight: function(row) {return row.y;}
        };

        var gutterYScales = {
            x: {
                scaleType: 'discrete',
                tickFormat: ChartUtils.tickFormat.empty
            },
            yRight: {
                scaleType: 'continuous',
                domain: chartData.getYDomain(),
                tickFormat: ChartUtils.tickFormat.empty
            }
        };

        return Ext.apply(this.getGutterPlotConfig(gutterYMargins, plotConfig.height, this.yGutterWidth, allDataRows.undefinedX, gutterYAes, gutterYScales, gutterYLabels),
                {gridLinesVisible: 'x'});

    },

    logRowCount : function(allDataRows) {
        if (LABKEY.devMode) {
            console.log('total plotted rows:', allDataRows.totalCount);
            if (allDataRows && allDataRows.undefinedX) {
                console.log('plotted x gutter rows:', allDataRows.undefinedX.length);
            }
            if (allDataRows && allDataRows.undefinedY) {
                console.log('plotted x gutter rows:', allDataRows.undefinedY.length);
            }
        }
    },

    xAxisClick : function(e, selection, target, index, y, layerScope) {
        // selectionInProgress keeps label highlighted while selection created
        this.selectionInProgress = target;

        var multi = e.ctrlKey||e.shiftKey||(e.metaKey),
            nodes,
            node = null;

        // node is needed for animation
        if (layerScope.plot.renderer)
        {
            nodes = layerScope.plot.renderer.canvas.selectAll('.tick-text text');
            nodes[0].forEach(function(n)
            {
                if (n.innerHTML === target)
                    node = n;
            });

            if (node)
                this.clickTask.delay(150, null, null, [node, this, this.measures[0].alias, target, multi]);
            else
                this.clickTask.delay(150, null, null, [e.target, this, this.measures[0].alias, target, multi]);

            this.showMessage('Hold Shift, CTRL, or CMD to select multiple');
        }
    },

    xAxisMouseOut : function(target, index, y, layerScope) {
        // Do not do mouse over/out for selected labels or labels in process of selection
        if (!layerScope.isBrushed && !this.isSelection(target) && this.selectionInProgress != target) {
            // Clear plot highlights
            this.clearHighlightedData();
            this.highlightSelected();

            // Clear label highlighting
            var targets = [];
            targets.push(target);
            this.highlightLabels.call(this, this.plot, targets, this.labelTextColor, this.labelBkgdColor, false);
        }
    },

    xAxisMouseOver : function(target, index, y, layerScope) {
        // Do not do mouse over/out for selected labels or labels in process of selection
        if (!layerScope.isBrushed && !this.isSelection(target) && this.selectionInProgress != target) {
            this.highlightPlotData(target);

            // Highlight label
            var targets = [];
            targets.push(target);
            this.highlightLabels.call(this, this.plot, targets, this.labelTextColor, this.labelBkgdHltColor, false);
        }
    },

    retrieveBinSubjectIds : function(plot, target, subjects) {
        var subjectIds = [];
        if (subjects) {
            subjects.forEach(function(s) {
                subjectIds.push(s);
            });
        }

        if (plot.renderer)
        {
            var bins = plot.renderer.canvas.selectAll('.vis-bin path');
            var selections = this.getCategoricalSelectionValues();

            bins.each(function(d)
            {
                // Check if value matches target or another selection
                for (var i = 0; i < d.length; i++)
                {
                    var data = d[i].data;
                    if (data.x === target && subjectIds.indexOf(data.subjectId) === -1)
                    {
                        subjectIds.push(data.subjectId);
                    }
                    else if (selections.indexOf(data.x) != -1 && subjectIds.indexOf(data.subjectId) === -1)
                    {
                        subjectIds.push(data.subjectId);
                    }
                }
            });
        }

        return subjectIds;
    },

    highlightBins : function(target, subjects) {
        // get the set of subjectIds in the binData
        var subjectIds = this.retrieveBinSubjectIds(this.plot, target, subjects);
        if (subjects) {
            subjects.forEach(function(s) {
                subjectIds.push(s);
            });
        }

        if (this.plot.renderer) {
            var isSubjectInMouseBin = function(d, yesVal, noVal) {
                if (d.length > 0 && d[0].data) {
                    for (var i = 0; i < d.length; i++) {
                        if (subjectIds.indexOf(d[i].data.subjectId) != -1) {
                            return yesVal;
                        }
                    }
                }

                return noVal;
            };

            var colorFn = function(d)
            {
                // keep original color of the bin (note: uses style instead of fill attribute)
                d.origStyle = d.origStyle || this.getAttribute('style');

                return isSubjectInMouseBin(d, 'fill: ' + ChartUtils.colors.SELECTED, d.origStyle);
            };

            var opacityFn = function(d)
            {
                return isSubjectInMouseBin(d, 1, 0.15);
            };

            this.highlightBinsByCanvas(this.plot.renderer.canvas, colorFn, opacityFn);

            if (this.requireXGutter && Ext.isDefined(this.xGutterPlot)) {
                this.highlightBinsByCanvas(this.xGutterPlot.renderer.canvas, colorFn, opacityFn);
            }

            if (this.requireYGutter && Ext.isDefined(this.yGutterPlot)) {
                this.highlightBinsByCanvas(this.yGutterPlot.renderer.canvas, colorFn, opacityFn);
            }
        }
    },

    highlightBinsByCanvas : function(canvas, colorFn, opacityFn) {
        canvas.selectAll('.vis-bin path').attr('style', colorFn)
            .attr('fill-opacity', opacityFn)
            .attr('stroke-opacity', opacityFn);
    },

    clearHighlightBins : function() {
        if (this.plot.renderer) {
            this.clearBinsByCanvas(this.plot.renderer.canvas);

            if (this.requireXGutter && Ext.isDefined(this.xGutterPlot)) {
                this.clearBinsByCanvas(this.xGutterPlot.renderer.canvas);
            }

            if (this.requireYGutter && Ext.isDefined(this.yGutterPlot)) {
                this.clearBinsByCanvas(this.yGutterPlot.renderer.canvas);
            }
        }
    },

    clearBinsByCanvas : function(canvas) {
        canvas.selectAll('.vis-bin path')
                .attr('style', function(d) {return d.origStyle || this.getAttribute('style');})
                .attr('fill-opacity', 1)
                .attr('stroke-opacity', 1);
    },

    clearHighlightedData : function() {
        if (this.showPointsAsBin)
            this.clearHighlightBins();
        else
            this.clearHighlightPoints();
    },

    retrievePointSubjectIds : function(target, subjects) {
        var subjectIds = [];
        if (subjects) {
            subjects.forEach(function(s) {
                subjectIds.push(s);
            });
        }

        if (this.plot.renderer) {
            var points = this.plot.renderer.canvas.selectAll('.point path'),
                selections = this.getCategoricalSelectionValues(),
                subject;

            points.each(function(d) {
                subject = d.subjectId;

                // Check if value matches target or another selection
                if (subjectIds.indexOf(subject) === -1) {
                    if (d.x == target) {
                        subjectIds.push(subject);
                    }
                    else if (selections.indexOf(d.x) != -1) {
                        subjectIds.push(subject);
                    }
                }
            });
        }

        return subjectIds;
    },

    highlightPlotData : function(target, subjects) {
        if (this.showPointsAsBin) {
            this.highlightBins(target, subjects);
        }
        else {
            this.highlightPoints(target, subjects);
        }
    },

    highlightPoints : function(target, subjects) {
        var subjectIds = this.retrievePointSubjectIds(target, subjects);

        var fillColorFn = function(d) {
            if (subjectIds.indexOf(d.subjectId) != -1) {
                return ChartUtils.colors.SELECTED;
            }
            return ChartUtils.colors.UNSELECTED;
        };

        if (this.plot.renderer) {
            this.highlightPointsByCanvas(this.plot.renderer.canvas, fillColorFn);

            if (this.requireXGutter && Ext.isDefined(this.xGutterPlot)) {
                this.highlightPointsByCanvas(this.xGutterPlot.renderer.canvas, fillColorFn);
            }

            if (this.requireYGutter && Ext.isDefined(this.yGutterPlot)) {
                this.highlightPointsByCanvas(this.yGutterPlot.renderer.canvas, fillColorFn);
            }
        }
    },

    highlightPointsByCanvas : function(canvas, fillColorFn) {
        canvas.selectAll('.point path')
            .attr('fill', fillColorFn).attr('fill-opacity', 1)
            .attr('stroke', fillColorFn).attr('stroke-opacity', 1);

        // Re-append the node so it is on top of all the other nodes, this way highlighted points are always visible. (issue 24076)
        canvas.selectAll('.point path[fill="' + ChartUtils.colors.SELECTED + '"]').each(function() {
            var node = this.parentNode;
            node.parentNode.appendChild(node);
        });
    },

    clearHighlightColorFn : function() {
        var colorScale = null, colorAcc = null;

        if (this.plot.scales.color && this.plot.scales.color.scale) {
            colorScale = this.plot.scales.color.scale;
            colorAcc = this.plot.aes.color;
        }

        return function(d) {
            if (colorScale && colorAcc) {
                return colorScale(colorAcc.getValue(d));
            }

            return ChartUtils.colors.BLACK;
        };
    },

    clearHighlightPoints : function() {
        if (this.plot.renderer) {
            this.clearPointsByCanvas(this.plot.renderer.canvas, this.clearHighlightColorFn());

            if (this.requireXGutter && Ext.isDefined(this.xGutterPlot)) {
                this.clearPointsByCanvas(this.xGutterPlot.renderer.canvas, this.clearHighlightColorFn());
            }

            if (this.requireYGutter && Ext.isDefined(this.yGutterPlot)) {
                this.clearPointsByCanvas(this.yGutterPlot.renderer.canvas, this.clearHighlightColorFn());
            }
        }
    },

    clearPointsByCanvas : function(canvas, colorFn) {
        canvas.selectAll('.point path')
                .attr('fill', colorFn)
                .attr('stroke', colorFn)
                .attr('fill-opacity', 0.5)
                .attr('stroke-opacity', 0.5);
    },

    highlightSelected : function() {
        var targets = this.getCategoricalSelectionValues(), me = this;
        if (targets.length < 1) {
            me.clearHighlightedData();
        }

        targets.forEach(function(t) {
            me.highlightPlotData(t);
        })
    },

    getCategoricalSelectionValues : function() {
        var selections = Connector.getState().getSelections();
        var values = [];
        selections.forEach(function(s) {
            var gridData = s.get('gridFilter');
            for (var i = 0; i < gridData.length; i++) {
                if (gridData[i] != null && Ext.isString(gridData[i].getValue())) {
                    values = gridData[i].getValue().split(';');
                    break;
                }
            }
        });

        // issue 24244: special handling for 'undefined' categorical selection
        for (var i = 0; i < values.length; i++) {
            if (values[i] == '') {
                values[i] = ChartUtils.emptyTxt;
            }
        }

        return values;
    },

    isSelection : function(target) {
        var values = this.getCategoricalSelectionValues(),
            found = false;

        values.forEach(function(t) {
            if (t === target) {
                found = true;
            }
        });

        return found;
    },

    clearHighlightLabels : function(plot) {
        var me = this;
        var tickFillFn = function(t) {
            return me.labelBkgdColor;
        };

        var labelFillFn = function(t) {
            return me.labelTextColor;
        };

        if (plot.renderer) {
            plot.renderer.canvas.selectAll('.tick-text rect.highlight').attr('fill', tickFillFn);
            plot.renderer.canvas.selectAll('.tick-text text').attr('fill', labelFillFn);
        }
    },

    highlightLabels : function(plot, targets, textColor, bgColor, clearOthers) {

        var me = this;

        if (targets.length < 1) {
            this.clearHighlightLabels(plot);
        }

        targets.forEach(function(target) {
            if (plot.renderer) {
                var tickFillOpacityFn = function(t) {
                    if (target === t || me.isSelection(t))
                        return 1;
                    return 0;
                };

                var tickFillFn = function(t) {
                    if (target === t)
                        return bgColor;

                    if (clearOthers && targets.indexOf(t) === -1)
                        return me.labelBkgdColor;

                    return this.getAttribute('fill');
                };

                var labelFillFn = function(t) {
                    if (target === t)
                        return textColor;

                    if (clearOthers && targets.indexOf(t) === -1)
                        return me.labelTextColor;

                    return this.getAttribute('fill');
                };

                var ticks = plot.renderer.canvas.selectAll('.tick-text rect.highlight');
                ticks.attr('fill', tickFillFn);
                ticks.attr('fill-opacity', tickFillOpacityFn);

                var label = plot.renderer.canvas.selectAll('.tick-text text');
                label.attr('fill', labelFillFn);
            }
        });
    },

    runXAxisSelectAnimation : function(node, view, name, target, multi) {

        this.allowHover = false;

        Animation.floatTo(node, '', ['.selectionpanel', '.filterpanel'], 'span', 'selected', function(node, view, name, target, multi) {
            this.allowHover = true;
            this.afterSelectionAnimation(node, view, name, target, multi);
        }, this, [node, view, name, target, multi]);
    },

    /**
     * @param {Array} sqlFilters
     * @param {Boolean} [allowInverseFilter=false]
     */
    createSelectionFilter : function(sqlFilters, allowInverseFilter) {
        var xMeasure = this.measures[0], yMeasure = this.measures[1];
        var wrapped = [ this._getAxisWrappedMeasure(xMeasure), this._getAxisWrappedMeasure(yMeasure) ];

        // TODO: Categorical filters need to only include their measures. This means modify wrapped
        var filter = Ext.create('Connector.model.Filter', {
            gridFilter: sqlFilters,
            plotMeasures: wrapped,
            isPlot: true,
            isGrid: true,
            operator: LABKEY.app.model.Filter.OperatorTypes.OR,
            filterSource: 'GETDATA',
            isWhereFilter: true,
            showInverseFilter: allowInverseFilter === true
        });

        Connector.getState().addSelection(filter, true, false, true);
    },

    afterSelectionAnimation : function(node, view, name, target, multi) {
        var sqlFilters = [null, null, null, null],
            selections = Connector.getState().getSelections(),
            type = LABKEY.Filter.Types.EQUAL,
            allowInverseFilter = true, values = '', data;

        if (multi) {
            for (var i=0; i < selections.length; i++) {
                data = selections[i].get('gridFilter')[0];
                if (data.getColumnName() === name) {
                    values = values.concat(data.getValue()).concat(';');
                }
            }
        }

        // issue 24244: filtering for emptyTxt category needs to apply a different filter
        values = values.concat(target == ChartUtils.emptyTxt ? '' : target);

        if (multi && selections.length > 0) {
            type = LABKEY.Filter.Types.EQUALS_ONE_OF;
        }
        else if (target == ChartUtils.emptyTxt) {
            type = LABKEY.Filter.Types.ISBLANK;
            allowInverseFilter = false;
        }

        sqlFilters[0] = LABKEY.Filter.create(name, values, type);

        this.createSelectionFilter(sqlFilters, allowInverseFilter);
        this.selectionInProgress = null;
        this.highlightLabels(this.plot, this.getCategoricalSelectionValues(), this.labelTextHltColor, this.labelBkgdHltColor, false);

    },

    getScale : function(axis) {
        var scale = 'linear';

        if (axis == 'y' && this.activeYSelection) {
            scale = this.getSelectedOptionScale(this.activeYSelection);
        }
        else if (axis == 'x' && this.activeXSelection) {
            scale = this.getSelectedOptionScale(this.activeXSelection);
        }

        return scale.toLowerCase();
    },

    getSelectedOptionScale : function(selected) {
        return (selected.options && selected.options.scale) ? selected.options.scale : selected.defaultScale;
    },

    setScaleType : function(scale, axis, properties) {
        var scaleType = this.getScale(axis), allowLog;

        if (scale.scaleType !== 'discrete') {
            allowLog = (axis == 'y') ? !properties.setYLinear : !properties.setXLinear;
            if (!allowLog && scaleType == 'log') {
                this.showMessage('Displaying the ' + axis.toLowerCase() + '-axis on a linear scale due to the presence of invalid log values.', true);
                scaleType = 'linear';
            }

            Ext.apply(scale, {trans : scaleType});
        }

        return scaleType;
    },

    setActiveMeasureSelectionFromFilter : function(filter, activeMeasures) {
        var plotMeasures = filter.get('plotMeasures'),
            x = plotMeasures[0], y = plotMeasures[1], color = plotMeasures[2];

        if (x) {
            activeMeasures.x = x.measure;

            this.activeXSelection = activeMeasures.x;
            if (this.initialized) {
                this.getXAxisSelector().setActiveMeasure(this.activeXSelection);
            }
        }

        if (y) {
            activeMeasures.y = y.measure;

            this.activeYSelection = activeMeasures.y;
            if (this.initialized) {
                this.getYAxisSelector().setActiveMeasure(this.activeYSelection);
            }
        }

        if (color) {
            activeMeasures.color = color.measure;

            this.activeColorSelection = activeMeasures.color;
            if (this.initialized) {
                this.getColorAxisSelector().setActiveMeasure(this.activeColorSelection);
            }
        }
    },

    getActiveMeasures : function() {
        this.fromFilter = false;
        var measures = {
            x: null,
            y: null,
            color: null
        };

        // set the measures based on the active filter (i.e. "In the plot" filter)
        if (!Ext.isDefined(this.activeXSelection) && !Ext.isDefined(this.activeYSelection) && !Ext.isDefined(this.activeColorSelection)) {
            Ext.each(Connector.getState().getFilters(), function(filter) {
                if (filter.isPlot() && !filter.isGrid()) {
                    this.setActiveMeasureSelectionFromFilter(filter, measures);
                    this.fromFilter = true;

                    // return false to break from this Ext.each
                    return false;
                }
            }, this);
        }
        // otherwise use the active measure selections
        else {
            if (this.activeXSelection) {
                measures.x = this.activeXSelection;

                // special case to look for userGroups as a variable option to use as filter values for the x measure
                if (Ext.isObject(measures.x.options) && measures.x.options.userGroups) {
                    measures.x.values = measures.x.options.userGroups;
                }
            }
            if (this.activeYSelection) {
                measures.y = this.activeYSelection;
            }
            if (this.activeColorSelection) {
                measures.color = this.activeColorSelection;
            }
        }

        return measures;
    },

    clearPlotSelections : function() {
        this.clearAxisSelection('y');
        this.clearAxisSelection('x');
        this.clearAxisSelection('color');
    },

    clearAxisSelection : function(axis) {
        if (axis == 'y') {
            this.getYAxisSelector().clearSelection();
            this.activeYSelection = undefined;
            this.getYSelector().clearModel();
        }
        else if (axis == 'x') {
            this.getXAxisSelector().clearSelection();
            this.activeXSelection = undefined;
            this.getXSelector().clearModel();
        }
        else if (axis == 'color') {
            this.getColorAxisSelector().clearSelection();
            this.activeColorSelection = undefined;
            this.getColorSelector().clearModel();
        }
    },

    onShowGraph : function() {

        if (this.isHidden()) {
            this.refreshRequired = true;
        }
        else {
            this.refreshRequired = false;
            this.requireStudyAxis = false;

            if (this.filterClear) {
                this.clearPlotSelections();
            }

            var activeMeasures = this.getActiveMeasures();

            // update variable selectors
            // TODO: Stop doing this every time, only do it when the measure has changed (complex?)
            this.getYSelector().getModel().updateVariable([activeMeasures.y]);
            this.getXSelector().getModel().updateVariable([activeMeasures.x]);
            this.getColorSelector().getModel().updateVariable([activeMeasures.color]);

            if (activeMeasures.y == null) {
                this.hideMessage();
                this.getStudyAxisPanel().setVisible(false);
                Connector.getState().clearSelections(true);
                this.filterClear = false;
                this.noPlot(false);
            }
            else {
                this.measures = [activeMeasures.x, activeMeasures.y, activeMeasures.color];

                this.fireEvent('showload', this);

                this.requireStudyAxis = activeMeasures.x !== null && activeMeasures.x.variableType === "TIME";

                // TODO: Refactor this
                // TODO: We only want to update the 'In the plot' filter when any of the (x, y, color) measure configurations change
                // TODO: This is what is causing our filter undo to fail because it causes the state to update twice
                if (!this.fromFilter && activeMeasures.y !== null) {
                    this.updatePlotBasedFilter(activeMeasures);
                }
                else {
                    this.initialized = true;
                }

                this.requestChartData(activeMeasures);
            }
        }
    },

    getWrappedMeasures : function(activeMeasures) {

        var wrappedMeasures = [
            this._getAxisWrappedMeasure(activeMeasures.x),
            this._getAxisWrappedMeasure(activeMeasures.y)
        ];
        wrappedMeasures.push(activeMeasures.color ? {measure : activeMeasures.color, time: 'date'} : null);

        return wrappedMeasures;
    },

    _getAxisWrappedMeasure : function(measure) {
        if (!measure) {
            return null;
        }

        var wrappedMeasure = {measure : measure},
            options = measure.options;

        if (measure.variableType == 'TIME') {
            var interval = measure.alias;
            measure.interval = interval;
            wrappedMeasure.dateOptions = {
                interval: interval,
                altQueryName: 'cds.VisitTagAlignment'
            };

            // handle visit tag alignment for study axis
            if (options && options.alignmentVisitTag !== undefined) {
                wrappedMeasure.dateOptions.zeroDayVisitTag = options.alignmentVisitTag;
            }
        }
        else if (this.requireStudyAxis) {
            // Issue 24002: Gutter plot for null y-values and study axis are appearing at the same time
            wrappedMeasure.filterArray = [LABKEY.Filter.create(measure.name, null, LABKEY.Filter.Types.NOT_MISSING)];
        }

        // we still respect the value if it is set explicitly on the measure
        if (!Ext.isDefined(wrappedMeasure.measure.inNotNullSet)) {
            wrappedMeasure.measure.inNotNullSet = Connector.model.ChartData.isContinuousMeasure(measure);
        }

        return wrappedMeasure;
    },

    /**
     *
     * @param activeMeasures Set of active measures (x, y, color)
     * @param {boolean} [includeFilterMeasures=false] Include all measures declared in all state filters
     * @returns {{measures: (*|Array), wrapped: *}}
     */
    getMeasureSet : function(activeMeasures, includeFilterMeasures) {

        var additionalMeasures = this.getAdditionalMeasures(activeMeasures),
            wrappedMeasures = this.getWrappedMeasures(activeMeasures),
            queryService = Connector.getService('Query'),
            nonNullMeasures = [],
            filterMeasures,
            measures, i;

        for (i=0; i < wrappedMeasures.length; i++) {
            if (wrappedMeasures[i]) {
                nonNullMeasures.push(wrappedMeasures[i]);
            }
        }

        measures = additionalMeasures.concat(nonNullMeasures);

        // set of measures from data filters
        if (includeFilterMeasures === true) {
            filterMeasures = queryService.getWhereFilterMeasures(Connector.getState().getFilters(), true, this.getQueryKeys(measures));
            if (!Ext.isEmpty(filterMeasures)) {
                measures = measures.concat(filterMeasures);
            }
        }

        return {
            measures: queryService.mergeMeasures(measures),
            wrapped: wrappedMeasures
        };
    },

    getQueryKeys : function(measures) {
        var queryKeys = [], key;

        Ext.each(measures, function(m){
            key = m.measure.schemaName + '|' + m.measure.queryName;
            if (queryKeys.indexOf(key) == -1) {
                queryKeys.push(key);
            }
        });

        return queryKeys;
    },

    /**
     * This creates a temp query via cdsGetData which is then used to query for unique participants, and is also what
     * we use to back the chart data (via an AxisMeasureStore).
     * @param activeMeasures
     */
    requestChartData : function(activeMeasures) {
        ChartUtils.getSubjectsIn(function(subjectList) {
            // issue 23885: Do not include the color measure in request if it's noe from the x, y, or demographic datasets
            if (activeMeasures.color) {
                var demographicSource = activeMeasures.color.isDemographic,
                    matchXSource = activeMeasures.x && activeMeasures.x.queryName == activeMeasures.color.queryName,
                    matchYSource = activeMeasures.y && activeMeasures.y.queryName == activeMeasures.color.queryName;

                if (!demographicSource && !matchXSource && !matchYSource) {
                    activeMeasures.color = null;
                }
            }

            var measures = this.getMeasureSet(activeMeasures, true /* includeFilterMeasures */).measures;

            this.applyFiltersToMeasure(measures, subjectList);

            // Request Chart MeasureStore Data
            Connector.getService('Query').getMeasureStore(measures, this.onChartDataSuccess, this.onFailure, this);
        }, this);
    },

    onChartDataSuccess : function(measureStore, measureSet) {
        var chartData = Ext.create('Connector.model.ChartData', {
            measureSet: measureSet,
            plotMeasures: this.measures,
            measureStore: measureStore,
            plotScales: {x: this.getScale('x'), y: this.getScale('y')}
        });

        this.dataQWP = {
            schema: chartData.getSchemaName(),
            query: chartData.getQueryName()
        };

        this.hasStudyAxisData = false;

        if (this.requireStudyAxis) {
            this.getStudyAxisData(chartData);
        }
        else if (chartData.getDataRows().totalCount == 0) {
            // show empty plot message if we have no data in main plot or gutter plots
            this.noPlot(true);
        }
        else {
            this.initPlot(chartData);
        }
    },

    _showWhyXGutter : function(data) {
        var percent = Ext.util.Format.round((data.undefinedY.length / data.totalCount) * 100, 2),
            config = {
                bubbleWidth: 325,
                target: document.querySelector("svg g text.xGutter-label"),
                placement: 'top',
                title: 'Percent with undefined y value: ' + percent + '%',
                content: 'Data points may have no matching y value due to differing subject, visit, assay, antigen, analyte, and other factors. See Help for more details',
                xOffset: -20
            };

        ChartUtils.showCallout(config, 'hideguttermsg', this);
    },

    _showWhyYGutter : function(data) {
        var percent = Ext.util.Format.round((data.undefinedX.length / data.totalCount) * 100, 2),
            config = {
                bubbleWidth: 325,
                target: document.querySelector("svg g text.yGutter-label"),
                placement: 'right',
                title: 'Percent with undefined x value: ' + percent + '%',
                content: 'Data points may have no matching x value due to differing subject, visit, assay, antigen, analyte, and other factors. See Help for more details',
                yOffset: -40,
                arrowOffset: 30
            };

        ChartUtils.showCallout(config, 'hideguttermsg', this);
    },

    _closeWhyGutter : function() {
        this.fireEvent('hideguttermsg', this);
    },

    toggleHeatmapMode : function() {
        this.getColorSelector().setDisabled(this.showPointsAsBin);
        this.getHeatmapModeIndicator().setVisible(this.showPointsAsBin);

        // Show binning message for a few seconds if first time user hits it
        var msgKey = 'HEATMAP_MODE';
        if (!this.disableAutoMsg && this.showPointsAsBin && Connector.getService('Messaging').isAllowed(msgKey)) {
            this.showWhyBinning();
            this.hideHeatmapModeTask.delay(5000);
            Connector.getService('Messaging').block(msgKey);
        }
    },

    showWhyBinning : function() {
        if (this.showPointsAsBin) {
            var config = {
                target: this.getHeatmapModeIndicator().getEl().dom,
                placement: 'bottom',
                title: 'Heatmap on',
                xOffset: -115,
                arrowOffset: 145,
                content: 'There are too many dots to show interactively. Higher data density is represented by darker'
                + ' tones. Color variables are disabled. Reduce the amount of data plotted to see dots again.'
            };

            ChartUtils.showCallout(config, 'hideheatmapmsg', this);
        }
    },

    toggleMedianMode : function() {
        this.getMedianModeIndicator().setVisible(this.showAsMedian);

        // Show median message for a few seconds if first time user hits it
        var msgKey = 'MEDIAN_MODE';
        if (!this.disableAutoMsg && this.showAsMedian && Connector.getService('Messaging').isAllowed(msgKey)) {
            this.showWhyMedian();
            this.hideMedianModeTask.delay(5000);
            Connector.getService('Messaging').block(msgKey);
        }
    },

    showWhyMedian : function() {
        if (this.showAsMedian) {
            var config = {
                target: this.getMedianModeIndicator().getEl().dom,
                placement: 'bottom',
                title: 'Median values',
                xOffset: -105,
                arrowOffset: 145,
                content: 'To enable an x-y plot, each subject now has one dot for its median response value at each visit.'
                + ' To see individual responses, narrow the choices in the X and Y controls.'
            };

            ChartUtils.showCallout(config, 'hidemedianmsg', this);
        }
    },

    getAdditionalMeasures : function(activeMeasures) {
        // map key to schema, query, name, and values
        var measuresMap = {}, additionalMeasuresArr = [];

        Ext.each(['x', 'y'], function(axis) {
            var schema, query;
            if (activeMeasures[axis])
            {
                schema = activeMeasures[axis].schemaName;
                query = activeMeasures[axis].queryName;

                // always add in the Container and SubjectId columns for a selected measure on the X or Y axis
                this.addValuesToMeasureMap(measuresMap, schema, query, 'Container', []);
                this.addValuesToMeasureMap(measuresMap, schema, query, Connector.studyContext.subjectColumn, []);

                // only add the SequenceNum column for selected measures that are not demographic and no time point
                if (!activeMeasures[axis].isDemographic && activeMeasures[axis].variableType != 'TIME') {
                    this.addValuesToMeasureMap(measuresMap, schema, query, 'SequenceNum', []);
                }

                // add selection information from the advanced options panel of the variable selector
                if (activeMeasures[axis].options && activeMeasures[axis].options.dimensions)
                {
                    Ext.iterate(activeMeasures[axis].options.dimensions, function(key, values) {
                        // null or undefined mean "select all" so don't apply a filter
                        if (!Ext.isDefined(values) || values == null) {
                            values = [];
                        }

                        this.addValuesToMeasureMap(measuresMap, schema, query, key, values);
                    }, this);
                }
            }
        }, this);

        Ext.iterate(measuresMap, function(k, m) {
            var measureRecord = Connector.model.Measure.createMeasureRecord(m);
            additionalMeasuresArr.push({ measure: measureRecord });
        });

        return additionalMeasuresArr;
    },

    addValuesToMeasureMap : function(measureMap, schema, query, name, values) {
        var key = schema + "|" + query + "|" + name;

        if (!measureMap[key]) {
            measureMap[key] = { schemaName: schema, queryName: query, name: name, values: [] };
        }

        measureMap[key].values = measureMap[key].values.concat(values);
    },

    /**
     * Update the values within the 'In the plot' filter
     * @param activeMeasures
     */
    updatePlotBasedFilter : function(activeMeasures) {

        var wrapped = this.getMeasureSet(activeMeasures).wrapped;

        this.plotLock = true;

        var state = Connector.getState();
        var filters = state.getFilters();
        var inPlotFilter;

        var sqlFilters = [null, null, null, null];

        // see if filter already exists
        Ext.each(filters, function(filter) {
            if (filter.get('isPlot') === true && filter.get('isGrid') === false) {
                inPlotFilter = filter;
                return false;
            }
        });

        if (inPlotFilter) {
            // update
            inPlotFilter.set('gridFilter', sqlFilters);
            inPlotFilter.set('plotMeasures', wrapped);
            state.updateFilterMembersComplete(false);
        }
        else {
            // create
            inPlotFilter = Ext.create('Connector.model.Filter', {
                gridFilter: sqlFilters,
                isPlot: true,
                isGrid: false,
                hierarchy: 'Subject',
                plotMeasures: wrapped,
                filterSource: 'GETDATA',
                isWhereFilter: false
            });
            state.prependFilter(inPlotFilter);
        }

        this.plotLock = false;

        state.getApplication().fireEvent('plotmeasures');
    },

    noPlot : function(showEmptyMsg) {

        var map = [{
            x : null,
            xname : 'X-Axis',
            y : null,
            yname : 'Y-Axis',
            subjectId: null
        }];

        this.initPlot(map, undefined, true, showEmptyMsg);

        this.getNoPlotMsg().setVisible(!showEmptyMsg);
        this.resizePlotMsg(this.getNoPlotMsg(), this.plotEl.getBox());

        this.getEmptyPlotMsg().setVisible(showEmptyMsg);
        this.resizePlotMsg(this.getEmptyPlotMsg(), this.plotEl.getBox());
    },

    hidePlotMsg : function() {
        this.getNoPlotMsg().hide();
        this.getEmptyPlotMsg().hide();
    },

    onFailure : function(response) {
        console.log(response);
        this.fireEvent('hideload', this);
        this.showMessage('Failed to Load', true);
    },

    updateSelectorWindow : function(win) {
        if (win) {
            var box = this.getBox();
            win.setHeight(box.height-100);
            win.center();
        }
        else {
            console.warn('Failed to updated measure selection');
        }
    },

    setVisibleWindow : function(win) {
        this.visibleWindow = win;
    },

    clearVisibleWindow : function() {
        if (Ext.isObject(this.visibleWindow) && this.visibleWindow.hideLock === true) {
            this.visibleWindow.hideLock = false;
        }
        else {
            this.visibleWindow = undefined;
        }
    },

    // Issue 23585: panel remains even if underlying page changes
    hideVisibleWindow : function() {
        if (Ext.isObject(this.visibleWindow)) {
            this.visibleWindow.hide();
        }
    },

    getYAxisSelector : function() {
        if (!this.yAxisSelector) {
            this.yAxisSelector = Ext.create('Connector.panel.Selector', {
                headerTitle: 'y-axis',
                testCls: 'y-axis-selector',
                activeMeasure: this.activeYSelection,
                sourceMeasureFilter: {
                    queryType: LABKEY.Query.Visualization.Filter.QueryType.DATASETS,
                    includeHidden: this.canShowHidden,
                    includeDefinedMeasureSources: true,
                    measuresOnly: true
                },
                memberCountsFn: ChartUtils.getSubjectsIn,
                memberCountsFnScope: this,
                listeners: {
                    selectionmade: function(selected) {
                        this.clearVisibleWindow();

                        this.activeYSelection = selected;
                        this.variableSelectionMade(this.ywin, this.getYSelector().getEl());
                    },
                    cancel: function() {
                        this.clearVisibleWindow();

                        this.ywin.hide(this.getYSelector().getEl());
                        // reset the selection back to this.activeYSelection
                        this.yAxisSelector.setActiveMeasure(this.activeYSelection);
                    },
                    scope: this
                }
            });
        }

        return this.yAxisSelector;
    },

    showYMeasureSelection : function() {

        if (!this.ywin) {
            this.ywin = this.createSelectorWindow(this.getYAxisSelector());
        }

        this.getYAxisSelector().loadSourceCounts();
        this.ywin.show(this.getYSelector().getEl());
    },

    getXAxisSelector : function() {
        if (!this.xAxisSelector) {
            this.xAxisSelector = Ext.create('Connector.panel.Selector', {
                headerTitle: 'x-axis',
                testCls: 'x-axis-selector',
                activeMeasure: this.activeXSelection,
                sourceMeasureFilter: {
                    queryType: LABKEY.Query.Visualization.Filter.QueryType.DATASETS,
                    includeHidden: this.canShowHidden,
                    includeDefinedMeasureSources: true,
                    includeTimpointMeasures: true
                },
                memberCountsFn: ChartUtils.getSubjectsIn,
                memberCountsFnScope: this,
                listeners: {
                    selectionmade: function(selected) {
                        this.clearVisibleWindow();

                        this.activeXSelection = selected;
                        this.variableSelectionMade(this.xwin, this.getXSelector().getEl());
                    },
                    remove: function() {
                        this.clearVisibleWindow();

                        // Need to remove the x measure (index 0) from the plot filter or we'll pull it down again.
                        this.removeVariableFromFilter(0);
                        this.clearAxisSelection('x');
                        this.variableSelectionMade(this.xwin, this.getXSelector().getEl());
                    },
                    cancel: function() {
                        this.clearVisibleWindow();

                        this.xwin.hide(this.getXSelector().getEl());
                        // reset the selection back to this.activeYSelection
                        this.xAxisSelector.setActiveMeasure(this.activeXSelection);
                    },
                    scope: this
                }
            });
        }

        return this.xAxisSelector;
    },

    showXMeasureSelection : function() {

        if (!this.xwin) {
            this.xwin = this.createSelectorWindow(this.getXAxisSelector());
        }

        this.getXAxisSelector().toggleRemoveVariableButton(Ext.isDefined(this.activeXSelection) && this.activeXSelection != null);
        this.getXAxisSelector().loadSourceCounts();
        this.xwin.show(this.getXSelector().getEl());
    },

    getColorAxisSelector : function() {
        if (!this.colorAxisSelector) {
            this.colorAxisSelector = Ext.create('Connector.panel.Selector', {
                headerTitle: 'color',
                testCls: 'color-axis-selector',
                disableAdvancedOptions: true,
                activeMeasure: this.activeColorSelection,
                sourceMeasureFilter: {
                    queryType: LABKEY.Query.Visualization.Filter.QueryType.DATASETS,
                    includeHidden: this.canShowHidden,
                    includeDefinedMeasureSources: true,
                    userFilter : function(row) {
                        return row.type === 'BOOLEAN' || row.type === 'VARCHAR';
                    }
                },
                memberCountsFn: ChartUtils.getSubjectsIn,
                memberCountsFnScope: this,
                listeners: {
                    selectionmade: function(selected) {
                        this.clearVisibleWindow();

                        this.activeColorSelection = selected;
                        this.variableSelectionMade(this.colorwin, this.getColorSelector().getEl());
                    },
                    remove: function() {
                        this.clearVisibleWindow();

                        // Need to remove the color measure (index 2) from the plot filter or we'll pull it down again.
                        this.removeVariableFromFilter(2);
                        this.clearAxisSelection('color');
                        this.variableSelectionMade(this.colorwin, this.getColorSelector().getEl());
                    },
                    cancel: function() {
                        this.clearVisibleWindow();

                        this.colorwin.hide(this.getColorSelector().getEl());
                        // reset the selection back to this.activeYSelection
                        this.colorAxisSelector.setActiveMeasure(this.activeColorSelection);
                    },
                    scope: this
                }
            });
        }

        return this.colorAxisSelector;
    },

    showColorSelection : function() {
        if (!this.colorwin) {
            this.colorwin = this.createSelectorWindow(this.getColorAxisSelector());
        }

        this.getColorAxisSelector().toggleRemoveVariableButton(Ext.isDefined(this.activeColorSelection) && this.activeColorSelection != null);
        this.getColorAxisSelector().loadSourceCounts();
        this.colorwin.show(this.getColorSelector().getEl());
    },

    variableSelectionMade : function(win, targetEl) {
        if (Ext.isDefined(this.activeYSelection)) {
            this.initialized = true;
            Connector.getState().clearSelections(true);
            this.showTask.delay(10);
            win.hide(targetEl);

            this.fireEvent('userplotchange', this, {
                targetId : targetEl.id,
                x: this.activeXSelection,
                y: this.activeYSelection,
                color: this.activeColorSelection
            });
        }
        else {
            // if we don't yet have a y-axis selection, show that variable selector
            win.hide(targetEl, function() {
                this.showYMeasureSelection();
            }, this);
        }
    },

    createSelectorWindow : function(item) {
        var win = Ext.create('Ext.window.Window', {
            ui: 'axiswindow',
            minHeight: 580,
            modal: true,
            draggable: false,
            header: false,
            closeAction: 'hide',
            resizable: false,
            border: false,
            layout: {
                type: 'fit'
            },
            style: 'padding: 0',
            items: [item],
            listeners: {
                scope: this,
                show: function(cmp) {
                    this.setVisibleWindow(cmp);
                }
            }
        });

        this.updateSelectorWindow(win);
        return win;
    },

    removeVariableFromFilter : function(measureIdx) {
        var filter = this.getPlotsFilter();
        if (filter) {
            var m = filter.get('plotMeasures');
            m[measureIdx] = null;
            Connector.getState().updateFilter(filter.get('id'), {plotMeasures: m});
        }
    },

    getPlotsFilter : function() {
        var filters = Connector.getState().getFilters(),
            _filter;

        for (var f=0; f < filters.length; f++) {
            if (filters[f].isPlot() && !filters[f].isGrid()) {
                _filter = filters[f]; break;
            }
        }

        return _filter;
    },

    onFilterChange : function(filters) {
        // plot lock prevents from listening to plots own changes to state filters
        if (this.plotLock) {
            this.plotLock = false;
            return;
        }

        // mark as clear when there are no plot filters
        this.filterClear = true;
        for (var f=0; f < filters.length; f++) {
            if (filters[f].isPlot() && !filters[f].isGrid()) {
                this.filterClear = false;
                break;
            }
        }

        if (this.isActiveView) {
            this.onReady(function() {
                this.showTask.delay(100);
            }, this);
        }
        else {
            this.refreshRequired = true;
        }

        Connector.getService('Query').clearSourceCountsCache();
    },

    onActivate: function() {
        this.isActiveView = true;
        if (this.refreshRequired) {
            this.onReady(function() {
                this.showTask.delay(100);
            }, this);
        }

        if (Ext.isObject(this.visibleWindow)) {
            this.visibleWindow.show();
        }
    },

    onDeactivate : function() {
        this.isActiveView = false;
        this.fireEvent('hideload', this);
        this.hideMessage();
        this.hideVisibleWindow();
    },

    applyFiltersToMeasure : function(measureSet, ptids) {
        // find the subject column(s) in the measure set to apply the values filter (issue 24123)
        if (Ext.isArray(ptids)) {
            Ext.each(measureSet, function(m) {
                if (m.measure && m.measure.name == Connector.studyContext.subjectColumn) {
                    if (Ext.isArray(m.measure.values)) {
                        console.error('There is a potentially unknown values array on the applied subject measure.');
                    }

                    m.measure.values = ptids;
                }
            }, this);
        }
    },

    onPlotSelectionRemoved : function(filterId, measureIdx) {
        var curExtent = this.plot.getBrushExtent();
        if (curExtent) {
            if (curExtent[0][0] === null || curExtent[0][1] === null) {
                // 1D, just clear the selection.
                this.clearAllBrushing();
            }
            else {
                // 2D selection.
                if (measureIdx === 0) {
                    // clear the x-axis.
                    this.plot.setBrushExtent([[null, curExtent[0][1]],[null, curExtent[1][1]]]);
                }
                else if (measureIdx === 1) {
                    // clear the y-axis.
                    this.plot.setBrushExtent([[curExtent[0][0], null],[curExtent[1][0], null]]);
                }
            }
        }
    },

    onSelectionChange : function(selections) {
        if (selections.length === 0) {
            var ex = this.plot.getBrushExtent();
            if (ex !== null) {
                this.clearAllBrushing();
            }
        }

        if (Ext.isFunction(this.highlightSelectedFn)) {
            this.highlightSelectedFn();
        }
    },

    getStudyAxisData : function(chartData) {
        var studyVisitTagStore = Connector.getApplication().getStore('StudyVisitTag');
        if (studyVisitTagStore.loading) {
            studyVisitTagStore.on('load', function(store) {
                this.getStudyVisitTagRecords(store, chartData);
            }, this);
        }
        else {
            this.getStudyVisitTagRecords(studyVisitTagStore, chartData);
        }
    },

    getStudyVisitTagRecords : function(store, chartData) {
        var alignMap = chartData.getContainerAlignmentDayMap(),
            studyContainers = Object.keys(alignMap);

        // filter the StudyVisitTag store based on the study container id array
        var containerFilteredRecords = store.queryBy(function(record) {
            return studyContainers.indexOf(record.get('container_id')) > -1;
        }).items;

        var studyAxisData = Ext.create('Connector.model.StudyAxisData', {
            records: containerFilteredRecords,
            measure: this.measures[0],
            containerAlignmentDayMap: alignMap
        });

        this.hasStudyAxisData = studyAxisData.getData().length > 0;

        if (chartData.getDataRows().totalCount == 0) {
            // show empty plot message if we have no data in main plot or gutter plots
            this.noPlot(true);
        }
        else {
            this.initPlot(chartData, studyAxisData);
        }
    },

    showVisitTagHover : function(data, visitTagEl) {
        var bubbleWidth, groupWidth = 0, tagWidth = 0,
            groupTags = {}, maxWidth = 0,
            config, visitTag, visitTagGrp, keyCount = 0;

        // map data to set object mapped by group (i.e. 'Group 1 Vaccine')
        for (var i = 0; i < data.visitTags.length; i++) {

            visitTag = data.visitTags[i];
            visitTagGrp = visitTag.group;

            if (!groupTags[visitTagGrp]) {
                keyCount++;
                groupTags[visitTagGrp] = {
                    tags: [],
                    desc: '',
                    isChallenge: data.isChallenge,
                    isVaccination: data.isVaccination
                };
            }

            groupTags[visitTagGrp].tags.push(visitTag.tag);
            groupTags[visitTagGrp].desc = visitTag.desc;

            // CONSIDER: Ideally, we could somehow measure the resulting tag's width by
            // asking the browser how wide the element would be (shadow DOM?)
            groupWidth = Ext.htmlEncode(visitTag.group).length;
            tagWidth = Ext.htmlEncode(groupTags[visitTagGrp].tags.join(',')).length + 4;

            if ((groupWidth + tagWidth) > maxWidth) {
                maxWidth = groupWidth + tagWidth;
            }
        }

        if (keyCount == 1) {

            var labelLength = Ext.htmlEncode(visitTag.group).length,
                tagLength = Ext.htmlEncode(groupTags[visitTagGrp].tags.join(',')).length + 4,
                descLength = Ext.htmlEncode(groupTags[visitTagGrp].desc).length + 3;

            if (groupTags[visitTagGrp].isVaccination) {
                maxWidth = Math.max(labelLength + descLength, tagLength);
            }
            else {
                maxWidth = labelLength + tagLength;
            }
        }

        var groupKeys = Object.keys(groupTags).sort(LABKEY.app.model.Filter.sorters.natural),
            isAggregate = groupKeys.length > 1,
            tplGroups = [];

        Ext.each(groupKeys, function(key) {
            var group = groupTags[key];

            tplGroups.push({
                label: key,
                desc: group.desc,
                tags: group.tags,
                isChallenge: group.isChallenge,
                isVaccination: group.isVaccination
            });
        });

        bubbleWidth = Math.min(maxWidth * 8, 400);

        config = {
            bubbleWidth: bubbleWidth,
            xOffset: -(bubbleWidth / 2),          // the non-vaccination icon is slightly smaller
            arrowOffset: (bubbleWidth / 2) - 10 - ((data.isVaccination || data.isChallenge) ? 4 : 0),
            target: visitTagEl,
            placement: 'top',
            title: data.studyLabel + ' - ' + data.label,
            content: Connector.view.Chart.studyAxisTipTpl.apply({
                groups: tplGroups,
                isAggregate: isAggregate
            })
        };

        ChartUtils.showCallout(config, 'hidevisittagmsg', this);

        // show the hover icon for this glyph
        // TODO: Re-enable when filtering by tag is implemented
        //this.updateVisitTagIcon(visitTagEl, 'normal', 'hover');
    },

    removeVisitTagHover : function(data, visitTagEl) {
        // change hover icon back to normal glyph state
        // TODO: Re-enable when filtering by tag is implemented
        //this.updateVisitTagIcon(visitTagEl, 'hover', 'normal');

        this.fireEvent('hidevisittagmsg', this);
    },

    updateVisitTagIcon : function(el, currentSuffix, newSuffix) {
        var suffix = '_' + currentSuffix + '.svg', iconHref = el.getAttribute('href');
        if (iconHref.indexOf(suffix, iconHref.length - suffix.length) !== -1) {
            el.setAttribute('href', iconHref.replace(suffix, '_' + newSuffix + '.svg'));
        }
    },

    initStudyAxis : function(studyAxisInfo) {
        if (!this.studyAxis) {
            this.studyAxis = Connector.view.StudyAxis().renderTo('study-axis');
        }

        this.studyAxis.studyData(studyAxisInfo.getData())
                .scale(this.plot.scales.x.scale)
                .width(Math.max(0, this.getStudyAxisPanel().getWidth() - 40))
                .visitTagMouseover(this.showVisitTagHover, this)
                .visitTagMouseout(this.removeVisitTagHover, this);

        this.studyAxis();
    },

    resizePlotContainers : function(numStudies) {
        if (this.requireStudyAxis && this.hasStudyAxisData) {
            this.plotEl.setStyle('padding', '0 0 0 ' + this.studyAxisWidthOffset + 'px');
            this.getStudyAxisPanel().setVisible(true);
            // set max height to 1/3 of the center region height
            this.getStudyAxisPanel().setHeight(Math.min(this.getCenter().getHeight() / 3, Math.max(20 * numStudies + 5, this.minStudyAxisHeight)));
        }
        else {
            this.plotEl.setStyle('padding', '0');
            this.getStudyAxisPanel().setVisible(false);
        }
    },

    // FOR TESTING USE
    showPlotDataGrid : function() {
        window.open(LABKEY.ActionURL.buildURL('query', 'executeQuery', null, {
            schemaName: this.dataQWP.schema,
            'query.queryName': this.dataQWP.query
        }), '_blank');
    }
});
