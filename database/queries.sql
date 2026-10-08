-- =====================================================================
-- queries.sql : demo queries (the same SQL used by the Spring Boot API)
-- =====================================================================
USE digital_wallet;

-- A. JOIN: USERS -> WALLETS -> TRANSACTIONS -> TRANSACTION_TYPES
SELECT t.id               AS transaction_id,
       u.id               AS user_id,
       u.name             AS user_name,
       u.email            AS user_email,
       w.id               AS wallet_id,
       tt.type_name       AS transaction_type,
       t.amount,
       t.description,
       t.transaction_date
FROM users u
JOIN wallets w            ON w.user_id = u.id
JOIN transactions t       ON t.wallet_id = w.id
JOIN transaction_types tt ON tt.id = t.transaction_type_id
ORDER BY t.transaction_date DESC, t.id DESC;

-- B. SUBQUERY: transactions larger than the average transaction amount
SELECT u.id AS user_id, u.name AS user_name, u.email, t.amount AS transaction_amount
FROM users u
JOIN wallets w      ON w.user_id = u.id
JOIN transactions t ON t.wallet_id = w.id
WHERE t.amount > (SELECT AVG(amount) FROM transactions)
ORDER BY t.amount DESC;

-- C. FUNCTION
SELECT get_wallet_balance(1) AS balance;

-- D. PROCEDURE
-- CALL transfer_money(1, 2, 100.00, 'Test transfer');
