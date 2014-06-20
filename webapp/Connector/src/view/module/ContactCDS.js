/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.module.ContactCDS', {

    xtype : 'module.contactcds',

    extend : 'Connector.view.module.BaseModule',

    // Replace with contact CDS team link
    tpl : new Ext.XTemplate(
        '<tpl>',
            Connector.constant.Templates.module.title,
            '<div class="item-row">',
                '<a href=".">Contact the Collaborative DataSpace team</a> for more information<br/>',
            '</div>',
            '<div class="item-row">',
                '<a href="http://www.hvtn.org/en/science/submit-idea-proposal.html" target="_blank">Propose an ancillary study</a>',
            '</div>',
        '</tpl>')
});