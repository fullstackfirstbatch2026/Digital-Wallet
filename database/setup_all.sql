-- Run everything in the correct order from the mysql command line client:
--   cd database
--   mysql -u root -p < setup_all.sql
SOURCE schema.sql;
SOURCE functions.sql;
SOURCE procedures.sql;
SOURCE triggers.sql;
SOURCE data.sql;
