-- =====================================================================
-- schema.sql : database + tables
-- Run order: 1) schema.sql 2) functions.sql 3) procedures.sql
--            4) triggers.sql 5) data.sql        (see database/README in main README)
-- =====================================================================
CREATE DATABASE IF NOT EXISTS digital_wallet
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE digital_wallet;

-- Drop in reverse dependency order so the script can be re-run safely
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS transaction_types;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    phone      VARCHAR(20),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

-- One wallet per user (UNIQUE user_id). Deleting a user removes the (empty) wallet,
-- but is blocked while the wallet still has transactions.
CREATE TABLE wallets (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    user_id    BIGINT        NOT NULL,
    balance    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_wallets_user UNIQUE (user_id),
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_wallet_balance CHECK (balance >= 0)
) ENGINE=InnoDB;

CREATE TABLE transaction_types (
    id        INT         NOT NULL AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_transaction_types_name UNIQUE (type_name)
) ENGINE=InnoDB;

CREATE TABLE transactions (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    wallet_id           BIGINT        NOT NULL,
    transaction_type_id INT           NOT NULL,
    amount              DECIMAL(15,2) NOT NULL,
    description         VARCHAR(255),
    transaction_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id),
    CONSTRAINT fk_transactions_type   FOREIGN KEY (transaction_type_id) REFERENCES transaction_types (id),
    CONSTRAINT chk_transaction_amount CHECK (amount > 0),
    INDEX idx_transactions_wallet (wallet_id),
    INDEX idx_transactions_type (transaction_type_id),
    INDEX idx_transactions_date (transaction_date)
) ENGINE=InnoDB;
