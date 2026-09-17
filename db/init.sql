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

-- Registered users. Passwords are stored as bcrypt hashes, never plaintext.
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit log of math operations, tied to the authenticated user who ran them.
CREATE TABLE IF NOT EXISTS math_operations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    operand_a INT NOT NULL,
    operand_b INT NOT NULL,
    result INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_math_operations_username FOREIGN KEY (username) REFERENCES users(username)
);
