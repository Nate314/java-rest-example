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
