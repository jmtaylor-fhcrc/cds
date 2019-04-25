ALTER TABLE cds.import_publication ADD COLUMN publication_author_first VARCHAR(250);
ALTER TABLE cds.import_publication ADD COLUMN publication_label VARCHAR(250);
ALTER TABLE cds.publication ADD COLUMN author_first VARCHAR(250);
ALTER TABLE cds.publication ADD COLUMN publication_label VARCHAR(250);

CREATE TABLE cds.import_tours (
  title       character varying(500)      ,
  description character varying(4000)     ,
  container   public.entityid             ,
  created     timestamp without time zone ,
  createdby   public.userid               ,
  modified    timestamp without time zone ,
  modifiedby  public.userid               ,
  json        character varying           ,
  mode        integer                      
);

