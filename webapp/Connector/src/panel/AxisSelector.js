/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.panel.AxisSelector', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.axisselector',

    ui: 'custom',

    showDisplay: true,

    preventHeader: true,

    displayConfig: {},

    // Configuration for nested Connector.panel.MeasuresView
    measureConfig: {},

    scalename: 'scale',

    disableLookups: true,

    disableScale: false,

    disableAntigenFilter: true,

    selectedSource: null,

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    lookupMap: {},

    initComponent : function() {

        var picker = this.getMeasurePicker();

        this.items = [
            this.getMainTitleDisplay(),
            picker
        ];

        if (this.showDisplay) {
            this.items.push(this.getSelectionDisplay());
        }

        this.callParent();

        var measureGrid = picker.getMeasuresGrid();
        var sourceView = picker.getSourcesView();

        // Source listeners
        sourceView.getSelectionModel().on('selectionchange', this.onSourceSelect, this);

        // Measure listeners
        measureGrid.getSelectionModel().on({
            select: this.onMeasureSelect,
            scope: this
        });

        if (this.showDisplay) {
            sourceView.on('itemclick', this.onSourceClick, this);
            measureGrid.on('itemclick', this.onMeasureClick, this);
        }

        //plugin to handle loading mask for the variable selector
        this.addPlugin({
            ptype: 'loadingmask',
            blockingMask: false,
            itemsMaskCls: 'item-spinner-mask-orange',
            beginConfig: {
                component: picker,
                events: ['beforeMeasureSourceCountsLoad']
            },
            endConfig: {
                component: picker,
                events: ['measureSourceCountsLoad']
            }
        });
    },

    getMainTitleDisplay : function() {
        return {
            xtype: 'box',
            autoEl: {
                tag : 'div',
                cls : 'curseltitle',
                html: this.displayConfig.mainTitle
            }
        };
    },

    getSelectionDisplay : function() {

        if (!this.selectionDisplay) {
            var displayCfg = this.displayConfig;

            Ext.apply(displayCfg, {
                height: 210,
                hidden: true,
                displayConfig: this.displayConfig,
                disableLookups: this.disableLookups,
                disableScale: this.disableScale,
                disableAntigenFilter: this.disableAntigenFilter,
                scalename: this.scalename,
                visitTagStore: this.visitTagStore
            });

            this.selectionDisplay = Ext.create('Connector.panel.AxisSelectDisplay', displayCfg);

            this.selectionDisplay.on('lookupselect', function()
            {
                if (this.lastMeasure)
                {
                    this.getMeasurePicker().getMeasuresGrid().getSelectionModel().select(this.lastMeasure, true, false);
                }
            }, this);
        }

        return this.selectionDisplay;
    },

    hideScale : function() {
        this.getSelectionDisplay().getScaleForm().hide();
    },

    showScale : function() {
        this.getSelectionDisplay().getScaleForm().show();
    },

    getScale : function() {
        return this.getSelectionDisplay().getScaleForm().getValues()[this.scalename];
    },

    getVariableChooserPanel : function() {

        if (this.variableChooserPanel) {
            return this.variableChooserPanel;
        }

        Ext.applyIf(this.measureConfig, {
            includeTimpointMeasures: false,
            allColumns: true,
            showHidden: false,
            multiSelect: true,
            flex: 1,
            sourceCls: undefined
        });

        Ext.apply(this.measureConfig, {
            sourceGroupHeader : 'Datasets',
            measuresAllHeader : 'All columns for this assay',
            getAdditionalMeasuresArray : this.getAdditionalMeasuresArray,
            getSessionMeasures: this.getSessionMeasures,
            bubbleEvents: ['beforeMeasuresStoreLoad', 'measuresStoreLoaded', 'measureChanged']
        });

        var panel = Ext.create('LABKEY.app.panel.MeasurePicker', this.measureConfig);

        // allows for a class to be added to the source selection panel
        if (Ext.isString(this.measureConfig.sourceCls)) {
            panel.getMeasurePicker().getSourcesView().on('afterrender', function(p) {
                p.addCls(this.measureConfig.sourceCls);
            }, this, {single: true});
        }

        this.variableChooserPanel = panel;
        return this.variableChooserPanel;
    },

    getAdditionalMeasuresArray : function() {
        return !this.includeTimpointMeasures ? [] : Connector.getService('Query').getTimeMeasures();
    },

    getSessionMeasures : function() {
        var cols = [];
        Ext.iterate(Connector.getState().getSessionColumns(), function(k,col) {
            cols.push(col);
        });
        return cols;
    },

    getMeasurePicker : function() {
        return this.getVariableChooserPanel().getMeasurePicker();
    },

    hasSelection : function() {
        return this.getSelection().length > 0;
    },

    getSelection : function() {
        return this.getMeasurePicker().getSelectedRecords();
    },

    getLookups : function() {
        return this.getSelectionDisplay().getLookups();
    },

    setSelection : function(measure) {
        this.getMeasurePicker().setSelectedRecord(measure);
    },

    clearSelection : function() {
        this.getMeasurePicker().clearSelection();
    },

    onMeasureClick : function(view, measure, element, index) {
        this.updateDefinition(measure);
        this.getSelectionDisplay().setVariableOptions(measure, this.selectedSource);
        this.lastMeasure = measure;

        this.handleMeasureHighlight(true, element);
    },

    onMeasureSelect : function(selModel, measure) {

        if (selModel.multiSelect && this.showDisplay)
        {
            this.updateDefinition(selModel.getLastSelected());
        }
        else if (this.showDisplay && this.disableLookups)
        {
            this.getSelectionDisplay().setMeasureSelection(measure);
            this.getSelectionDisplay().setVariableOptions(measure, this.selectedSource);
            this.getSelectionDisplay().getScaleForm().setVisible(measure.get('variableType') == null && measure.get('type') !== 'VARCHAR' && measure.get('type') !== 'TIMESTAMP');
            this.lastMeasure = measure;
        }

        this.handleMeasureHighlight(false, measure);
    },

    handleMeasureHighlight : function(isClick, element) {
        if (isClick)
        {
            if (element)
            {
                var _el = Ext.get(element);
                if (_el)
                {
                    if (_el.hasCls('x-grid-row-focused'))
                    {
                        // for some reason itemclick fires in the case of unchecking but not
                        // checking. This will stop us from removing highlight when someone
                        // unchecked a row
                        Ext.defer(function() {
                            var el = Ext.get(this.lastElementId);
                            if (el)
                            {
                                el.addCls('highlight');
                            }
                        }, 1, this);
                    }
                    else if (this.lastElementId != element.id)
                    {
                        // clear the former element highlight
                        var el = Ext.get(this.lastElementId);
                        if (el)
                        {
                            el.removeCls('highlight');
                        }

                        this.lastElementId = element.id;

                        el = Ext.get(element.id);
                        if (el)
                        {
                            el.addCls('highlight');
                        }
                    }
                }
            }
            else
            {
                console.warn('No element available onClick');
            }
        }
        else
        {
            if (this.lastMeasure)
            {
                if (this.lastMeasure.id == element.id)
                {
                    if (this.lastElementId)
                    {
                        var el = Ext.get(this.lastElementId);
                        if (el)
                        {
                            el.addCls('highlight');
                        }
                    }
                }
            }
        }
    },

    onSourceClick : function(view, source) {
        this.updateDefinition(source);
    },

    onSourceSelect : function(selModel, sources) {

        this.getSelectionDisplay().clearVariableOptions();

        if (Ext.isArray(sources) && sources.length > 0)
        {
            this.selectedSource = sources[0];
            this.updateDefinition(this.selectedSource);

            this.down('button#gotoassaypage').hide();
        }

        //select first variable in the list on source selection change, for singleSelect grid
        var measureGrid = this.getMeasurePicker().getMeasuresGrid();
        if (!measureGrid.multiSelect)
            measureGrid.getSelectionModel().select(0);
    },

    updateDefinition : function(record) {

        if (record.id != this.lastDefinitionId)
        {
            this.lastDefinitionId = record.id;

            var display = this.getSelectionDisplay();

            display.getDefinitionPanel().update({
                label: record.get("label") || record.get("queryLabel"),
                description: record.get("description")
            });

            display.show();
        }
    },

    getVariableOptionValues : function() {
        return this.getSelectionDisplay().getVariableOptionValues();
    }
});

Ext.define('Connector.panel.AxisSelectDisplay', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.axisselectdisplay',

    ui: 'custom',

    border: false,

    frame: false,

    disableLookups: true,

    disableAntigenFilter: true,

    layout: {
        type: 'vbox'
    },

    lookupMap: {},

    initComponent : function() {

        this.items = [{
            xtype: 'panel',
            layout: 'hbox',
            width: '100%',
            border: false,
            bodyStyle: 'background-color: transparent;',
            items: [{
                xtype: 'panel',
                width: '50%',
                bodyStyle: 'background-color: transparent;',
                border: false,
                items: [
                    this.getDefinitionPanel(),
                    {
                        xtype: 'button',
                        text: 'go to assay page',
                        itemId: 'gotoassaypage'
                    },
                    {
                        xtype: 'container',
                        height: 55,
                        padding: '10px 0 0 0',
                        layout: { type: 'hbox' },
                        items: [ this.getScaleForm() ]
                    }
                ]
            },{
                xtype: 'panel',
                itemId: 'variableoptions',
                cls: 'variableoptions',
                width: '50%',
                bodyStyle: 'background-color: transparent;',
                padding: '10px 5px 0 5px',
                items: [] // items to be added/removed as variables are selected
            }]
        }];

        this.callParent();
    },

    getDefinitionPanel : function() {
        if (!this.definitionPanel)
        {
            this.definitionPanel = Ext.create('Ext.panel.Panel', {
                border: false,
                ui: 'custom',
                cls: 'definitionpanel iScroll',
                height: !this.disableScale ? 140 : 180,
                bodyStyle: 'background-color: transparent;',
                padding: '10px 5px 5px 0',
                data: {},
                tpl: new Ext.XTemplate(
                        '<div class="definition-overflow"></div>',
                        '<div class="curselauth" style="width: 100%;">Definition: {label:htmlEncode}</div>',
                        '<div class="curseldesc" style="width: 100%;">{description:htmlEncode}</div>'
                )
            });
        }

        return this.definitionPanel;
    },

    getScaleForm : function() {
        if (!this.scaleForm) {
            this.scaleForm = Ext.create('Ext.form.Panel', {
                ui: 'custom',
                hidden: this.disableScale,
                border: false, frame : false,
                items: [{
                    xtype: 'radiogroup',
                    itemId: 'scale',
                    ui: 'custom',
                    border: false, frame : false,
                    width : 150,
                    fieldLabel: 'Scale',
                    labelAlign: 'top',
                    labelCls: 'curselauth',
                    items: [
                        { boxLabel: 'Log', name : this.scalename, inputValue: 'log', width: 80 },
                        { boxLabel: 'Linear', name : this.scalename, inputValue: 'linear', checked : true }
                    ]
                }]
            });
        }

        return this.scaleForm;
    },

    setMeasureSelection : function(record) {
        if (record)
            this.setDefaultScale(record);
    },

    setDefaultScale : function(record) {
        var value = {};
        value[this.scalename] = record.get('defaultScale').toLowerCase();
        this.getScaleForm().getComponent('scale').setValue(value);
    },

    getVariableOptionsPanel : function() {
        return this.down('#variableoptions');
    },

    clearVariableOptions : function() {
        this.getVariableOptionsPanel().removeAll(false);

        // reset the source specific variable options
        this.alignmentForm = null;
        this.assayOptionsPanel = null;
        this.antigensGrid = null;
        this.userGroupsGrid = null;
    },

    setVariableOptions : function(measure, source) {
        if (measure && measure.id != this.lastMeasureId)
        {
            this.lastMeasureId = measure.id;

            var optionsPanel = this.getVariableOptionsPanel();

            if (measure.get('variableType') == 'TIME')
            {
                optionsPanel.add(this.getAlignmentForm());
            }
            else if (measure.get('variableType') == 'USER_GROUPS')
            {
                optionsPanel.add(this.getVariableOptionsTitlePanel(measure.get('label')));
                optionsPanel.add(this.getUserGroupsGrid());
            }
            else if (!this.disableLookups)
            {
                this.clearVariableOptions();

                if (Ext.isDefined(measure.get('lookup').queryName))
                {
                    optionsPanel.add(this.getVariableOptionsTitlePanel(measure.get('name') + ' details'));
                    optionsPanel.add(this.getLookupGrid(measure));
                }
            }
            else
            {
                this.determineAssayOptionsPanel(optionsPanel, measure, source);
            }
        }
    },

    getLookupGrid : function(measure) {
        return {
            xtype: 'grid',
            viewConfig : { stripeRows : false },
            selType: 'checkboxmodel',
            selModel: {
                checkOnly: true,
                checkSelector: 'td.x-grid-cell-row-checker'
            },
            enableColumnHide: false,
            enableColumnResize: false,
            multiSelect: true,
            border: false,
            store: this.getLookupColumnStore(measure),
            ui: 'custom',
            height: 175,
            cls: 'measuresgrid iScroll lookupgrid variableoptionsgrid',
            flex: 1,
            hideHeaders: true,
            columns: [{
                dataIndex: 'shortCaption',
                width: '100%',
                flex: 1,
                sortable: false,
                menuDisabled: true
            }],
            listeners: {
                boxready: function(grid) {
                    var store = grid.getStore();
                    if (store.isLoading())
                    {
                        grid.getStore().on('load', function()
                        {
                            this.applyLookupSelections(grid, store);
                        }, this);
                    }
                    else
                    {
                        this.applyLookupSelections(grid, store);
                    }
                },
                select: this.onLookupSelect,
                deselect: this.onLookupDeselect,
                scope: this
            }
        };
    },

    onLookupSelect : function(selModel, record, ix) {

        if (Ext.isDefined(this.boundColumn))
        {
            var alias = this.boundColumn.get('alias');
            if (!this.lookupMap[alias])
            {
                this.lookupMap[alias] = {};
            }

            this.lookupMap[alias][record.get('fieldKeyPath')] = {
                fieldKeyPath: record.get('fieldKeyPath')
            };

            this.fireEvent('lookupselect', this, record);
        }
    },

    onLookupDeselect : function(selModel, record, ix) {

        var alias = this.boundColumn.get('alias');

        if (this.lookupMap[alias])
        {
            if (Ext.isDefined(this.lookupMap[alias][record.get('fieldKeyPath')])) {
                delete this.lookupMap[alias][record.get('fieldKeyPath')];
            }
        }
    },

    applyLookupSelections : function(grid, lookupStore) {

        var lookupSelections = this.lookupMap[this.boundColumn.get('alias')];

        if (Ext.isDefined(grid) && Ext.isDefined(lookupSelections))
        {
            var selectedLookups = [];

            Ext.iterate(lookupSelections, function(fieldKeyPath, value)
            {
                var idx = lookupStore.findExact('fieldKeyPath', fieldKeyPath);
                if (idx > -1)
                {
                    selectedLookups.push(lookupStore.getAt(idx));
                }
            }, this);

            grid.getSelectionModel().select(selectedLookups, false, true);
        }
    },

    getLookups : function() {
        return this.lookupMap;
    },

    getLookupColumnStore : function(boundColumn) {

        this.boundColumn = boundColumn;
        var lookup = boundColumn.get('lookup');

        var storeId = "fkColumns-" +  lookup.schemaName + "-" + lookup.queryName + "-" + lookup.keyColumn;

        var store = Ext.getStore(storeId);
        if (null != store) {
            return store;
        }

        var url = LABKEY.ActionURL.buildURL("query", "getQueryDetails", null, {
            schemaName: lookup.schemaName,
            queryName: lookup.queryName
        });

        return Ext.create('Ext.data.Store', {
            model   : 'Connector.model.ColumnInfo',
            storeId : storeId,
            proxy   : {
                type   : 'ajax',
                url    : url,
                reader : {
                    type: 'json',
                    root: 'columns'
                }
            },
            filterOnLoad: true,   //Don't allow user to select hidden cols or the display column (because it is already being displayed)
            filters: [function(item) {
                return !item.raw.isHidden; //&& item.raw.name != displayColFieldKey;
            }],
            autoLoad: true
        });
    },

    getVariableOptionsTitlePanel : function(title) {
        return Ext.create('Ext.Component', {
            cls: 'curselauth',
            autoEl: {
                tag: 'div',
                html: Ext.htmlEncode(title)
            }
        });
    },

    getAlignmentForm : function() {
        if (!this.alignmentForm)
        {
            // the default option is 'Unaligned'
            var visitTagRadios = [{
                name : 'alignmentVisitTag',
                boxLabel : 'Unaligned',
                inputValue : null,
                checked : true
            }];

            if (this.visitTagStore)
            {
                Ext.each(this.visitTagStore.getRange(), function(record){
                    visitTagRadios.push({
                        name : 'alignmentVisitTag',
                        boxLabel: record.get('Caption'),
                        inputValue: record.get('Name')
                    })
                });
            }

            this.alignmentForm = Ext.create('Ext.form.Panel', {
                ui: 'custom',
                border: false, frame: false,
                width: '100%',
                padding: '0 0 10px 5px',
                items: [{
                    xtype: 'radiogroup',
                    itemId: 'alignment',
                    fieldLabel: 'Align multiple studies by',
                    labelAlign: 'top',
                    labelCls: 'curselauth',
                    height: 175,
                    autoScroll: true,
                    cls: 'iScroll',
                    border: false, frame : false,
                    vertical: true,
                    columns: 1,
                    items: visitTagRadios
                }]
            });
        }

        return this.alignmentForm;
    },

    hasUserGroupsGrid : function() {
        return this.userGroupsGrid != undefined;
    },

    getUserGroupsGrid : function() {
        if (!this.userGroupsGrid)
        {
            var store = Connector.model.Group.getGroupStore();

            this.userGroupsGrid =  Ext.create('Ext.grid.Panel', {
                viewConfig : {
                    stripeRows : false,
                    emptyText: 'none'
                },
                cls: 'measuresgrid iScroll variableoptionsgrid',
                border: false,
                height: 175,
                forceFit: true,
                hidden: true,
                enableColumnResize: false,
                enableColumnMove: false,
                multiSelect: true,
                selType: 'checkboxmodel',
                selModel: {
                    checkOnly: true,
                    checkSelector: 'td.x-grid-cell-row-checker'
                },
                store: store,
                columns: [{
                    header: 'All',
                    dataIndex: 'label',
                    menuDisabled: true,
                    sortable: false,
                    renderer: Ext.String.htmlEncode
                }]
            });

            store.on('load', function() {
                this.selectAllInGrid(this.userGroupsGrid);
            }, this, {single: true});

            store.load();

            this.addDescriptionHoverListeners(this.userGroupsGrid, 'label', 'description');
        }

        return this.userGroupsGrid;
    },

    addDescriptionHoverListeners : function(grid, labelFieldName, descFieldName) {
        grid.on('itemmouseenter', function(grid, record, item){
            this.previousDefinitionData = this.getDefinitionPanel().data;

            this.getDefinitionPanel().update({
                label: record.get(labelFieldName),
                description: record.get(descFieldName)
            });
        }, this);

        grid.on('itemmouseleave', function(grid, record, item){
            this.getDefinitionPanel().update(this.previousDefinitionData);
            this.previousDefinitionData = null;
        }, this);
    },

    determineAssayOptionsPanel : function(parentPanel, measure, source) {
        // the first time a source is selected, determine if it contains a lookup for Antigen and StudyDesignAssays
        if (source.antigenLookup === undefined || source.assaysLookup === undefined)
        {
            source.antigenLookup = null;
            source.assaysLookup = null;

            LABKEY.Query.getQueryDetails({
                schemaName: source.get('schemaName'),
                queryName: source.get('queryName'),
                scope: this,
                success: function(data){
                    Ext.each(data.columns, function(col){
                        if (Ext.isDefined(col.lookup) && col.lookup.schemaName == 'CDS' && col.lookup.queryName == 'Antigens')
                            source.antigenLookup = col;

                        if (Ext.isDefined(col.lookup) && col.lookup.schemaName == 'study' && col.lookup.queryName == 'StudyDesignAssays')
                            source.assaysLookup = col;
                    });

                    parentPanel.add(this.getAssayOptionsPanel(source));
                }
            });
        }
        else if (source.antigenLookup != null || source.assaysLookup != null)
        {
            parentPanel.add(this.getAssayOptionsPanel(source));
        }
    },

    getAssayOptionsPanel : function(source) {
        if (!this.assayOptionsPanel)
        {
            this.assayOptionsPanel = Ext.create('Ext.panel.Panel', { items: [] });

            if (!this.disableAntigenFilter && source.antigenLookup != null)
            {
                this.assayOptionsPanel.add(this.getVariableOptionsTitlePanel('Antigens'));
                this.assayOptionsPanel.add(this.getAntigensGrid(source));
            }
        }

        // issue 20664: only show for datasets with assays lookup
        var assaybtn = this.down('button#gotoassaypage');
        if (assaybtn) {
            assaybtn.setVisible(source.assaysLookup != null);
            assaybtn.source = source;
        }

        return this.assayOptionsPanel;
    },

    getAntigensGrid : function(source) {
        if (!this.antigensGrid)
        {
            this.antigensGrid = this.getAssayValueGrid(source, 'antigenLookup');
        }

        return this.antigensGrid;
    },

    hasAntigensGrid : function() {
        return this.antigensGrid != undefined;
    },

    getAssayValueGrid : function(source, lookupProperty) {
        var store = Ext.create('Connector.store.AssayDistinctValue', {
            schemaName: source.get('schemaName'),
            queryName: source.get('queryName'),
            colName: source[lookupProperty].name
        });

        var grid = Ext.create('Ext.grid.Panel', {
            viewConfig : { stripeRows : false },
            cls: 'measuresgrid iScroll variableoptionsgrid',
            border: false,
            height: 175,
            forceFit: true,
            hidden: true,
            enableColumnResize: false,
            enableColumnMove: false,
            multiSelect: true,
            emptyText: 'none',
            selType: 'checkboxmodel',
            selModel: {
                checkOnly: true,
                checkSelector: 'td.x-grid-cell-row-checker'
            },
            store: store,
            columns: [{
                header: 'All',
                dataIndex: 'Name',
                menuDisabled: true,
                sortable: false,
                renderer: Ext.String.htmlEncode
            }],
            valueColumn: source[lookupProperty]
        });

        this.addDescriptionHoverListeners(grid, 'Name', 'Description');

        store.on('load', function() {
            this.selectAllInGrid(grid);
        }, this);

        return grid;
    },

    selectAllInGrid : function(grid) {
        if (grid.getStore().getCount() > 0)
        {
            grid.getSelectionModel().selectAll();
            grid.getView().focusRow(0);
        }

        if (grid.getStore().getCount() < 2)
            grid.getEl().query('.x-grid-header-ct')[0].style.display = 'none';

        grid.show();
    },

    getVariableOptionValues : function() {
        var values = {};

        if (this.getAlignmentForm() && this.getAlignmentForm().isVisible())
        {
            values = this.getAlignmentForm().getForm().getFieldValues();

            // manually set to null for 'Unaligned' case
            if (!values.alignmentVisitTag)
                values = {alignmentVisitTag : null};

            // get the caption for the visit tag and add it to the values for the sublabel display
            var record = this.visitTagStore.findRecord('Name', values.alignmentVisitTag, 0, false, false, true);
            values.alignmentVisitTagLabel = record ? record.get('Caption') : 'Unaligned';
        }

        if (this.hasUserGroupsGrid() && this.getUserGroupsGrid().isVisible())
        {
            var groupsArr = [];
            Ext.each(this.getUserGroupsGrid().getSelectionModel().getSelection(), function(sel){
                groupsArr.push(sel.get('id'));
            });

            if (groupsArr.length > 0)
                values.userGroups = groupsArr;
        }

        if (this.hasAntigensGrid() && this.getAntigensGrid().isVisible())
        {
            var antigensArr = [];
            Ext.each(this.getAntigensGrid().getSelectionModel().getSelection(), function(sel){
                antigensArr.push(sel.get('Name'));
            });

            if (antigensArr.length > 0)
            {
                values.antigen = {
                    name: this.getAntigensGrid().valueColumn.name,
                    values: antigensArr
                };
            }
        }

        return values;
    }
});
