/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.window.Facet', {

    extend: 'Connector.window.AbstractFilter',

    alias: 'widget.columnfacetwin',

    bodyStyle: 'overflow-y: auto; padding: 10px;',

    width: 290,

    height: 375,

    getItems : function()
    {
        var model = this.dataView.getModel(),
            wrappedMeasures = model.getWrappedMeasures(),
            fieldAlias = this.columnMetadata.filterField.toLowerCase(),
            matchFilters = [],
            newMeasures = [];

        var loader = Ext.create('Ext.Component', {
            html: 'Loading...'
        });

        // Include all measures in the request removing the matching filters
        Ext.each(wrappedMeasures, function(wrapped)
        {
            var wrappedAlias = wrapped.measure.alias.toLowerCase();

            // base measures should not show up in filtering (e.g. subject filter)
            if (!wrapped.isBaseMeasure)
            {
                if (wrappedAlias === fieldAlias)
                {
                    var newMeasure = {
                        measure: Ext.clone(wrapped.measure)
                    };

                    if (wrapped.dateOptions)
                    {
                        newMeasure.dateOptions = Ext.clone(wrapped.dateOptions);
                    }

                    Ext.each(wrapped.filterArray, function(f)
                    {
                        matchFilters.push(f);
                    });

                    newMeasures.push(newMeasure);
                }
                else
                {
                    newMeasures.push(wrapped);
                }
            }
        });

        var datasource = model.getDataSource();
        var isDemographicsOnlyQuery = datasource === QueryUtils.DATA_SOURCE_SUBJECT_CHARACTERISTICS;
        var extraFilters = isDemographicsOnlyQuery ? model.getDemographicsSubjectFilters() : model.get('extraFilters');

        Connector.getQueryService().getData(newMeasures, function(metadata) {
                this.remove(loader);

                var faceted = Ext.create('LABKEY.dataregion.filter.Faceted', {
                    itemId: 'faceted',
                    border: false,
                    useGrouping: true,
                    useStoreCache: false,
                    filters: matchFilters,
                    groupFilters: model.getFilterArray(true),
                    model: {
                        column: this.columnMetadata,
                        schemaName: metadata.schemaName,
                        queryName: metadata.queryName
                    }
                });

                this.add(faceted);
            },
            function() {
                console.log('Failed to load...');
            },
            this,
            extraFilters,
            {
                dataSource: datasource,
                demographicsOnly: isDemographicsOnlyQuery
            }
        );

        return [loader];
    },

    onAfterRender : function() {
        this.callParent(arguments);
        if (this.hasFilters) {
            this.getButton('dofilter').setText('Update');
        }
    },

    applyFiltersAndColumns : function()
    {
        var view = this.getComponent('faceted');
        if (view.checkValid())
        {
            this.fireEvent('filter', this, view.getModel().get('column'), view.getOriginalFilters(), view.getFilters());
            this.close();
        }
    },

    onClear : function() {
        var column = this.columnMetadata;
        var fieldKeyPath = column.displayField ? column.displayField : column.fieldKeyPath;

        this.fireEvent('clearfilter', this, fieldKeyPath);
        this.close();
    }
});
