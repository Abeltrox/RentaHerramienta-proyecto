-- Ejecutar conectado a la base "postgres":  psql -U postgres -f 00_crear_base_de_datos.sql
-- (Opcional: si usas Flyway basta con crear la base; la app crea tablas y datos al arrancar.)
CREATE DATABASE renta_herramientas WITH ENCODING 'UTF8';
