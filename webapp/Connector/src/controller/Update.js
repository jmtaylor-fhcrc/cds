Ext.define('Connector.controller.Update', {

    extend: 'Ext.app.Controller',

    isService: true,

    _ready: false,

    UPDATE_FLAG: 'dataUpdate',

    init : function () {

        if (LABKEY.devMode) {
            UPDATE = this;
        }

        var params = LABKEY.ActionURL.getParameters();

        if (Ext.isDefined(params[this.UPDATE_FLAG])) {
            Ext.create('Ext.window.Window', {
                title: 'Application Update',
                height: 400,
                width: 500,
                ui: 'axiswindow',
                cls: 'axiswindow',
                plain: true,
                draggable: false,
                preventHeader: true,
                resizable: false,
                autoShow: true,
                modal: true,
                items: [{
                    xtype: 'box',
                    autoEl: {
                        tag: 'div',
                        cls: 'curseltitle',
                        html: 'Completing updates'
                    }
                },{
                    xtype: 'button',
                    id: 'usreload',
                    text: 'Click to Complete',
                    margin: '0 0 5 0',
                    hidden: true,
                    handler : function() {
                        window.location = LABKEY.ActionURL.buildURL('cds', 'app'); // drop parameters
                    }
                }, this._getLogger()]
            });

            this.runUpdates();
        }
    },

    _getLogger : function() {
        if (!this.logger) {

            var model = Ext.define('Connector.model.UpdateLog', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'message', type: 'string'},
                    {name: 'error', type: 'boolean'}
                ]
            });

            this.logger = Ext.create('Ext.view.View', {
                id: 'update-service-log',
                itemSelector: 'div.uslog',
                store: Ext.create('Ext.data.Store', { model: 'Connector.model.UpdateLog' }),
                tpl: new Ext.XTemplate(
                    '<tpl for="."><div class="uslog" {error:this.asError}>{message:htmlEncode}</div></tpl>',
                    { asError : function(error) { return error ? 'style="color: red;"' : ''; } }
                )
            });
        }

        return this.logger;
    },

    log : function(msg, error) {
        if (Ext.isString(msg)) {
            this._getLogger().getStore().add({message: msg, error: error === true});
        }
    },

    runUpdates : function() {
        if (LABKEY.user.isAdmin !== true) {
            this.log('You must be an admin to complete updates.');
        }
        else {
            this.log('Running updates...');

            Connector.getState().onMDXReady(function(mdx) {
                Connector.getService('Query').onQueryReady(function(query) {
                    this.log('State/Query services ready.');

                    var stats = false, grps = false, me = this;
                    function success() {
                        if (stats && grps) {
                            me.log('Success!');
                            Ext.getCmp('usreload').show();
                        }
                    }

                    this.log('Updating Statistics...');
                    this.updateStatistics(mdx, function() {
                        this.log('Statistics complete.');
                        stats = true;
                        success();
                    }, function() {
                        this.log('Statistics failed.', true);
                    }, this);

                    // Update all Subjects Groups saved by users
                    this.log('Updating Subject Groups...');
                    this.updateGroups(mdx, function() {
                        this.log('Subject Groups update complete.');
                        grps = true;
                        success();
                    }, function() {
                        this.log('Subject Groups update failed.', true);
                    }, this);

                }, this);
            }, this);
        }
    },

    updateGroups : function(mdx, callback, failureFn, scope) {

        var me = this;

        var successTask = new Ext.util.DelayedTask(function() {
            if (Ext.isFunction(callback)) {
                callback.call(scope);
            }
        });

        var failureTask = new Ext.util.DelayedTask(function() {
            successTask.cancel();
            if (Ext.isFunction(failureFn)) {
                failureFn.call(scope);
            }
        });

        Ext.Ajax.request({
            url: LABKEY.ActionURL.buildURL('participant-group', 'getParticipantGroupsWithLiveFilters'),
            method: 'POST',
            success : function(response) {
                var obj = Ext.decode(response.responseText);
                var groups = obj['participantGroups'];
                if (Ext.isEmpty(groups)) {
                    me.log('No Subject Groups with live filters are defined.');
                    successTask.delay(0);
                }
                else {
                    function groupUpdated(group) {
                        var subjects = group.participantIds;
                        me.log('"' + Ext.htmlEncode(group.label) + '" now has ' + subjects.length + ' subjects.');

                        // not really...
                        successTask.delay(500);
                    }

                    try
                    {
                        for (var i = 0; i < groups.length; i++) {
                            LABKEY.app.model.Filter.doParticipantUpdate(mdx, groupUpdated, null, groups[i], 'Subject');
                        }
                    }
                    catch (error) {
                        me.log(error.message + '. See console.', true);
                        failureTask.delay(0);
                        throw error;
                    }
                }
            },
            failure : function() {
                failureTask.delay(0);
            },
            scope: me
        });
    },

    updateStatistics : function(mdx, callback, failureFn, scope) {

        var data = {
            primaryCount: -1,
            dataCount: -1
        };

        var me = this;
        function check() {
            if (data.primaryCount >= 0 && data.dataCount >= 0) {
                Statistics.update(data, callback, failureFn, scope);
            }
        }

        //
        // Retrieve Primary Count
        //
        var studyCountLvl = '[Study].[Name]';
        mdx.query({
            onRows: [{ level: studyCountLvl }],
            success : function(cellset) { data.primaryCount = cellset.cells.length; check(); },
            failure : function() {
                me.log('Statistics failed to update the number of studies. Used \"' + studyCountLvl + '\" level.', true);
                if (Ext.isFunction(failureFn)) {
                    failureFn.call(scope);
                }
            }
        });

        //
        // Retrieve Data Count
        //
        LABKEY.Query.selectRows({
            schemaName: 'study',
            queryName: 'StudyData',
            requiredVersion: 9.1,
            maxRows: 1,
            success: function(_data) { data.dataCount = _data.rowCount; check(); },
            failure : function() {
                me.log('Statistics failed to update the number of data points available. See console errors.', true);
                if (Ext.isFunction(failureFn)) {
                    failureFn.call(scope);
                }
            }
        });
    }
});