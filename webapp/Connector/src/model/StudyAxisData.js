/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.model.StudyAxisData', {

    extend : 'Ext.data.Model',

    fields : [
        {name : 'measures', defaultValue: [null, null, null]}, // Array [x, y, color]
        {name : 'visitMap', defaultValue: {}},
        {name : 'containerAlignmentDayMap', defaultValue: {}},

        /* from the selectRows call to the temp query generated by the getData call */
        {name : 'schemaName'},
        {name : 'queryName'},
        {name : 'rows', defaultValue: []},

        /* generated properties based on the processing of the above rows */
        {name : 'data', defaultValue: []},
        {name : 'range', defaultValue: {min: null, max: null}}
    ],

    constructor : function(config) {
        this.callParent([config]);

        this.processStudyAxisData();
    },

    getDataRows : function() {
        return this.get('rows');
    },

    getMeasure : function(index) {
        return this.get('measures')[index];
    },

    getContainerAlignmentDayMap : function() {
        return this.get('containerAlignmentDayMap');
    },

    getVisitMap : function() {
        return this.get('visitMap');
    },

    getData : function() {
        return this.get('data');
    },

    getRange : function() {
        return this.get('range');
    },

    processStudyAxisData : function(mappings) {
        var rows = this.getDataRows(), visitMap = this.getVisitMap(), containerAlignmentDayMap = this.getContainerAlignmentDayMap(),
                interval, studyMap = {}, studyLabel, data = [], range = {min: null, max: null},
                study, studyContainer, studyKeys, visit, visits, visitId, visitKeys, visitKey, visitLabel, seqMin,
                seqMax, protocolDay, timepointType, visitTagCaption, shiftVal, i, j, alignmentVisitTag, visitTagName;

        interval = this.getMeasure(0).interval.toLowerCase();

        // first we have to loop through the study axis visit information to find the alignment visit for each container
        alignmentVisitTag = this.getMeasure(0).options ? this.getMeasure(0).options.alignmentVisitTag : null;
        if (alignmentVisitTag != null)
        {
            for (j = 0; j < rows.length; j++)
            {
                studyContainer = rows[j].StudyContainer.value;
                visitTagName = rows[j].VisitTagName.value;
                if (visitTagName == alignmentVisitTag)
                    containerAlignmentDayMap[studyContainer] = rows[j].ProtocolDay.value;
            }
        }

        for (j = 0; j < rows.length; j++) {
            studyLabel = rows[j].StudyLabel.value;
            studyContainer = rows[j].StudyContainer.value;
            shiftVal = containerAlignmentDayMap[studyContainer];
            visitId = rows[j].VisitRowId.value;
            visitLabel = rows[j].VisitLabel.value;
            seqMin = rows[j].SequenceNumMin.value;
            seqMax = rows[j].SequenceNumMax.value;
            protocolDay = this.convertInterval(rows[j].ProtocolDay.value - shiftVal, interval);
            timepointType = rows[j].TimepointType.value;
            visitTagCaption = rows[j].VisitTagCaption.value;

            if (!visitMap[visitId] && !visitTagCaption) { continue; }

            if (timepointType !== 'VISIT') {
                seqMin = this.convertInterval(seqMin - shiftVal, interval);
                seqMax = this.convertInterval(seqMax - shiftVal, interval);
            }

            if (!studyMap.hasOwnProperty(studyLabel)) {
                studyMap[studyLabel] = {
                    label : studyLabel,
                    timepointType : timepointType,
                    visits: {}
                };
            }

            study = studyMap[studyLabel];

            if (!study.visits.hasOwnProperty(visitId)) {
                study.visits[visitId] = {
                    label: visitLabel,
                    sequenceNumMin: seqMin,
                    sequenceNumMax: seqMax,
                    protocolDay: protocolDay,
                    hasPlotData: visitMap[visitId] != undefined,
                    visitTags: {}
                };
            }

            visit = study.visits[visitId];

            if (visitTagCaption !== null && !visit.visitTags.hasOwnProperty(visitTagCaption)) {
                visit.visitTags[visitTagCaption] = visitTagCaption;
            }

            if (range.min == null || range.min > protocolDay)
                range.min = protocolDay;
            if (range.max == null || range.max < protocolDay)
                range.max = protocolDay;
        }

        // Convert study map and visit maps into arrays.
        studyKeys = Object.keys(studyMap).sort();

        for (i = 0; i < studyKeys.length; i++) {
            study = studyMap[studyKeys[i]];
            visitKeys = Object.keys(study.visits).sort();
            visits = [];
            for (j = 0; j < visitKeys.length; j++) {
                visitKey = visitKeys[j];
                study.visits[visitKey].visitTags = Object.keys(study.visits[visitKey].visitTags).sort();
                visits.push(study.visits[visitKey]);
            }

            study.visits = visits;
            data.push(study);
        }

        this.set('data', data);
        this.set('range', range);
    },

    convertInterval : function(d, interval) {
        // Conversion methods here taken from VisualizationIntervalColumn.java line ~30
        if (interval == 'days') {
            return d;
        } else if (interval == 'weeks') {
            return Math.floor(d / 7);
        } else if (interval == 'months') {
            return Math.floor(d / (365.25/12));
        }
    }
});