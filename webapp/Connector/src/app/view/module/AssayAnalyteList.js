/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.module.AssayAnalyteList', {

    xtype : 'app.module.assayanalytelist',

    extend : 'Connector.view.module.BaseModule',

    tpl : new Ext.XTemplate(
        '<tpl>',
            Connector.constant.Templates.module.title,
            'Analyte data goes here',
            // '<tpl if="model.get(\'Category\')"><p class="item-row">Category: {[values.model.get("Category")]}</p></tpl>',
        '</tpl>')
});
