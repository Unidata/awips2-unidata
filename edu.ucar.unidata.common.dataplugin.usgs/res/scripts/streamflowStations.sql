BEGIN;
INSERT INTO "awips"."streamflow_spatial" ("station_id","station_name","source","lon","lat",the_geom) VALUES ('06719505','CLEAR CREEK AT GOLDEN, CO','USGS','-105.2352667','39.75304299',ST_GeometryFromText('POINT(-105.2352667 39.75304299)',4326) );
END;