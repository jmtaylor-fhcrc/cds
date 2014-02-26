/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.Scatter', {

    extend: 'Ext.panel.Panel',

    requires: ['Connector.panel.AxisSelector'],

    alias: 'widget.plot',

    cls: 'scatterview',

    measures: [],
    subjectColumn: 'ParticipantId',
    subjectVisitColumn: 'ParticipantVisit',
    allColumns: false,
    canShowHidden: false,

    isActiveView: true,
    refreshRequired: true,
    initialized: false,
    showAxisButtons: true,

    plotHeightOffset: 90, // value in 'px' that the plot svg is offset for container region
    rowlimit: 5000,

    initComponent : function() {

        this.items = [
            this.getYAxisButton(),
            this.getPlotDisplay(),
            this.getXAxisButton(),
            this.getSrcButton()
        ];

        this.callParent();

        this.on('afterrender', function() {
            Ext.create('Ext.Component', {
                id: 'scatterloader',
                renderTo: Ext.getBody(),
                autoEl: {
                    tag: 'img',
                    src: LABKEY.contextPath + '/production/Connector/resources/images/grid/loading.gif',
                    alt: 'loading',
                    height: 25,
                    width: 25
                }
            });
        }, this, {single: true});

        this.attachInternalListeners();
    },

    attachInternalListeners : function() {

        this.showTask = new Ext.util.DelayedTask(this.onShowGraph, this);
        this.resizeTask = new Ext.util.DelayedTask(this.handleResize, this);

        this.on('resize', function() {
            this.resizeTask.delay(150);
        }, this);
    },

    getPlotDisplay : function() {

        if (!this.plotDisplay) {
            this.plotDisplay = Ext.create('Ext.Component', {
                autoEl: {
                    tag: 'div',
                    cls: 'emptyplot plot'
                },
                listeners: {
                    afterrender: function(c) {
                        this.plotid = Ext.get(Ext.DomQuery.select('.emptyplot')[0]).id;
                    },
                    scope: this
                },
                scope: this
            });
        }

        return this.plotDisplay;
    },

    getSrcButton : function() {
        var btn = this.items ? this.query('#plotsourcesbutton') : null;
        if (!btn) {
            btn = Ext.create('Connector.button.RoundedButton', {
                id: 'plotsources',
                itemId: 'plotsourcesbutton',
                text: 'Sources',
                ui: 'rounded-accent',
                hidden: true,
                handler: function() {
                    if (this.srcs && this.srcs.length > 0)
                        this.fireEvent('sourcerequest', this.srcs, this.srcs[0]);
                },
                scope: this
            });
        }
        else {
            btn = btn[0];
        }

        return btn;
    },

    getXAxisButton : function() {
        var btn = this.items ? this.query('#xaxisbutton') : null;
        if (!btn) {
            btn = Ext.create('Connector.button.DropDownButton', {
                itemId: 'xaxisbutton',
                cls: 'xaxisbutton',
                text: '&#9650;', // up-arrow
                hidden: !this.showAxisButtons,
                scope : this
            });
        }
        else {
            btn = btn[0];
        }

        return btn;
    },

    getYAxisButton : function() {
        var btn = this.items ? this.query('#yaxisbutton') : null;
        if (!btn) {
            btn = Ext.create('Connector.button.DropDownButton', {
                itemId: 'yaxisbutton',
                cls: 'yaxisbutton',
                text: '&#9658;', // right-arrow
                hidden: !this.showAxisButtons,
                scope: this
            });
        }
        else {
            btn = btn[0];
        }

        return btn;
    },

    getPlotElements : function() {
        return Ext.DomQuery.select('.axis');
    },

    getPlotElement : function() {
        var el = Ext.DomQuery.select('svg');
        if (el.length > 0) {
            el = el[0];
        }
        return el;
    },

    handleResize : function() {

        if (!this.isActiveView) {
            return;
        }

        var plotbox = this.getBox();

        if (!this.initialized && !this.showNoPlot) {
            this.showNoPlot = true;
            this.noPlot();
        }

        if (this.msg) {
            this.msg.getEl().setLeft(Math.floor(plotbox.width/2 - Math.floor(this.getEl().getTextWidth(this.msg.msg)/2)));
        }

        if (this.ywin && this.ywin.isVisible()) {
            this.updateMeasureSelection(this.ywin);
        }

        if (this.xwin && this.xwin.isVisible()) {
            this.updateMeasureSelection(this.xwin);
        }

        if (this.plot) {
            var dim = this.getAspect(plotbox.width, plotbox.height);
            this.plot.setSize(dim, dim, true);

            if (this.showAxisButtons) {
                var plotEl = this.getPlotElement();
                if (plotEl) {
                    var box = Ext.get(plotEl).getBox();
                    var x = Math.floor(box.x - 25); // minus the buttons width
                    var y = Math.floor((box.height/2) - 10); // line up with label
                    this.getYAxisButton().setPosition(x,y);
                }
            }
        }
    },

    getAspect : function(w, h) {
        // maintain ratio 1:1
        var aspect = Math.round((w > h ? h : w));
        if (aspect <= 0) {
            aspect = this.getHeight();
        }
        aspect = Math.floor(aspect * 0.95);
        return aspect;
    },

    initPlot : function(config, noplot) {
        var rows = config.rows;
        // Below vars needed for brush and mouse event handlers.
        var isBrushed = false, plot;

        if (!rows || !rows.length) {
            this.showMessage('No information available to plot.');
            this.hideLoad();
            this.plot = null;
            Ext.get(this.plotid).update('');
            this.noPlot();
            return;
        }
        else if (rows.length < this.rowlimit && !noplot && (this.percentOverlap && this.percentOverlap == 1)) {
            this.hideMessage();
        }

        if (this.plot) {
            this.plot.clearGrid();
            Ext.get(this.plotid).update('');
            this.plot = null;
        }

        var pointLayer = new LABKEY.vis.Layer({
            geom: new LABKEY.vis.Geom.Point({
                size: 3,
                plotNullPoints: !noplot,
                opacity: 0.5
            }),
            aes: {
                yLeft: function(row){return row.y},
                hoverText : function(row) {
                    // TODO: figure out how to display subject id.
                    return '' + row.xname + ': ' + row.x + ', ' + row.yname + ': ' + row.y;
                },
                mouseOverFn: function(event, pointData, layerSel){
                    if (!isBrushed) {
                        var colorFn, opacityFn, strokeFn, colorScale = null, colorAcc = null;

                        if (plot.scales.color && plot.scales.color.scale) {
                            colorScale = plot.scales.color.scale;
                            colorAcc = plot.aes.color;
                        }

                        colorFn = function(d) {
                            if (d.subjectId.value === pointData.subjectId.value) {
                                return '#01BFC2';
                            } else {
                                if (colorScale && colorAcc) {
                                    return colorScale(colorAcc.getValue(d));
                                }

                                return '#000000';
                            }
                        };

                        strokeFn = function(d) {
                            if (d.subjectId.value === pointData.subjectId.value) {
                                return '#00EAFF';
                            } else {
                                if (colorScale && colorAcc) {
                                    return colorScale(colorAcc.getValue(d));
                                }

                                return '#000000';
                            }
                        };

                        opacityFn = function(d) {
                            return  d.subjectId.value === pointData.subjectId.value ? 1 : .5;
                        };

                        layerSel.selectAll('.point path').attr('fill', colorFn)
                                .attr('stroke', strokeFn)
                                .attr('fill-opacity', opacityFn)
                                .attr('stroke-opacity', opacityFn);
                    }
                },
                mouseOutFn: function(event, pointData, layerSel){
                    if (!isBrushed) {
                        var colorFn, colorScale = null, colorAcc = null;

                        if (plot.scales.color && plot.scales.color.scale) {
                            colorScale = plot.scales.color.scale;
                            colorAcc = plot.aes.color;
                        }

                        colorFn = function(d) {
                            if (colorScale && colorAcc) {
                                return colorScale(colorAcc.getValue(d));
                            }

                            return '#000000';
                        };

                        layerSel.selectAll('.point path').attr('fill', colorFn)
                                .attr('stroke', colorFn)
                                .attr('fill-opacity', .5)
                                .attr('stroke-opacity', .5);
                    }
                }
            }
        });

        // maintain ratio 1:1
        var box = Ext.get(this.plotid).getSize();
        var aspect = this.getAspect(box.width, box.height);

        var tickFormat = function(val) {

            if (noplot) {
                return '';
            }

            var s = val.toString();
            if (s.indexOf('.') > -1)
            {
                s = s.split('.');
                if (s[s.length-1].length > 2)
                {
                    s[s.length-1] = s[s.length-1].substr(0,2);
                    s = s.join('.');
                    return parseFloat(s, 10);
                }
            }
            return val;
        };

        var scales = {};
        if (noplot) {
            scales.x = scales.yLeft = {
                scaleType: 'continuous',
                domain: [0, 100]
            };
        }
        else {
            scales.x = {scaleType: 'continuous'};
            scales.yLeft = {scaleType: 'continuous'};
        }

        scales.x.tickFormat     = tickFormat;
        scales.yLeft.tickFormat = tickFormat;

        var labelX = (config.xaxis) ? config.xaxis.query + ': ' : '',
                labelY = (config.yaxis) ? config.yaxis.query + ': ' : '';

        var plotConfig = {
            renderTo: this.plotid,
            rendererType: 'd3',
            throwErrors: true,
            labels: {
                x: {value: labelX + rows[0].xname.replace(/_/g, ' ')},
                yLeft: {value: labelY + rows[0].yname.replace(/_/g, ' ')}
            },
            width     : aspect,
            height    : aspect,
            data      : rows,
            legendPos : 'none',
            aes: {
                x: function(row){return row.x;}
            },
            brushing: {
                brushstart: function(event, data, extent, layerSelections){
                    isBrushed = true;
                },
                brush: function(event, layerData, extent, layerSelections){
                    var sel = layerSelections[0]; // We only have one layer, so grab the first one.
                    var colorFn, opacityFn, strokeFn, colorScale = null, colorAcc = null;

                    if (plot.scales.color && plot.scales.color.scale) {
                        colorScale = plot.scales.color.scale;
                        colorAcc = plot.aes.color;
                    }

                    colorFn = function(d) {
                        var x = d.x, y = d.y;
                        d.isSelected = (x > extent[0][0] && x < extent[1][0] && y > extent[0][1] && y < extent[1][1]);
                        if (d.isSelected) {
                            return '#14C9CC';
                        } else {
                            if (colorScale && colorAcc) {
                                return colorScale(colorAcc.getValue(d));
                            }

                            return '#000000';
                        }
                    };

                    strokeFn = function(d) {
                        if (d.isSelected) {
                            return '#00393A';
                        } else {
                            if (colorScale && colorAcc) {
                                return colorScale(colorAcc.getValue(d));
                            }

                            return '#000000';
                        }
                    };

                    opacityFn = function(d) {
                        return d.isSelected ? 1 : .5;
                    };

                    sel.selectAll('.point path').attr('fill', colorFn)
                            .attr('stroke', strokeFn)
                            .attr('fill-opacity', opacityFn)
                            .attr('stroke-opacity', opacityFn);
                },
                brushclear: function(event, data, layerSelections){
                    isBrushed = false;
                }
            },
            bgColor   : '#F0F0F0', // see $light-color in connector.scss
            gridColor : '#FFFFFF',
            gridLineColor : '#FFFFFF',
            scales: scales
        };

        if (!noplot) {
            this.setScale(plotConfig.scales.x, 'x', config);
            this.setScale(plotConfig.scales.yLeft, 'y', config);
        }

        this.plot = new LABKEY.vis.Plot(plotConfig);
        plot = this.plot; // hoisted for brushing events

        if (this.plot) {
            this.plot.addLayer(pointLayer);
            try
            {
                this.plot.render();
            }
            catch(err) {
                this.showMessage(err.message);
                this.hideLoad();
                this.plot = null;
                Ext.get(this.plotid).update('');
                this.noPlot();
                return;
            }
        }
        this.hideLoad();
    },

    getScale : function(axis) {
        var scale = 'linear';
        if (axis == 'y' && this.axisPanelY) {
            scale = this.axisPanelY.getScale();
        }
        else if (axis == 'x' && this.axisPanelX) {
            scale = this.axisPanelX.getScale();
        }

        return scale;
    },

    setScale : function(scale, axis, config) {
        var axisValue = this.getScale(axis), allowLog = (axis == 'y') ? !config.setYLinear : !config.setXLinear;

        if (!allowLog && axisValue == 'log') {
            this.showMessage('Displaying the ' + axis.toLowerCase() + '-axis on a linear scale due to the presence of invalid log values.');
            axisValue = 'linear';
        }

        Ext.apply(scale, {
            trans : axisValue,
            domain: [axisValue == 'log' ? 1 : null, null] // allow negative values in linear plots
        });

        return scale;
    },

    getActiveMeasures : function() {
        this.fromFilter = false;
        var measures = {
            x: null,
            y: null
        };

        // first check the measure selections
        if (this.axisPanelX) {
            var sel = this.axisPanelX.getSelection();
            if (sel && sel.length > 0) {
                measures.x = sel[0].data;
            }
        }
        if (this.axisPanelY) {
            var sel = this.axisPanelY.getSelection();
            if (sel && sel.length > 0) {
                measures.y = sel[0].data;
            }
        }

        // second, check the set of active filters
        if (!measures.x && !measures.y) {
            var filters = this.state.getFilters();
            for (var f=0; f < filters.length; f++) {
                if (filters[f].get('isPlot') == true) {
                    var m = filters[f].get('plotMeasures');
                    measures.x = m[0].measure;
                    measures.y = m[1].measure;
                    this.fromFilter = true;
                    break;
                }
            }
        }

        return measures;
    },

    onShowGraph : function() {

        this.hideMessage();
        this.refreshRequired = false;

        var activeMeasures = this.getActiveMeasures();

        if (this.filterClear || !activeMeasures.x || !activeMeasures.y) {
            this.filterClear = false;
            this.noPlot();
            return;
        }

        this.measures = [ activeMeasures.x, activeMeasures.y ];

        this.showLoad();

        if (this.measures.length > 0) {

            var sorts = this.getSorts();

            var wrappedMeasures = [
                {measure : this.measures[0], time: 'visit'},
                {measure : this.measures[1], time: 'visit'}
            ];

            if (!this.fromFilter) {
                this.updatePlotBasedFilter(wrappedMeasures);
            }
            else {
                this.initialized = true;
            }

            // Request Participant List
            this.getParticipantIn(function(ptidList) {

                if (ptidList)
                {
                    this.applyFiltersToSorts(sorts, ptidList);
                }

                // Request Chart Data
                Ext.Ajax.request({
                    url: LABKEY.ActionURL.buildURL('visualization', 'getData.api'),
                    method: 'POST',
                    jsonData: {
                        measures: wrappedMeasures,
                        sorts: sorts,
                        limit: (this.rowlimit+1)
                    },
                    success: this.onChartDataSuccess,
                    failure: this.onFailure,
                    scope: this
                });

                this.requestCitations();
            });
        }
    },

    showLoad : function() {
        if (!this.isActiveView) {
            return;
        }
        var plotEl = this.getPlotElement();
        if (plotEl) {
            var box = Ext.get(plotEl).getBox();
            var sload = Ext.get('scatterloader');
            sload.setLeft(box.x+10);
            sload.setTop(box.y+10);
            if (this.isActiveView) {
                sload.setStyle('visibility', 'visible');
            }
        }
    },

    hideLoad : function() {
        Ext.get('scatterloader').setStyle('visibility', 'hidden');
    },

    requestCitations : function() {
        var measures = this.getActiveMeasures();
        var x = measures.x, y = measures.y;

        var xy = [{
            s : x.schemaName,
            q : x.queryName
        },{
            s : y.schemaName,
            q : y.queryName
        }];

        this.srcs = [];
        var me = this;
        for (var i=0; i < xy.length; i++) {
            LABKEY.Query.getQueryDetails({
                schemaName : xy[i].s,
                queryName  : xy[i].q,
                success    : function(d) {
                    for (var c=0; c < d.columns.length; c++) {
                        if (d.columns[c].name.toLowerCase() == 'source') {
                            var src = d.columns[c];
                            Ext.apply(src, {
                                isSourceURI : true,
                                schemaName  : d.schemaName,
                                queryName   : d.queryName || d.name,
                                alias       : src.fieldKeyPath
                            });
                            me.srcs.push(src);
                        }
                    }
                    if (me.srcs.length == 0) {
                        me.getSrcButton().hide();
                    }
                    else {
                        me.getSrcButton().show();
                    }
                }
            });
        }
    },

    onChartDataSuccess : function(response) {

        if (!this.isActiveView) {
//            this.priorResponse = response;
            return;
        }

        // preprocess decoded data shape
        var config = this._preprocessData(Ext.decode(response.responseText));

        // call render
        this.initPlot(config, false);
    },

    updatePlotBasedFilter : function(measures) {
        // Request Distinct Participants
        Ext.Ajax.request({
            url: LABKEY.ActionURL.buildURL('visualization', 'getData.api'),
            method: 'POST',
            jsonData: {
                measures: measures,
                sorts: this.getSorts(),
                limit: (this.rowlimit+1)
            },
            success: function(response) {
                this.onFilterDataSuccess(Ext.decode(response.responseText), measures);
            },
            failure: this.onFailure,
            scope: this
        });
    },

    onFilterDataSuccess : function(r, measures) {
        LABKEY.Query.selectDistinctRows({
            schemaName: r.schemaName,
            queryName: r.queryName,
            column: r.measureToColumn[this.subjectColumn],
            success: function(data) {

                var filter = {
                    hierarchy: 'Subject',
                    isPlot: true,
                    plotMeasures: measures,
                    plotScales: [this.getScale('x'), this.getScale('y')],
                    members: []
                };

                for (var i=0; i < data.values.length; i++) {
                    filter.members.push({
                        uname: ['Subject', data.values[i]]
                    });
                }

                this.plotLock = true;
                var filters = this.state.getFilters(), found = false;
                for (var f=0; f < filters.length; f++) {
                    if (filters[f].get('isPlot') == true) {
                        found = true;
                        filters[f].set('plotMeasures', measures);
                        this.state.updateFilterMembers(filters[f].get('id'), filter.members);
                        break;
                    }
                }
                if (!found) {
                    this.state.addFilters([filter]);
                }
                this.plotLock = false;
            },
            scope: this
        });
    },

    noPlot : function() {

        var map = [{
            x : null,
            xname : 'X-Axis',
            y : null,
            yname : 'Y-Axis',
            subjectId: null
        }];

        this.initPlot({rows:map}, true);
        this.resizeTask.delay(300);
    },

    onFailure : function(response) {
        this.hideLoad();
        this.showMessage('Failed to Load');
    },

    isValidNumber: function(number){
        return !(number === undefined || isNaN(number) || number === null);
    },

    _preprocessData : function(data) {

        var x = this.measures[0], y = this.measures[1];

        // TODO: In the future we will have data from multiple studies, meaning we might have more than one valid
        // subject columName value. We'll need to make sure that when we get to that point we have some way to coalesce
        // that information into one value for the SubjectId (i.e. MouseId, ParticipantId get renamed to SubjectId).
        var subjectNoun = LABKEY.moduleContext.study.subject.columnName;
        var subjectColumn = data.measureToColumn[subjectNoun];
        var xa = {
            schema : x.schemaName,
            query  : x.queryName,
            name   : x.name,
            alias  : x.alias,
            label  : x.label
        };

        var ya = {
            schema : y.schemaName,
            query  : y.queryName,
            name   : y.name,
            alias  : y.alias,
            label  : y.label
        };

        var map = [], r,
                _xid = data.measureToColumn[xa.alias] || data.measureToColumn[xa.name],
                _yid = data.measureToColumn[ya.alias] || data.measureToColumn[ya.name],
                rows = data.rows,
                len = rows.length,
                validCount = 0;

        if (len > this.rowlimit) {
            len = this.rowlimit;
            this.showMessage('Plotting first ' + Ext.util.Format.number(this.rowlimit, '0,000') + ' points.');
        }
        else if (this.msg) {
            this.msg.hide();
        }

        var xnum, ynum, negX = false, negY = false;
        for (r = 0; r < len; r++) {
            if (rows[r][_xid] && rows[r][_yid]) {
                x = parseFloat(rows[r][_xid].value, 10);
                y = parseFloat(rows[r][_yid].value, 10);

                // allow any pair that does not contain a negative value.
                // NaN, null, and undefined are non-negative values.

                // validate x
                xnum = !(Ext.isNumber(x) && x < 1);
                if (!negX && !xnum) {
                    negX = true;
                }

                // validate y
                ynum = !(Ext.isNumber(y) && y < 1);
                if (!negY && !ynum) {
                    negY = true;
                }

                map.push({
                    x : x,
                    y : y,
                    subjectId: rows[r][subjectColumn],
                    xname : xa.label,
                    yname : ya.label
                });

                if (this.isValidNumber(x) && this.isValidNumber(y)) {
                    validCount ++;
                }
            }
        }

        this.percentOverlap = validCount / len;

        if(this.percentOverlap < 1){
            var id = Ext.id();
            var msg = 'Points outside the plotting area have no match on the other axis.';
            msg += '&nbsp;<a id="' + id +'">Details</a>';
            this.showMessage(msg, true);

            var tpl = new Ext.XTemplate(
                    '<div class="matchtip">',
                    '<div>',
                    '<p class="tiptitle">Plotting Matches</p>',
                    '<p class="tiptext">Percent match: {overlap}%. Mismatches may be due to data point subject, visit, or assay antigen.</p>',
                    '</div>',
                    '</div>'
            );

            var el = Ext.get(id);
            if (el) {
                Ext.create('Ext.tip.ToolTip', {
                    target : el,
                    anchor : 'left',
                    data : {
                        overlap : Ext.util.Format.round(this.percentOverlap * 100, 2)
                    },
                    tpl : tpl,
                    autoHide: true,
                    mouseOffset : [15,0],
                    maxWidth: 500,
                    minWidth: 200,
                    bodyPadding: 0,
                    padding: 0
                });
            }
        }

        return {
            xaxis: xa,
            yaxis: ya,
            rows : map,
            setXLinear : negX,
            setYLinear : negY
        };
    },

    updateMeasureSelection : function(win) {
        if (win) {
            var pos = this.getPlotPosition();
            win.setSize(pos.width, pos.height);
            win.setPosition(pos.leftEdge, pos.topEdge, false);
        }
        else {
            console.warn('Failed to updated measure selection');
        }
    },

    //
    // The intent of this method is to return the position of the plots contents as the user sees them
    //
    getPlotPosition : function() {
        var pos = {
            topEdge: 0,
            leftEdge: 0,
            width: 0,
            height: 0
        };

        var plotEl = this.getPlotElement();
        if (plotEl && this.plot) {
            plotEl = Ext.get(plotEl);
            var box = plotEl.getBox();
            var grid = this.plot.grid;

            pos.topEdge = box.top + grid.topEdge;
            pos.leftEdge = box.left + grid.leftEdge;
            pos.width = grid.rightEdge - grid.leftEdge;
            pos.height = grid.bottomEdge - grid.topEdge;
        }

        return pos;
    },

    showYMeasureSelection : function(targetEl) {

        if (!this.ywin) {

            var sCls = 'yaxissource';

            this.axisPanelY = Ext.create('Connector.panel.AxisSelector', {
                flex: 1,
                title: 'Y Axis',
                bodyStyle: 'padding-left: 27px; padding-top: 15px;',
                open : function() {},
                measureConfig: {
                    allColumns: this.allColumns,
                    filter: LABKEY.Query.Visualization.Filter.create({
                        schemaName: 'study',
                        queryType: LABKEY.Query.Visualization.Filter.QueryType.DATASETS
                    }),
                    showHidden: this.canShowHidden,
                    cls: 'yaxispicker',
                    sourceCls: sCls,
                    multiSelect: false
                },
                displayConfig: {
                    defaultHeader: 'Choose Y Axis'
                },
                scalename: 'yscale'
            });

            var pos = this.getPlotPosition();

            this.ywin = Ext.create('Ext.window.Window', {
                id: 'plotymeasurewin',
                ui: 'axiswindow',
                cls: 'axiswindow',
                animateTarget: targetEl,
                sourceCls: sCls,
                axisPanel: this.axisPanelY,
                modal: true,
                draggable: false,
                resizable: false,
                minHeight: 450,
                height: pos.height,
                width: pos.width,
                x: pos.leftEdge,
                y: pos.topEdge,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [this.axisPanelY],
                buttons: [{
                    text: 'Set Y-Axis',
                    ui: 'rounded-inverted-accent',
                    handler: function() {
                        if (this.axisPanelX && this.axisPanelX.hasSelection() && this.axisPanelY.hasSelection()) {
                            this.initialized = true;
                            this.showTask.delay(300);
                            this.ywin.hide();
                        }
                        else if (this.axisPanelY.hasSelection()) {
                            this.ywin.hide(null, function(){
                                this.showXMeasureSelection(this.getXAxisButton().getEl());
                            }, this);
                        }
                    },
                    scope: this
                },{
                    text: 'cancel',
                    ui: 'rounded-inverted-accent',
                    handler: function() {
                        if (this.activeYSelection) {
                            this.axisPanelY.setSelection(this.activeYSelection);
                            this.activeYSelection = undefined;
                        }
                        this.ywin.hide();
                    },
                    scope : this
                }],
                scope : this
            });

        }
        else {
            this.updateMeasureSelection(this.ywin);
        }

        if (this.axisPanelY.hasSelection()) {
            this.activeYSelection = this.axisPanelY.getSelection()[0];
        }
        this.ywin.show(null, function() {
            this.runUniqueQuery(false, this.axisPanelY, 'yaxissource');
        }, this);
    },

    showXMeasureSelection : function(targetEl) {

        if (!this.xwin) {

            var sCls = 'xaxissource';

            this.axisPanelX = Ext.create('Connector.panel.AxisSelector', {
                flex      : 1,
                ui        : 'axispanel',
                title     : 'X Axis',
                bodyStyle : 'padding-left: 27px; padding-top: 15px;',
                measureConfig : {
                    allColumns : this.allColumns,
                    filter     : LABKEY.Query.Visualization.Filter.create({
                        schemaName: 'study',
                        queryType: LABKEY.Query.Visualization.Filter.QueryType.DATASETS
                    }),
                    showHidden : this.canShowHidden,
                    cls        : 'xaxispicker',
                    sourceCls  : sCls,
                    multiSelect: false
                },
                displayConfig : {
                    defaultHeader : 'Choose X Axis'
                },
                scalename : 'xscale'
            });

            var pos = this.getPlotPosition();

            this.xwin = Ext.create('Ext.window.Window', {
                id        : 'plotxmeasurewin',
                cls       : 'axiswindow',
                animateTarget : targetEl,
                sourceCls : sCls,
                axisPanel : this.axisPanelX,
                modal     : true,
                draggable : false,
                resizable : false,
                minHeight : 450,
                height: pos.height,
                width: pos.width,
                x: pos.leftEdge,
                y: pos.topEdge,
                layout : {
                    type : 'vbox',
                    align: 'stretch'
                },
                items   : [this.axisPanelX],
                buttons : [{
                    text  : 'Set X-Axis',
                    ui    : 'rounded-inverted-accent',
                    handler : function() {
                        if (this.axisPanelY && this.axisPanelY.hasSelection() && this.axisPanelX.hasSelection()) {
                            this.initialized = true;
                            this.showTask.delay(300);
                            this.xwin.hide();
                        }
                        else if (this.axisPanelX.hasSelection()) {
                            this.xwin.hide(null, function(){
                                this.showYMeasureSelection(this.getYAxisButton().getEl());
                            }, this);
                        }
                    },
                    scope: this
                },{
                    text  : 'cancel',
                    ui    : 'rounded-inverted-accent',
                    handler : function() {
                        if (this.activeXSelection) {
                            this.axisPanelX.setSelection(this.activeXSelection);
                            this.activeXSelection = undefined;
                        }
                        this.xwin.hide();
                    },
                    scope : this
                }],
                scope : this
            });

        }
        else {
            this.updateMeasureSelection(this.xwin);
        }

        if (this.axisPanelX.hasSelection()) {
            this.activeXSelection = this.axisPanelX.getSelection()[0];
        }
        this.xwin.show(null, function() {
            this.runUniqueQuery(false, this.axisPanelX, 'xaxissource');
        }, this);
    },

    runUniqueQuery : function(force, axisSelector, cls) {
        var picker = axisSelector.getMeasurePicker();

        if (picker) {
            var store = picker.getSourceStore();
            if (force) {
                if (store.getCount() > 0) {
                    this._processQuery(store, cls);
                }
                else {
                    store.on('load', function(s) {
                        this._processQuery(s, cls);
                    }, this, {single: true});
                }
            }
            else if (!force) {
                if (this.control) {
                    var me = this;
                    this.control.getParticipantIn(function(ptids){
                        if (!me.initialized) {
                            me.queryPtids = ptids;
                            me.runUniqueQuery(true, axisSelector, cls);
                        }
                    });
                }
            }
        }
    },

    _processQuery : function(store, cls) {
        var sources = [], s;

        for (s=0; s < store.getCount(); s++) {
            sources.push(store.getAt(s).data['queryLabel'] || store.getAt(s).data['queryName']);
        }

        if (this.control) {
            var me = this;
            if (this.state.getFilters().length == 0) {
                me.control.requestCounts(sources, [], function(r){
                    me._postProcessQuery(r, cls);
                }, me);
            }
            else {
                this.control.getParticipantIn(function(ids) {
                    me.control.requestCounts(sources, ids, function(r){
                        me._postProcessQuery(r, cls);
                    }, me);
                });
            }
        }
    },

    _postProcessQuery : function(response, cls) {
        this.control.displayCounts(response, cls);
    },

    showMessage : function(msg, append) {

        if (!append)
            this.hideMessage();
        else if (this.msg && this.msg.isVisible()) {
            this.msg.msg += '<br/>' + msg;
            this.msg.update(this.msg.msg);
            var box = this.getBox();
            var x = Math.floor(box.width/2 - Math.floor(this.getEl().getTextWidth(msg)/2)) - 20;
            this.msg.showAt(x,box.y+20);
            return;
        }

        var box = this.getBox();

        this.msg = Ext.create('Connector.window.SystemMessage', {
            msg : msg,
            x   : Math.floor(box.width/2 - Math.floor(this.getEl().getTextWidth(msg)/2)) - 20,
            y   : (box.y+20), // height of message window,
            autoShow : this.isActiveView
        });
    },

    hideMessage : function() {
        if (this.msg) {
            this.msg.hide();
            this.msg.destroy();
            this.msg = null;
        }
    },

    onFilterChange : function(filters) {
        // plot lock prevents from listening to plots own changes to state filters
        if (this.plotLock) {
            this.plotLock = false;
            return;
        }

        this.filterClear = filters.length == 0;

        if (this.isActiveView) {
            this.showTask.delay(300);
        }
        else if (this.initialized) {
            this.refreshRequired = true;
        }
    },

    onViewChange : function(xtype) {
        this.isActiveView = (xtype == 'plot');

        if (this.isActiveView) {

            if (this.refreshRequired) {
                this.showTask.delay(300);
            }

            if (this.msg) {
                this.msg.show();
            }
        }
        else {
            this.hideLoad();

            if (this.msg) {
                this.msg.hide();
            }

            if (this.win) {
                this.win.hide();
            }
        }
    },

    getParticipantIn : function(callback, scope) {
        var me = this;

        this.state.onMDXReady(function(mdx){

            if (mdx.hasFilter('statefilter')) {
                mdx.queryParticipantList({
                    useNamedFilters : ['statefilter'],
                    success : function (cs) {
                        var ptids = [], pos = cs.axes[1].positions, a;
                        for (a=0; a < pos.length; a++) {
                            ptids.push(pos[a][0].name);
                        }

                        callback.call(scope || me, ptids);
                    },
                    scope : scope || me
                });
            }
            else
                callback.call(scope || me, null);

        }, me);
    },


    applyFiltersToSorts : function (sorts, ptids) {
        var ptidSort;
        for (var i = 0; i < sorts.length; i++)
        {
            if (sorts[i].name == this.subjectColumn) {
                ptidSort = sorts[i];
                break;
            }
        }

        ptidSort.values = ptids;
    },

    getSorts : function() {
        var firstMeasure = this.measures[0];

        // if we can help it, the sort should use the first non-demographic measure
        for (var i=0; i < this.measures.length; i++) {
            var item = this.measures[i];
            if (!item.isDemographic) {
                firstMeasure = item;
                break;
            }
        }

        return [
            {name : this.subjectColumn,                     queryName : firstMeasure.queryName,  schemaName : firstMeasure.schemaName},
            {name : this.subjectVisitColumn + '/VisitDate', queryName : firstMeasure.queryName,  schemaName : firstMeasure.schemaName}
        ];
    }
});
