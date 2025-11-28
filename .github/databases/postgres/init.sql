-- Since PostgreSQL 15, the database revokes the CREATE permission from all users
-- except a database owner from the public (or default) schema.
-- Reference: https://www.postgresql.org/about/news/postgresql-15-released-2526/

CREATE DATABASE :"target_db";
CREATE USER dtf11 WITH ENCRYPTED PASSWORD 'dtf11';
CREATE USER dtf12 WITH ENCRYPTED PASSWORD 'dtf12';
GRANT ALL PRIVILEGES ON DATABASE :"target_db" TO dtf11;
GRANT ALL PRIVILEGES ON DATABASE :"target_db" TO dtf12;
\connect :"target_db" :"pguser"
GRANT ALL ON SCHEMA public TO dtf11;
GRANT ALL ON SCHEMA public TO dtf12;

ALTER SYSTEM SET max_prepared_transactions = 100;