create table if not exists directorios(id serial, nombre text, primary key (id), constraint nombre_unico unique (nombre));
create table if not exists archivos(id serial, nombre text, archivo bytea, dir integer, primary key (id), constraint nombre_dir_unico unique (nombre, dir));
CREATE OR REPLACE FUNCTION notificar_cambio_archivo() RETURNS trigger AS $$ BEGIN PERFORM pg_notify('cambio_archivo',NEW.id::text); RETURN NEW; END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS not_cambio_archivo ON archivos;
CREATE TRIGGER not_cambio_archivo AFTER INSERT ON archivos FOR EACH ROW EXECUTE PROCEDURE notificar_cambio_archivo();
CREATE OR REPLACE FUNCTION notificar_cambio_directorio() RETURNS trigger AS $$ BEGIN PERFORM pg_notify('cambio_directorio',NEW.id::text); RETURN NEW; END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS not_cambio_directorio ON directorios;
CREATE TRIGGER not_cambio_directorio AFTER INSERT ON directorios FOR EACH ROW EXECUTE PROCEDURE notificar_cambio_directorio();