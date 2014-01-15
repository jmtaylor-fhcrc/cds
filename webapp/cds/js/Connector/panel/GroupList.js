/*
 * Copyright (c) 2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext4.define('Connector.panel.GroupList', {

    extend : 'Ext.Panel',

    alias  : 'widget.grouplist',

    cls : 'grouplist',

    initComponent : function() {

        this.defaults = {
            plain  : true,
            ui : 'custom'
        };

        this.items = [];

        this.items.push(this.getGroupListView());

        this.callParent();
    },

    getGroupListView : function() {
        if (this.grouplist)
            return this.grouplist;

        this.grouplist = Ext4.create('Connector.view.GroupListView',{
            arrow: this.arrow
        });

        return this.grouplist;
    }
});

Ext4.define('Connector.view.GroupListView', {

    extend : 'Ext.view.View',

    alias : 'widget.grouplistview',

    trackOver : true,

    emptyText : '<div class="emptytext"><span class="left-label">No groups defined</span>',

    initComponent : function() {

        this.cls         = 'grouplist-view';
        this.overItemCls = 'grouplist-label-over';
        this.selectedItemCls = 'grouplist-label-selected '+ this.arrow;

        this.tpl = new Ext4.XTemplate(
            '<tpl for=".">',
                '<div class="nav-label">',
                    '{label:this.renderContent}',
                '</div>',
            '</tpl>'
        );

        var me = this;

        this.tpl.renderContent = function(val) {
            var ret = '';
            if (me.arrow == 'left'){
                ret += '<span class="' + me.arrow +'-arrow"></span>';
                ret += '<span class="right-label">' + Ext4.String.ellipsis(Ext4.htmlEncode(val), 20) + '</span>';
            } else if (me.arrow == 'right'){
                ret += '<span class="left-label">' + Ext4.String.ellipsis(Ext4.htmlEncode(val), 20) + '</span>';
                ret += '<span class="' + me.arrow +'-arrow"></span>';
            }
            return ret;
        }

        this.itemSelector = 'div.nav-label';

        // models Participant Groups and Cohorts mixed
        Ext4.define('LABKEY.study.GroupCohort', {
            extend : 'Ext.data.Model',
            fields : [
                {name : 'id'},
                {name : 'label'},
                {name : 'description'},
                {name : 'filters'},
                {name : 'type'}
            ]
        });

        var storeConfig = {
            pageSize : 100,
            model    : 'LABKEY.study.GroupCohort',
            autoLoad : true,
            proxy    : {
                type   : 'ajax',
                url    : LABKEY.ActionURL.buildURL('participant-group', 'browseParticipantGroups.api'),
                reader : {
                    type : 'json',
                    root : 'groups'
                }
            },
            listeners : {
                load : function(s, recs) {
                    for (var i=0; i < recs.length; i++)
                    {
                        if (recs[i].data.id < 0)
                            s.remove(recs[i]);
                    }
                }
            }
        };

        var groupConfig = Ext4.clone(storeConfig);
        Ext4.apply(groupConfig.proxy, {
            extraParams : { type : 'participantGroup'}
        });

        this.store = Ext4.create('Ext.data.Store', groupConfig);

        this.callParent();
    }
});