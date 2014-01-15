/*
 * Copyright (c) 2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext4.define('Connector.model.State', {

    extend : 'Ext.data.Model',

    requires : [
        'Connector.types.Filter'
    ],

    fields : [
        {name : 'activeView'},
        {name : 'viewState'},
        {name : 'views'},
        {name : 'filters'}, //,    type : Ext4.data.Types.FILTER},
        {name : 'selections'},
        {name : 'detail'}
    ],

    proxy : {
        type : 'sessionstorage',
        id   : 'connectorStateProxy'
    }
});