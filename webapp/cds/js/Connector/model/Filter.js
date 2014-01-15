/*
 * Copyright (c) 2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext4.define('Connector.Filter', {
    statics : {
        Operators : {
            UNION : 'UNION',
            INTERSECT : 'INTERSECT'
        }
    }
});

Ext4.define('Connector.model.Filter', {
    extend : 'Ext.data.Model',
    fields : [
        {name : 'id'},
        {name : 'hierarchy'},
        {name : 'members'},
        {name : 'operator'},
        {name : 'isGroup', type: 'boolean'},
        {name : 'isGrid', type: 'boolean'},
        {name : 'gridFilter'} // instance of LABKEY.Filter
    ],

    lookupOperator : function() {

        // This case is used when the user overrides the operator
        if (this.data.operator) {
            return this.data.operator;
        }

        var ops = Connector.Filter.Operators;

        switch (this.data.hierarchy) {
            case 'Study':
                return ops.UNION;
            case 'Participant.Race':
                return ops.UNION;
            case 'Participant.Country':
                return ops.UNION;
            case 'Participant.Sex':
                return ops.UNION;
            default:
                return ops.INTERSECT;
        }
    },

    getOlapFilter : function() {

        var filter = {
            operator : this.lookupOperator(),
            arguments: []
        };

        if (this.data.hierarchy == 'Participant') {

            filter.arguments.push({
                hierarchy : 'Participant',
                members  : this.data.members
            });
            return filter;
        }

        for (var m=0; m < this.data.members.length; m++) {

            filter.arguments.push({
                hierarchy : 'Participant',
                membersQuery : {
                    hierarchy : this.data.hierarchy,
                    members   : [this.data.members[m]]
                }
            });

        }

        return filter;
    },

    getDisplayHierarchy : function() {
        var h = this.getHierarchy().split('.');

        // Simple hierarchy (e.g. 'Study')
        if (h.length == 1) {
            return h[0];
        }

        if (this.data.isGroup) {
            return h[1];
        }

        // Issue 15380
        if (h[0] == 'Participant') {
            return h[1];
        }
        return h[0];
    },

    getHierarchy : function() {
        return this.data.hierarchy;
    },

    getMembers : function() {
        return this.data.members;
    },

    removeMember : function(memberUname) {

        // Allow for removal of the entire filter if a uname is not provided
        if (!memberUname) {
            return [];
        }

        var newMembers = [];
        for (var m=0; m < this.data.members.length; m++) {
            if (memberUname != this.data.members[m].uname)
            {
                newMembers.push(this.data.members[m]);
            }
        }
        return newMembers;
    },

    getOperator : function() {
        return this.lookupOperator();
    },

    /**
     * Simple comparator that says two filters are equal if they share the same hierarchy, have only 1 member, and the member
     * is equivalent
     * @param f - Filter to compare this object against.
     */
    isEqualAsFilter : function(f) {
        var d = this.data;
        if ((d && f.data) && (d.hierarchy == f.data.hierarchy)) {
            if (d.members.length == 1 && f.data.members.length == 1) {
                if (d.members[0].uname[d.members[0].uname.length-1] == f.data.members[0].uname[f.data.members[0].uname.length-1]) {
                    return true;
                }
            }
        }
        return false;
    },

    isGrid : function() {
        return this.data['isGrid'];
    },

    getGridHierarchy : function() {
        if (this.data['gridFilter']) {
            if (!Ext4.isFunction(this.data['gridFilter'].getColumnName))
            {
                console.warn('invalid filter object being processed.');
                return 'Unknown';
            }
            var label = this.data['gridFilter'].getColumnName().split('/');

            // check lookups
            if (label.length > 1) {
                label = label[label.length-2];
            }
            else {
                // non-lookup column
                label = label[0];
            }

            label = label.split('_');
            return Ext4.String.ellipsis(label[label.length-1], 9, false);
        }
        return 'Unknown';
    },

    getGridLabel : function() {
        if (this.data['gridFilter']) {
            var gf = this.data.gridFilter;
            if (!Ext4.isFunction(gf.getFilterType))
            {
                console.warn('invalid label being processed');
                return 'Unknown';
            }
            return this.getShortFilter(gf.getFilterType().getDisplayText()) + ' ' + gf.getValue();
        }
        return 'Unknown';
    },

    /**
     * Returns abbreviated display value. (E.g. 'Equals' returns '=');
     * @param displayText - display text from LABKEY.Filter.getFilterType().getDisplayText()
     */
    getShortFilter : function(displayText) {

        switch (displayText) {
            case "Does Not Equal":
                return '!=';
            case "Equals":
                return '=';
            case "Is Greater Than":
                return '>';
            case "Is Less Than":
                return '<';
            case "Is Greater Than or Equal To":
                return '>=';
            case "Is Less Than or Equal To":
                return '<=';
            default:
                return displayText;
        }
    },

    isGroup : function() {
        return false;
    },

    getValue : function(key) {
        return this.data[key];
    }
});
