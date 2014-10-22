/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.Selection', {

    extend: 'LABKEY.app.view.Selection',

    alias: 'widget.selectionview',

    itemSelector: 'div.wrapitem',

    loadMask: false,

    tpl: new Ext.XTemplate(
            '<tpl for=".">',
                '<tpl if="this.isPlotSelection(values) === true">',
                    // Plot Selection Filter
                    '<div class="wrapitem">',
                        '<div class="circle"></div>',
                        '<div class="selitem sel-listing">Subjects with:</div>',
                        '{[this.renderPlotSelection(values)]}',
                    '</div>',
                '</tpl>',
                '<tpl if="this.isGrid(values) === true">',
                    // Grid Filter
                    '<div class="wrapitem">',
                        '<div class="circle"></div>',
                        '<div class="selitem status-over memberitem memberloc">',
                            '<div class="closeitem" data-id="{id}" member-index="0"></div>',
                            '{[this.renderLabel(values)]}',
                        '</div>',
                    '</div>',
                '</tpl>',
                '<tpl if="this.isPlot(values) === true">',
                    // "In the plot" Filter
                    '<div class="wrapitem">',
                        '<div class="circle"></div>',
                        '<div class="selitem status-over memberitem memberloc">',
                            '<div class="closeitem" data-id="{id}" member-index="0"></div>',
                            '{[this.renderMeasures(values)]}',
                        '</div>',
                    '</div>',
                '</tpl>',
                '<tpl if="this.isPlot(values) === false && this.isGrid(values) === false && this.isPlotSelection(values) === false">',
                    // Normal Filter (and Group Filters)
                    '<div class="wrapitem">',
                        '<div class="circle"></div>',
                        '<tpl if="members.length &gt; 0">',
                            '<div class="closeitem wholeitem" data-id="{id}"></div>',
                            '<div class="selitem sel-listing">{[this.renderType(values)]}</div>',
                            '<tpl if="members.length &gt; 1 || isSelection === true">',
                                '<tpl for="members">',
                                    '<tpl if="xindex == 1 && xcount &gt; 1">',
                                        '<select>',
                                            '<option value="' + LABKEY.app.model.Filter.Operators.INTERSECT + '" {parent.operator:this.selectIntersect}>AND</option>',
                                            '<option value="' + LABKEY.app.model.Filter.Operators.UNION + '" {parent.operator:this.selectUnion}>OR</option>',
                                        '</select>',
                                    '</tpl>',
                                    '{% if (parent.isSelection !== true && xindex > 5) break; %}',
                                    '<div class="status-over memberitem memberloc collapsed-member">',
                                        '<span>{uniqueName:this.renderUniqueName}</span>',
                                    '</div>',
                                '</tpl>',
                                '<tpl if="members.length &gt; 5">',
                                    '<div class="fader"><img class="ellipse"></div>',
                                '</tpl>',
                            '</tpl>',
                        '</tpl>',
                    '</div>',
                '</tpl>',
            '</tpl>',
            {
                isGrid : function(values) {
                    var isPlot = values.hasOwnProperty('isPlot') ? values.isPlot : false;
                    var isGrid = values.hasOwnProperty('isGrid') ? values.isGrid : false;
                    return isGrid && !isPlot;
                },
                isPlot : function(values) {
                    var isPlot = values.hasOwnProperty('isPlot') ? values.isPlot : false;
                    var isGrid = values.hasOwnProperty('isGrid') ? values.isGrid : false;
                    return isPlot && !isGrid;
                },
                isPlotSelection : function(values) {
                    var isPlot = values.hasOwnProperty('isPlot') ? values.isPlot : false;
                    var isGrid = values.hasOwnProperty('isGrid') ? values.isGrid : false;
                    return isPlot && isGrid;
                },
                selectIntersect : function(op) {
                    var markup = op.indexOf('AND') > -1 ? 'selected="selected"' : '';
                    if (op.indexOf('REQ_OR') > -1) { markup += ' disabled'; }
                    return markup;
                },
                selectUnion : function(op) {
                    var markup = op.indexOf('OR') > -1 ? 'selected="selected"' : '';
                    if (op.indexOf('REQ_AND') > -1) { markup += ' disabled'; }
                    return markup;
                },
                renderType : function(values) {
                    var lvl = values.level;
                    var label = '';
                    Connector.getState().onMDXReady(function(mdx) {
                        var level = mdx.getLevel(lvl);
                        if (level) {
                            label = level.hierarchy.dimension.friendlyName;

                            // friendlyName was not overidden so compose label with lvl info
                            if (label === level.hierarchy.dimension.singularName) {
                                if (Ext.isDefined(level.countSingular)) {
                                    // escape redundant dim/lvl naming (e.g. Study (Study))
                                    if (level.countSingular.toLowerCase() !== label.toLowerCase()) {
                                        label += ' (' + level.countSingular + ')';
                                    }
                                }
                                else {
                                    label += ' (' + level.name + ')';
                                }
                            }
                        }
                    });

                    label = Ext.htmlEncode(label);
                    if (!values.isSelection) {
                        if (!values.isWhereFilter) {
                            label = "Subjects: " + label;
                        }

                        // render filter with a single member on one line
                        if (values.members.length == 1) {
                            label += ": <div style=\"display:inline;\" class=\" memberloc\">" + this.renderUniqueName(values.members[0].uniqueName) + "</div>";
                        }
                    }

                    return label;
                },
                renderUniqueName : function(uniqueName) {
                    var arrayName = LABKEY.app.view.Selection.uniqueNameAsArray(uniqueName);
                    var member = arrayName[arrayName.length-1];
                    if (member == '#null') {
                        member = 'Unknown';
                    }
                    return Ext.htmlEncode(member);
                },
                renderMeasures : function(values) {
                    var label = 'In the plot: ';
                    var measures = values.plotMeasures, measureLabels = [];
                    for (var i=0; i < measures.length; i++) {
                        if (measures[i]) {
                            measureLabels.push(measures[i].measure.label);
                        }
                    }
                    return Ext.htmlEncode(label + measureLabels.join(', '));
                },
                renderLabel : function(values) {
                    var type = LABKEY.app.model.Filter.getGridHierarchy(values);
                    return Ext.htmlEncode(type + ": " + LABKEY.app.model.Filter.getGridLabel(values));
                },
                renderSelectionMeasure : function(measure, filters, id, idx) {

                    var domString = '';

                    if (measure && filters && filters[0] && filters[1]) {
                        var minVal = filters[0].getValue(),
                            maxVal = filters[1].getValue();

                        if (filters[0].getFilterType().getURLSuffix() === 'dategte') {
                            var d = new Date(minVal);
                            var year = (d.getFullYear()%1000);
                            year = year.toString().length == 1 ? "0" + year : year;
                            minVal = (d.getMonth()+1) + "/" + d.getDate() + "/" + year;
                            d = new Date(maxVal);
                            year = (d.getFullYear()%1000);
                            year = year.toString().length == 1 ? "0" + year : year;
                            maxVal = (d.getMonth()+1) + "/" + d.getDate() + "/" + year;
                        }

                        domString =
                                '<div class="status-over memberitem memberloc plot-selection">' +
                                    '<div class="closeitem measure" data-id="' + id + '" member-index="' + idx + '"></div>' +
                                        measure.measure.label +
                                        ': &gt;= ' + minVal +
                                        ', &lt;= ' + maxVal +
                                '</div>';
                    }

                    return domString;
                },
                renderPlotSelection: function(values) {
                    var measures = values.plotMeasures,
                        filters = values.gridFilter, // TODO: rename to sqlFilters
                        xMeasure = measures[0],
                        yMeasure = measures[1],
                        xFilters = filters.slice(0, 2),
                        yFilters = filters.slice(2),
                        domString;

                    domString = this.renderSelectionMeasure(xMeasure, xFilters, values.id, 0);
                    domString = domString + this.renderSelectionMeasure(yMeasure, yFilters, values.id, 1);

                    return domString;
                }
            }
    )
});