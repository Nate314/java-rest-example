#!/bin/bash
# Creates the least-privilege account the app connects with. Runs once, on
# first initialisation of the data volume, after 01-schema.sql has created the
# tables. The app user can only read/insert the tables it needs: no DROP,
# ALTER, UPDATE, DELETE, and no access to any other schema.
# DB_USER / DB_PASSWORD must not contain single quotes.
set -euo pipefail

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE USER '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT SELECT ON \`${MYSQL_DATABASE}\`.testing_table TO '${DB_USER}'@'%';
GRANT SELECT, INSERT ON \`${MYSQL_DATABASE}\`.users TO '${DB_USER}'@'%';
GRANT SELECT, INSERT ON \`${MYSQL_DATABASE}\`.math_operations TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
SQL
