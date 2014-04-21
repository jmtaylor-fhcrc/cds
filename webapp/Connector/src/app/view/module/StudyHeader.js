/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.module.StudyHeader', {

    xtype : 'module.studyheader',

    extend : 'Connector.view.module.BaseModule',

    tpl : new Ext.XTemplate(
        '<tpl>',
            '<p>Network: {[values.model.get("Network")]}</p>',
            '<p>Study Type: {[this.typeString(values.model)]}</p>',
            '<tpl if="model.get(\'StartDate\')">',
                '<p>Started: {[Connector.app.view.Study.dateRenderer(values.model.get("StartDate"))]}</p>',
            '</tpl>',
            '<tpl if="model.get(\'EndDate\')">',
                '<p>Ended: {[Connector.app.view.Study.dateRenderer(values.model.get("EndDate"))]}</p>',
            '</tpl>',
        '</tpl>',
    {
        typeString : function(model) {
            var phase = model.get('Phase');
            var type = model.get('Type');
//            var start = model.get('StartDate');
//            var end = model.get('EndDate');
            var s = '';
            if (phase) {
                s = "Phase " + phase + " ";
            }
            if (type) {
                s += type;
            }
            return s;
        },
    })
});
