Ext.define('Connector.Application', {
    name: 'Connector',

    extend: 'Ext.app.Application',

    requires: [
        'Connector.app.model.Assay',
        'Connector.app.model.Labs',
        'Connector.app.model.Site',
        'Connector.app.model.Study',
        'Connector.app.model.StudyProducts',

        'Connector.app.store.Assay',
        'Connector.app.store.Labs',
        'Connector.app.store.Site',
        'Connector.app.store.Study',
        'Connector.app.store.StudyProducts',

        'Connector.app.view.Assay',
        'Connector.app.view.Labs',
        'Connector.app.view.Site',
        'Connector.app.view.Study',
        'Connector.app.view.StudyProducts'
    ],

    views: [
        'Header',
        'Main',
        'Viewport'
    ],

    controllers: [
        'Connector', // View Manager must be registered first to properly initialize
        'State',
        'Main',
        'Router',
        'Citation',
        'Chart',
        'Explorer',
        'FilterStatus',
        'Learn',
        'Navigation',
        'RawData',
        'Summary'
    ],

    stores: [
        // TODO: add stores here
    ]
});
