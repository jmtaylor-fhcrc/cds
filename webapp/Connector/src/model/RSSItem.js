/* Models an RSS <item> element. See http://www.w3schools.com/rss/rss_item.asp */
Ext.define('Connector.model.RSSItem', {
    extend: 'Ext.data.Model',

    fields: [
        /* Required */
        {name: 'title'},
        {name: 'description'},
        {name: 'link'},

        /* Optional */
        {name: 'pubDate', type: 'date'},
        {name: 'guid'}
    ],

    proxy: {
        type: 'ajax',
        url: LABKEY.ActionURL.buildURL('cds', 'news.api'),
        reader: {
            type: 'xml',
            record: 'item',
            root: 'channel'
        }
    }
});