/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.view.TimepointPane', {

    extend: 'Connector.view.InfoPane',

    padding: '10',

    showSort: true,

    isShowOperator: false,

    displayTitle: 'Time points in the plot',

    updateSelections : function()
    {
        var grid = this.getGrid();

        if (Ext.isDefined(this.getModel().getFilterVisitRowIds()))
        {
            grid.getSelectionModel().select(grid.getStore().query('selected', true).items);
        }
        else
        {
            grid.getSelectionModel().selectAll();
        }

        grid.fireEvent('selectioncomplete', this);
    }
});