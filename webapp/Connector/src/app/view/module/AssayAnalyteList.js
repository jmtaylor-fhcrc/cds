/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.module.AssayAnalyteList', {

    xtype : 'app.module.assayanalytelist',

    extend : 'Connector.view.module.BaseModule',

    cls : 'module assaylist',

    tpl : new Ext.XTemplate(
            '<tpl>',
                '<h3>Assay Analytes</h3>',
                '<tpl if="results.length &gt; 0">',
                    '<table class="learn-study-info">',
                        '<tpl for="results">',
                            '<tr>',
                                '<td class="item-label">{col:htmlEncode}:</td>',
                                '<td class="item-value">{value:htmlEncode}</td>',
                            '</tr>',
                        '</tpl>',
                        '<tr>',
                            '<td class="item-label">{antigen_display_name:htmlEncode} Name:</td>',
                            '<td class="item-value"><a href="{antigen_link:htmlEncode}">{antigen_display_name:htmlEncode} List</td>',
                        '</tr>',
                    '</table>',
                '<tpl else>',
                    '<p>There are no Analytes to Display</p>',
                '</tpl>',
            '</tpl>'
    ),

    assayNameToAnalytesMap : {
        "BAMA" : [{label: "Dilution", name: "dilution"},
            {label: "Detection System", name: "detection_ligand"},
            {label: "Instrument Code", name: "instrument_code"},
            {label: "Specimen type", name: "specimen_type"},
            {label: "Lab ID", name: "lab_code"}],
        "ELISPOT" : [{label: "Functional marker name", name: "functional_marker_name"},
            {label: "Specimen type", name: "specimen_type"},
            {label: "Lab ID", name: "lab_code"}],
        "ICS" : [{label: "Cell type", name: "cell_type"},
            {label: "Functional marker name", name: "functional_marker_name"},
            {label: "Specimen type", name: "specimen_type"},
            {label: "Lab ID", name: "lab_code"}],
        "NAb" : [{label: "Initial dilution", name: "initial_dilution"},
            {label: "Specimen type", name: "specimen_type"},
            {label: "Lab ID", name: "lab_code"}]
    },

    initComponent : function() {
        var store = StoreCache.getStore('Connector.app.store.Assay'),
                assay_id = this.data.model.get('assay_identifier'),
                assay_type = this.data.model.get('assay_type');

        store.loadAnalytes(assay_type, this.assayNameToAnalytesMap[assay_type], function(results) {
            this.update({
                results: results,
                antigen_display_name: assay_type == "NAb" ? 'Virus' : 'Antigen',
                antigen_link: Connector.getService('Learn').getURL('Assay', assay_id) + '/antigens'
            });
        }, this);
    }
});
