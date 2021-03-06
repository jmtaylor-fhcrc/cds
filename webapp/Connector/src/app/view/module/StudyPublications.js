/*
 * Copyright (c) 2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.module.StudyPublications', {

    xtype : 'app.module.studypublications',

    extend : 'Connector.view.module.BaseModule',

    plugins : ['documentvalidation'],

    tpl : new Ext.XTemplate(
            '<tpl if="publications && publications.length &gt; 0">',
                '<h3>{title_study_publications}</h3>',
                '<table class="learn-study-info">',
                    '<tpl for="publications">',
                        '<tr>',
                            '<td class="item-value">{authors:htmlEncode}. {title:htmlEncode}. {journal:htmlEncode}. {date:htmlEncode};{volume}',
                            '<tpl if="issue">',
                                '({issue:htmlEncode})',
                            '</tpl>',
                            ':{location:htmlEncode}. ',
                            '<tpl if="pmid">',
                                'PMID: {pmid:htmlEncode}. ',
                            '</tpl>',
                            '<tpl if="link">',
                                    '<a href="{link}" target="_blank">View publication <img src="' + LABKEY.contextPath + '/Connector/images/outsidelink.png' + '"/></a></td>',
                            '</tpl>',
                        '</tr>',
                    '</tpl>',
                '</table>',
            '</tpl>'
    ),

    initComponent : function() {
        this.callParent();

        var data = this.initialConfig.data.model.data;
        data['title_study_publications'] = this.initialConfig.data.title;

        this.update(data);
    },

    hasContent : function() {
        var pubs = this.initialConfig.data.model.data.publications;
        if (pubs) {
            return pubs.length > 0;
        }
        return false;
    }
});
