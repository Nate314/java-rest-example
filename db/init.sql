-- Minimal schema/seed for the "testing" database used by com.nathangawith.database.Database
-- (queried by GET /math/add/{a}/{b} via `select * from testing_table`).
-- This table/data did not previously exist anywhere in the repo; it is added here
-- solely so the Dockerized app has something real to query end-to-end.
CREATE TABLE IF NOT EXISTS testing_table (
    col_date DATE NOT NULL,
    col_int INT NOT NULL,
    col_string VARCHAR(255) NOT NULL
);

INSERT INTO testing_table (col_date, col_int, col_string)
VALUES ('2024-01-01', 42, 'hello from docker mysql');
