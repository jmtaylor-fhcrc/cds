/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.app.model.Study', {

    extend : 'Ext.data.Model',

    idProperty: 'study_name',

    resolvableField: 'label',

    fields: [
        {name: 'study_name'},
        {name: 'Container'},
        {name: 'network'},
        {name: 'label', sortType: 'asUCString'},
        {name: 'short_name'},
        {name: 'title'},
        {name: 'type'},
        {name: 'status'},
        {name: 'stage'},
        {name: 'species'},
        {name: 'population'},
        {name: 'executive_summary'},
        {name: 'description'},
        {name: 'groups'},
        {name: 'treatment_schema_link'},
        {name: 'rationale'},
        {name: 'hypothesis'},
        {name: 'objectives'},
        {name: 'methods'},
        {name: 'assay_schema_link'},
        {name: 'findings'},
        {name: 'conclusions'},
        {name: 'publications'},
        {name: 'start_date', defaultValue: undefined },
        {name: 'public_date', defaultValue: undefined },
        {name: 'data_availability'},
        {name: 'cavd_affiliation'},
        {name: 'cavd_affiliation_filename'},
        {name: 'cavd_affiliation_file_exists'},
        {name: 'study_cohort'},
        {name: 'first_enr_date', defaultValue: undefined },
        {name: 'followup_complete_date', defaultValue: undefined },
        {name: 'discussion'},
        {name: 'context'},
        {name: 'grant_pi_name'},
        {name: 'grant_pi_email'},
        {name: 'grant_pm_name'},
        {name: 'grant_pm_email'},
        {name: 'investigator_name'},
        {name: 'investigator_email'},
        {name: 'primary_poc_name'},
        {name: 'primary_poc_email'},
        {name: 'date_to_sort_on', sortType: 'asDate'},
        {name: 'start_year'},
        {name: 'product_to_sort_on'},
        {name: 'products', convert : function(value) {
            return Ext.isArray(value) ? value : [];
        }},
        {name: 'product_names', convert : function(value) {
            return Ext.isArray(value) ? value : [];
        }},
        {name: 'assays', convert : function(value) {
            return Ext.isArray(value) ? value : [];
        }},
        {name: 'groups_treatment_schema', defaultValue: undefined},
        {name: 'methods_assay_schema', defaultValue: undefined},
        {name: 'assays_added_count'},
        {name: 'assays_added', convert : function(value) {
            return Ext.isArray(value) ? value : [];
        }},
        {name: 'atlas_link'},
        {name: 'cavd_link'}
    ]
});