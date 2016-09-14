Ext.define('Connector.plugin.DocumentValidation', {
    extend: 'Ext.AbstractPlugin',

    alias: 'plugin.documentvalidation',

    init : function(component) {

        Ext.override(component, {
            validateDocLinks: this.validateDocLinks
        });
    },

    validateDocLinks : function (docList, data, callback) {
        var allExternal = true;
        for (var itr = 0; itr < docList.length; itr++) {
            const doc = docList[itr];
            if (!doc.isExternal) {
                allExternal = false;
                if (!doc.isLinkValid) {
                    LABKEY.Ajax.request({
                        method: 'HEAD',
                        url: doc.fileName,
                        success: LABKEY.Utils.getCallbackWrapper(function (json, response) {
                            if (200 === response.status) {
                                doc.isLinkValid = true;
                            }
                            callback.call(this);
                        }, this),
                        scope: this
                    });
                }
            }
        }
        if (allExternal) {
            callback.call(this);
        }
    }
});