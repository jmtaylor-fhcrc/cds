/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.model.StudyAxisData', {

    extend : 'Ext.data.Model',

    fields : [
        /* values passed in from Chart.js */
        {name : 'measure', defaultValue: {}}, // x-axis timepoint measure
        {name : 'containerAlignmentDayMap', defaultValue: {}},
        {name : 'records', defaultValue: []}, // filtered set of StudyVisitTag model records

        /* generated properties based on the processing of the above records */
        {name : 'data', defaultValue: []},
        {name : 'range', defaultValue: {min: null, max: null}}
    ],

    constructor : function(config) {
        this.callParent([config]);

        this.processStudyAxisData();
    },

    getRecords : function() {
        return this.get('records');
    },

    getMeasure : function(index) {
        return this.get('measure');
    },

    getContainerAlignmentDayMap : function() {
        return this.get('containerAlignmentDayMap');
    },

    getData : function() {
        return this.get('data');
    },

    getRange : function() {
        return this.get('range');
    },

    processStudyAxisData : function() {
        var records = this.getRecords(), containerAlignmentDayMap = this.getContainerAlignmentDayMap(),
                interval, studyMap = {}, studyLabel, data = [], range = {min: null, max: null},
                study, studyContainer, studyKeys, visit, visits, visitId, visitKeys, visitKey, visitLabel, seqMin,
                seqMax, protocolDay, timepointType, groupName, visitTagCaption, isVaccination,
                shiftVal, i, j, alignmentVisitTag, visitTagName, _row;

        if (this.getMeasure().interval) {
            interval = this.getMeasure().interval.toLowerCase();
        }

        // first we have to loop through the study axis visit information to find the alignment visit for each container
        alignmentVisitTag = this.getMeasure().options ? this.getMeasure().options.alignmentVisitTag : null;
        if (alignmentVisitTag != null)
        {
            Ext.each(records, function(record){
                studyContainer = record.get('container_id');
                visitTagName = record.get('visit_tag_name');
                if (visitTagName == alignmentVisitTag)
                    containerAlignmentDayMap[studyContainer] = record.get('protocol_day');
            }, this);
        }

        // loop through the StudyVisitTag records to gather data about each visit
        Ext.each(records, function(record){
            studyLabel = record.get('study_label');
            studyContainer = record.get('container_id');
            shiftVal = containerAlignmentDayMap[studyContainer];
            visitId = record.get('visit_row_id');
            visitLabel = record.get('visit_label');
            seqMin = record.get('sequence_num_min');
            seqMax = record.get('sequence_num_max');
            protocolDay = this.convertInterval(record.get('protocol_day') - shiftVal, interval);
            timepointType = record.get('timepoint_type');
            groupName = record.get('group_name');
            visitTagCaption = record.get('visit_tag_caption');
            isVaccination = record.get('is_vaccination');

            if (timepointType !== 'VISIT') {
                seqMin = this.convertInterval(seqMin - shiftVal, interval);
                seqMax = this.convertInterval(seqMax - shiftVal, interval);
            }

            // track each unique study by label
            if (!studyMap.hasOwnProperty(studyLabel)) {
                studyMap[studyLabel] = {
                    label : studyLabel,
                    timepointType : timepointType,
                    visits: {}
                };
            }

            study = studyMap[studyLabel];

            // track each unique visit in a study by rowId
            if (!study.visits.hasOwnProperty(visitId)) {
                study.visits[visitId] = {
                    studyLabel: studyLabel,
                    label: visitLabel,
                    sequenceNumMin: seqMin,
                    sequenceNumMax: seqMax,
                    protocolDay: protocolDay,
                    imgSrc: 'nonvaccination_normal.svg',
                    visitTags: []
                };
            }

            visit = study.visits[visitId];

            if (visitTagCaption !== null) {
                // determine which visit tag/milestone glyph to display
                // TODO: why is is_vaccination always coming back as false?
                if (isVaccination || visitTagCaption == 'Vaccination') {
                    visit.imgSrc = 'vaccination_normal.svg';
                    visit.imgSize = 14;
                }

                visit.visitTags.push({group: groupName, tag: visitTagCaption});
            }

            if (range.min == null || range.min > protocolDay)
                range.min = protocolDay;
            if (range.max == null || range.max < protocolDay)
                range.max = protocolDay;
        }, this);

        // Convert study map and visit maps into arrays.
        studyKeys = Object.keys(studyMap).sort();
        for (i = 0; i < studyKeys.length; i++) {
            study = studyMap[studyKeys[i]];
            visitKeys = Object.keys(study.visits).sort();
            visits = [];
            for (j = 0; j < visitKeys.length; j++) {
                visitKey = visitKeys[j];
                visits.push(study.visits[visitKey]);
            }

            study.visits = visits;
            data.push(study);
        }

        this.set({
            data: data,
            range: range
        });
    },

    convertInterval : function(d, interval) {
        // Conversion methods here taken from VisualizationIntervalColumn.java line ~30
        if (interval == 'days') {
            return d;
        }
        else if (interval == 'weeks') {
            return Math.floor(d / 7);
        }
        else if (interval == 'months') {
            return Math.floor(d / (365.25/12));
        }

        throw 'Invalid interval supplied! Expected "days", "weeks", or "months"';
    }
});