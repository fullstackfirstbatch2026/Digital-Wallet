-- =====================================================================
-- data.sql : sample data. Run AFTER functions, procedures and triggers
-- because balances are produced by the trigger and the stored procedure.
-- =====================================================================
USE digital_wallet;

INSERT INTO transaction_types (type_name) VALUES ('DEPOSIT'), ('WITHDRAW'), ('TRANSFER');

INSERT INTO users (name, email, phone) VALUES
    ('Arjun Kumar',   'arjun.kumar@example.com',   '9876543210'),
    ('Priya Sharma',  'priya.sharma@example.com',  '9876501234'),
    ('Rahul Verma',   'rahul.verma@example.com',   '9123456780'),
    ('Sneha Iyer',    'sneha.iyer@example.com',    '9988776655'),
    ('Karthik Raj',   'karthik.raj@example.com',   '9090909090'),
    ('Divya Nair',    'divya.nair@example.com',    '9812345670');

-- Every wallet starts at 0.00; the trigger fills the balance from the deposits below
INSERT INTO wallets (user_id, balance) VALUES (1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0);

-- DEPOSIT = 1, WITHDRAW = 2  (trigger updates the balances)
INSERT INTO transactions (wallet_id, transaction_type_id, amount, description, transaction_date) VALUES
    (1, 1, 5000.00, 'Initial deposit',   '2026-09-01 10:00:00'),
    (2, 1, 3000.00, 'Initial deposit',   '2026-09-01 10:05:00'),
    (3, 1, 4000.00, 'Initial deposit',   '2026-09-01 10:10:00'),
    (4, 1, 2000.00, 'Initial deposit',   '2026-09-01 10:15:00'),
    (5, 1, 6000.00, 'Initial deposit',   '2026-09-01 10:20:00'),
    (6, 1, 1500.00, 'Initial deposit',   '2026-09-01 10:25:00'),
    (3, 1, 2500.00, 'Salary credit',     '2026-09-05 09:00:00'),
    (1, 2,  500.00, 'ATM withdrawal',    '2026-09-07 18:30:00'),
    (2, 2,  200.00, 'Mobile recharge',   '2026-09-08 12:00:00'),
    (5, 2, 1000.00, 'Rent payment',      '2026-09-10 08:45:00');

-- Transfers go through the stored procedure (creates 2 rows each)
CALL transfer_money(1, 2,  500.00, 'Dinner share');
CALL transfer_money(3, 4,  750.00, 'Book fees');
CALL transfer_money(5, 1, 1200.00, 'Loan repayment');
