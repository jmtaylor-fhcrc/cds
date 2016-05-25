/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.Learn', {

    extend : 'Ext.container.Container',

    alias  : 'widget.learn',

    requires : ['Connector.model.Dimension'],

    cls: 'learnview auto-scroll-y',

    /**
     * Allows for search to do a depth search on properties that are setup. Example
     * ['label', 'type', 'description', {field: 'products', value: 'product_name'}]
     * where 'products' is a depth search that iterates across the 'products' and matches
     * against 'product_name'.
     * Since this could be an expensive operation it can be turned off with this flag.
     */
    allowNestedSearch: true,

    searchFilter: undefined,

    initComponent : function() {

        this.headerViews = {};
        this.listViews = {};

        // Multiple headers are added here but they are initially set to hidden. When the page
        // url changes the headers will be updated and made visible based on the type of view -
        // parent learn view or study/assay detail page etc
        this.items = [this.getHeader()];

        this.callParent();
    },

    getHeader : function() {
        if (!this.headerViews.main) {
            this.headerViews.main = Ext.create('Connector.view.LearnHeader', {
                dimensions: this.getDimensions(),
                hidden: true,
                cls: 'learnheader',
                searchValue: this.searchFilter,
                listeners: {
                    searchchanged: this.onSearchFilterChange,
                    scope: this
                }
            });
        }
        return this.headerViews.main;
    },

    onSearchFilterChange : function(filter) {
        if (Ext.isString(filter)) {
            this.searchFilter = filter;
            if (this.activeListing) {
                var view = this.activeListing;
                this.loadData(view.dimension, view.getStore());
            }
        }
    },

    dimensionDataLoaded : function(dimension, store) {
        store.clearFilter();

        if (!Ext.isEmpty(this.searchFilter)) {

            var fields = this.searchFields || [],
                regex = new RegExp(LABKEY.Utils.escapeRe(this.searchFilter), 'i'),
                allowNestedSearch = this.allowNestedSearch === true;

            store.filterBy(function(model) {
                var match = false,
                    value;
                Ext.each(fields, function(field) {
                    if (Ext.isString(field)) {
                        value = model.get(field);

                        if (regex.test(value)) {
                            match = true;
                        }
                    }
                    else if (allowNestedSearch && Ext.isObject(field)) {
                        value = model.get(field.field);
                        if (Ext.isArray(value)) {
                            if (Ext.isEmpty(value) && Ext.isString(field.emptyText)) {
                                if (regex.test(field.emptyText)) {
                                    match = true;
                                }
                            }
                            else {
                                for (var i=0; i < value.length; i++) {
                                    if (regex.test(value[i][field.value])) {
                                        match = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                });
                return match;
            });
        }
    },

    loadData : function(dimension, store) {
        if (dimension) {
            var hierarchy = dimension.getHierarchies()[0],
                dimensionName = hierarchy.getName();

            if (!this.dimensionDataLoaded[dimensionName]) {
                store.on('load', function() {
                    this.dimensionDataLoaded[dimensionName] = true;
                    this.dimensionDataLoaded(dimensionName, store);
                }, this);
                Connector.getState().onMDXReady(function(mdx) {
                    mdx.query({
                        onRows: [{
                            hierarchy: hierarchy.getName(),
                            member: 'members'
                        }],
                        success: function(slice) {
                            if (store) {
                                store.loadSlice(slice);
                            }
                        },
                        scope: this
                    });
                }, this);
            }
            else {
                this.dimensionDataLoaded(dimensionName, store);
            }
        }
        else {
            console.warn(this.className + '.loadData() unable to find dimension:', dimension);
        }
    },

    setHeader : function(dimension, id) {
        this.getHeader().setVisible(id ? false : true);
    },

    loadDataView : function(dimension, id, urlTab) {

        this.setHeader(dimension, id);

        // do not hide the header
        if (this.items.length > 1) {
            for (var i=1; i < this.items.items.length; i++) {
                this.items.items[i].hide();
            }
        }

        this.completeLoad(dimension, id, urlTab);
    },

    completeLoad : function(dimension, id, urlTab) {

        if (Ext.isDefined(dimension)) {
            var store, _id = id;

            // If we have an id we are loading the details for that id
            if (Ext.isDefined(id) && dimension.itemDetail) {
                store = StoreCache.getStore(dimension.detailItemCollection || dimension.detailCollection);

                // coerce the id's type, this 'id' is possibly coming from the URL context
                if (Ext.isNumber(parseInt(id))) {
                    _id = parseInt(id);
                }

                var model = store.getById(_id) || this.resolveModel(store, _id);

                if (model) {
                    this.loadModel(model, dimension, urlTab);
                }
                else {
                    if (!store.isLoading() && store.getCount() > 0) {
                        Connector.getApplication().getController('Connector').showNotFound();
                    }
                    else {
                        store.on('load', function(s) {
                            var _model = s.getById(_id) || this.resolveModel(s, _id);
                            if (_model) {
                                this.loadModel(_model, dimension, urlTab);
                            }
                            else {
                                Connector.getApplication().getController('Connector').showNotFound();
                            }
                        }, this, {single: true});
                    }
                    this.loadData(dimension, store);
                }
            }
            else if (dimension.detailModel && dimension.detailView) {
                // otherwise, show the listing
                var listId = 'learn-list-' + dimension.uniqueName;

                // listView -- cache hit
                if (this.listViews[listId]) {
                    this.getComponent(listId).show();
                }
                else {
                    // listView -- cache miss, create the view
                    store = StoreCache.getStore(dimension.detailCollection);

                    this.add(Ext.create(dimension.detailView, {
                        itemId: listId,
                        dimension: dimension,
                        store: store
                        //plugins: ['learnheaderlock'],
                    }));

                    this.listViews[listId] = true;
                    this.loadData(dimension, store);
                }

                this.activeListing = this.getComponent(listId);
            }
            else {
                console.warn('Dimension \"' + dimension.getUniqueName() + '\" is marked as \'supportsDetails\'. It must provide itemDetail or detailModel and detailView configurations.');
            }
        }
        else {
            //
            // See which one the header is respecting
            //
            var dimModel = this.getHeader().getHeaderView().getStore().getAt(0);
            if (dimModel && dimModel.get('detailModel') && dimModel.get('detailView')) {
                this.loadDataView(dimModel.data);
            }
        }
    },

    resolveModel : function(store, id) {
        var delimiter = Connector.getService('Learn').URL_DELIMITER;
        if (Ext.isString(id) && id.indexOf(delimiter) != -1) {
            var _id = id.split(delimiter),
                    prop = _id[0],
                    val = Ext.isNumber(parseInt(_id[1])) ? parseInt(_id[1]) : _id[1],
                    data = store.data.items,
                    ret = [];

            for (var i = 0; i < data.length; i++) {
                if (ret.length < 2 && data[i].get(prop) === val) {
                    ret.push(data[i]);
                }
            }

            if (ret.length === 1) {
                return ret[0];
            }
        }
    },

    loadModel : function(model, dimension, urlTab) {
        var tabViews = [];
        Ext.each(dimension.itemDetail, function(item) {
            if (item.view) {
                tabViews.push(Ext.create(item.view, {
                    model: model,
                    modules: item.modules
                }));
            }
        });

        var activeTab = 0;
        if (!Ext.isEmpty(dimension.itemDetailTabs)) {
            Ext.each(dimension.itemDetailTabs, function(tab, i) {
                if (tab.url === urlTab) {
                    activeTab = i;
                    return false;
                }
                else if (tab.isDefault === true) {
                    activeTab = i;
                }
            });
        }

        var pageView = Ext.create('Connector.view.Page', {
            pageID: 'learnDetail' + dimension.singularName,
            contentViews: tabViews,
            initialSelectedTab: activeTab,
            header: Ext.create('Connector.view.PageHeader', {
                title: model.get(model.labelProperty ? model.labelProperty : 'label'),
                model: model,
                dimension: dimension,
                activeTab: activeTab
            })
        });

        this.add(pageView);
    },

    getDimensions : function() {
        return this.dimensions;
    },

    setDimensions : function(dimensions) {
        this.dimensions = dimensions;
        this.getHeader().setDimensions(dimensions);
    },

    // TODO: Move this to cube.js or hang the search fields on the model definitions themselves
    viewByDimension : {
        'Assay' : 'Assay',
        'Study' : 'Study',
        'Lab' : 'Labs',
        'Study product' : 'StudyProducts'
    },

    selectDimension : function(dimension, id, urlTab, searchTerm) {
        this.searchFilter = searchTerm ? searchTerm : undefined;
        this.searchFields = Connector.app.view[this.viewByDimension[dimension.singularName]].searchFields;

        if (dimension) {
            this.loadDataView(dimension, id, urlTab);
        }
        else {
            this.getHeader().on('selectdimension', this.loadDataView, this, {single: true});
        }

        this.getHeader().selectDimension(dimension ? dimension.uniqueName : undefined, id, dimension, searchTerm);
    }
});


Ext.define('Connector.view.LearnHeader', {

    extend: 'Ext.container.Container',

    alias: 'widget.learnheader',

    height: 180,

    cls: 'header-container learnaboutheader',

    defaults: {
        ui: 'custom',
        flex: 1
    },

    searchValue: undefined,

    initComponent : function() {

        this.items = [{
            xtype: 'actiontitle',
            text: 'Learn about...'
        },{
            xtype: 'container',
            items: [this.getDataView(), this.getSearchField()]
        }];

        this.callParent();

        this.addEvents('selectdimension', 'searchchanged');
    },

    getDataView : function() {
        if (!this.headerDataView) {
            this.headerDataView = Ext.create('Connector.view.LearnHeaderDataView', {
                cls: 'dim-selector',
                dimensions: this.dimensions,
                store: Ext.create('Ext.data.Store', {
                    model: 'Connector.model.Dimension',
                    proxy: {
                        type: 'memory',
                        reader: {
                            type: 'json',
                            root: 'dimensions'
                        }
                    }
                })
            });
            this.headerDataView.on({
                itemclick: function(view, model) {
                    this.fireEvent('selectdimension', model);
                },
                requestselect : function(model) {
                    this.fireEvent('selectdimension', model, true);
                },
                scope: this
            });
        }
        return this.headerDataView;
    },

    getSearchField : function() {
        if (!this.searchField) {
            this.searchField = Ext.create('Ext.form.field.Text', {
                emptyText: 'Search',
                cls: 'learn-search-input',
                checkChangeBuffer: 500,
                value: this.searchValue,
                validator: Ext.bind(function(value) {
                    this.fireEvent('searchchanged', value);
                    return true;
                }, this)
            });
        }
        return this.searchField;
    },

    setDimensions : function(dimensions) {
        this.dimensions = dimensions;
        this.getDataView().setDimensions(dimensions);
    },

    selectDimension : function(dimUniqueName, id, dimension, searchTerm) {
        if (!Ext.isEmpty(this.dimensions)) {
            this.getDataView().selectDimension(dimUniqueName);
        }
        var search = this.getSearchField();
        search.emptyText = 'Search ' + dimension.pluralName.toLowerCase();
        search.setValue(searchTerm ? searchTerm : '');
        this.fireEvent('searchchanged', searchTerm ? searchTerm : '');
    }
});

//
// This is an internal class to header which is wrapped by Connector.view.LearnHeader
//
Ext.define('Connector.view.LearnHeaderDataView', {

    extend: 'Ext.view.View',

    alias: 'widget.learnheaderdataview',

    itemSelector: 'h1.lhdv',

    selectedItemCls: 'active',

    loadMask: false,

    selectInitialDimension: false,

    tpl: new Ext.XTemplate(
        '<tpl for=".">',
            '<h1 class="lhdv">{pluralName:htmlEncode}</h1>',
        '</tpl>'
    ),

    initComponent : function() {

        this.addEvents('requestselect');

        this.callParent();

        if (this.dimensions) {
            this.setDimensions(this.dimensions);
            this.dimensions = undefined;
        }
    },

    setDimensions : function(dimensions) {

        var store = this.getStore();

        store.loadRawData(dimensions);

        //
        // Filter out hidden dimensions, and dimensions which do not support detail view
        //
        store.filter('hidden', false);
        store.filter('supportsDetails', true);

        //
        // Sort dimensions by stated priority
        //
        store.sort('priority', 'desc');

        //
        // Select the initial dimension
        //
        if (this.selectInitialDimension && store.getCount() > 0) {
            this.selectDimension(store.getAt(0).get('uniqueName'));
        }
    },

    //
    // Select a dimension by unique name. If this method is called and a name is
    // not provided then the first valid dimension will be selected.
    //
    selectDimension : function(dimUniqueName) {
        var uniqueName = dimUniqueName;
        var store = this.getStore();

        if (!Ext.isDefined(uniqueName)) {
            uniqueName = store.getAt(0).get('uniqueName');
        }

        var idx = store.findExact('uniqueName', uniqueName);
        if (idx >= 0) {
            var model = store.getAt(idx);
            if (!this.rendered) {
                this.on('afterrender', function() { this._select(model); }, this, {single: true});
            }
            else {
                this._select(model);
            }
        }
        else {
            console.warn('Unable to select dimension:', uniqueName);
        }
    },

    _select : function(model) {
        this.getSelectionModel().select(model);
        this.fireEvent('requestselect', model);
    }
});

Ext.define('Connector.view.LearnColumnHeader', {

    extend : 'Ext.container.Container',

    height: 30,

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    cls: 'learncolumnheader',

    defaults: {
        ui: 'custom',
        flex: 1
    },

    initComponent : function() {

        this.items = [
            {
                xtype: 'box',
                cls: 'learn-column-header',
                autoEl: {
                    tag: 'div'
                }
            }
        ];

        this.callParent();
    }
});

Ext.define('Connector.view.Learn.plugin.HeaderLock', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.learnheaderlock',

    lockCls: 'headerlock',

    headerOffset: 53,

    init : function(cmp) {

        cmp.on('afterrender', function() {

            // initialize constants
            this.elements = {
                view: this.selectUnique('.learnview')
            };

            // initialize listeners
            var EM = Ext.EventManager;
            EM.on(window, 'resize', this.onResize, this);
            EM.on(this.elements.view, 'scroll', this.onScroll, this);

        }, this, {single: true});

        this.resizeTask = new Ext.util.DelayedTask(function() {
            var w = this.getCalculatedWidth();
            if (w > 0) {
                var lock = this.getLockElement();
                if (lock) {
                    lock.setWidth(w);
                    this.elements.dupe.setWidth(w);
                }
            }
        }, this);
    },

    destroy : function() {
        // stop
        this.resizeTask.cancel();

        // unregister
        var EM = Ext.EventManager;
        EM.un(window, 'resize', this.onResize, this);
        if (this.elements) {
            EM.un(this.elements.view, 'scroll', this.onScroll, this);

            // clear
            this.elements.lock = null;
            this.elements.header = null;
            this.elements.view = null;
        }
    },

    update : function() {

        var hdr = this.getHeaderElement(),
            lock = this.getLockElement();

        if (hdr && lock) {
            var box = hdr.getBox();
            if (box.bottom > this.headerOffset) {
                lock.removeCls(this.lockCls);
                if (this.elements.dupe)
                    this.elements.dupe.hide();
            }
            else {
                lock.addCls(this.lockCls);
                if (this.elements.dupe)
                    this.elements.dupe.show();
            }
        }
    },

    onResize : function() {
        this.resizeTask.delay(100);
    },

    onScroll : function() {
        this.update();
    },

    getCalculatedWidth : function() {
        return (this.elements.view ? this.elements.view.getWidth() : 0);
    },

    // Nullable
    getHeaderElement : function() {
        if (!this.elements.header) {
            this.elements.header = this.selectUnique('.learnheader');
        }
        return this.elements.header;
    },

    // Nullable
    getLockElement : function() {
        if (!this.elements.lock) {
            var lock = this.selectUnique('.learncolumnheader');
            if (lock) {

                var h = lock.getHeight();
                var w = lock.getWidth();

                lock.setWidth(w);
                this.elements.lock = lock;

                //
                // Once we have the associated lock element,
                // place an element next to it to fill its space
                // when it unlocks. Prevents jumping.
                //
                var style = 'display: none; ';
                style += 'height: ' + h + 'px; ';
                style += 'width: ' + w + 'px; ';
                style += 'background-color: transparent; ';

                var dupe = document.createElement('div');
                dupe.setAttribute('style', style);
                dupe = Ext.get(dupe);
                dupe.insertBefore(lock);
                dupe.setVisibilityMode(2); // use 'display'
                this.elements.dupe = dupe;
            }
        }
        return this.elements.lock;
    },

    // Nullable
    selectUnique : function(selector) {
        return Ext.get(Ext.DomQuery.select(selector)[0])
    }
});
