Ext.define('Connector.view.MabReport', {
    extend : 'Ext.container.Container',

    alias: 'widget.mabreportview',

    reportHeaderHeight: 56 + 58, // page header + report title

    rightSideNavWidth: 244,

    initComponent : function() {
        if (this.parentGrid) {
            this.parentGrid.on('resize', this.resizeReport, this, {buffer: 200});
        }
        this.items = [this.getHeaderItem(), this.getReportResult()];
        this.callParent();
    },

    getHeaderItem: function() {
        return {
            xtype: 'box',
            width: '100%',
            cls: 'mabreportheader learnheader',
            flex: 1,
            renderTpl: new Ext.XTemplate(
                    '<div class="title-and-back-panel">',
                    '<div class="iarrow">&nbsp;</div>',
                    '<div class="breadcrumb">Monoclonal antibodies / </div>',
                    '<div class="studyname">{title:htmlEncode}</div>',
                    '</div>'
            ),
            renderData: {
                title: this.reportLabel
            },
            renderSelectors: {
                upEl: 'div.title-and-back-panel'
            },
            listeners: {
                afterrender: {
                    fn: function (cmp) {
                        cmp.upEl.on('click', this._onBackClick, this);
                    },
                    single: true,
                    scope: this
                }
            }
        }
    },

    _onBackClick: function(){
        this.hide();
        if (this.parentGrid) {
            this.parentGrid.showGridView(true);
            this.parentGrid.remove(this);
        }
    },

    getReportResult: function() {
        var me = this;
        return {
            xtype: 'box',
            cls: 'reportViewMAb',
            width: '100%',
            html: '<div id="mabreportrenderpanel"></div>',
            listeners: {
                render : function(cmp){
                    me.reportPanel = cmp;
                    var url = LABKEY.ActionURL.buildURL('reports', 'viewScriptReport', LABKEY.container.path, {
                        reportId: this.reportId,
                        filteredKeysQuery: this.filteredKeysQuery,
                        filteredDatasetQuery: this.filteredDatasetQuery
                    });
                    cmp.getEl().mask("Generating " + this.reportLabel);
                    Ext.Ajax.request({
                        url : url,
                        method: 'POST',
                        success: function(resp){
                            cmp.getEl().unmask();
                            var json = LABKEY.Utils.decode(resp.responseText);
                            if (!json || !json.html)
                                Ext.Msg.alert("Error", "Unable to load " + this.reportLabel + ". The report doesn't exist or you may not have permission to view it.");
                            LABKEY.Utils.loadAjaxContent(resp, 'mabreportrenderpanel', function() {
                                me.resizeReport();
                            });
                        },
                        failure : function() {
                            Ext.Msg.alert("Error", "Failed to load " + this.reportLabel);
                        },
                        scope : this
                    });
                },
                resize : function() {
                    this.resizeReport();
                },
                scope: this
            }
        }
    },

    resizeReport: function() {
        if (this.reportPanel && !this.isHidden()) {
            var box = Ext.getBody().getBox();
            this.reportPanel.setWidth(box.width - this.rightSideNavWidth);
            this.reportPanel.setHeight(box.height - this.reportHeaderHeight);
        }
    }
});
