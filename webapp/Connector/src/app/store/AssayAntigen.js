/*
 * Copyright (c) 2016-2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext.define('Connector.app.store.AssayAntigen', {

    extend : 'Ext.data.Store',

    model : 'Connector.app.model.AssayAntigen',

    isAntigensLoaded: false,

    assayType: null,

    constructor: function(config) {
        Ext.applyIf(config, {
            cache: []
        });
        this.callParent([config]);
    },

    loadAntigens : function() {
        if (this.isAntigensLoaded)
            return;
        var assayType = this.assayType.toUpperCase();
        if (assayType === 'ICS' || assayType === 'ELISPOT') {
            LABKEY.Query.selectRows({
                schemaName: 'cds',
                queryName: 'learn_' + assayType + '_antigens',
                scope: this,
                success: function(result) {
                    var antigens = [],
                            idx = 0;
                    Ext.each(result.rows, function(row) {

                            if(antigens[idx-1] && antigens[idx-1].antigen_name === row.antigen_name) {
                                antigens[idx-1].antigen_proteinAndPools.push({
                                    protein: row.protein,
                                    pools: row.pools
                                });
                                antigens[idx-1].antigen_proteins.push(row.protein);
                                Ext.each(row.pools, function(pool){
                                    antigens[idx-1].antigen_pools.push(pool);
                                });
                                if (row.antigen_description[0] && !Ext.Array.contains(antigens[idx-1].antigen_description, row.antigen_description[0])) {
                                    var concat = '';
                                    if (antigens[idx-1].antigen_description.length > 0)
                                        concat = ', ';
                                    antigens[idx-1].antigen_description.push(concat + row.antigen_description[0]);
                                }
                            }
                            else {
                                var antigen = this.getAntigen(row);
                                Ext.apply(antigen, {
                                    antigen_proteinAndPools: [{
                                        protein: row.protein,
                                        pools: row.pools
                                    }],
                                    antigen_proteins: [row.protein],
                                    antigen_pools: row.pools
                                });
                                antigens[idx] = antigen;
                                idx++;
                            }
                    }, this);

                    this.loadRawData(antigens);
                    this.isAntigensLoaded = true;
                }
            })
        }
        else
        {
            if (assayType === 'NABMAB')
                assayType = "NAB";
            LABKEY.Query.selectRows({
                schemaName: 'cds',
                queryName: assayType + 'antigen',
                scope: this,
                success: function(result) {
                    var antigens = [];
                    Ext.each(result.rows, function(row) {
                        if (row.assay_identifier === this.assayId) {
                            var antigen = this.getAntigen(row);
                            antigens.push(antigen);
                        }
                    }, this);
                    this.loadRawData(antigens);
                    this.isAntigensLoaded = true;
                }
            })
        }
    },

    getAntigen: function(row)
    {
        var identifier = row.antigen_name;
        if (row.target_cell)
            identifier += row.target_cell;
        identifier += row.antigen_type;
        var description = Ext.isArray(row.antigen_description) ? (row.antigen_description[0] ? [row.antigen_description[0]] : []) : [row.antigen_description];
        return {
            antigen_identifier: identifier,
            antigen_name: row.antigen_name,
            antigen_description: description,
            antigen_type: row.antigen_type,
            antigen_control_value: row.antigen_control && row.antigen_control != "0" ? "YES" : "NO", //this assumes the control status is the same for all peptide pools of a protein panel
            antigen_clade: row.clade,
            antigen_clades: row.clades,
            antigen_neutralization_tier: row.neutralization_tier,
            antigen_protein: row.protein,
            antigen_target_cell: row.target_cell,
            antigen_virus_type: row.virus_type
        };
    }

});
