-- =====================================================================
-- triggers.sql : keeps wallets.balance in sync with the transactions table
--
--  DEPOSIT   -> balance + amount
--  WITHDRAW  -> balance - amount (rejected with error 50003 if funds are insufficient)
--  TRANSFER  -> NO-OP here. transfer_money() already moved the money on both wallets,
--               so updating again would double count.
-- =====================================================================
USE digital_wallet;

DROP TRIGGER IF EXISTS trg_transactions_after_insert;

DELIMITER $$
CREATE TRIGGER trg_transactions_after_insert
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    DECLARE v_type_name VARCHAR(50);

    SELECT type_name INTO v_type_name
    FROM transaction_types
    WHERE id = NEW.transaction_type_id;

    IF v_type_name = 'DEPOSIT' THEN
        UPDATE wallets SET balance = balance + NEW.amount WHERE id = NEW.wallet_id;

    ELSEIF v_type_name = 'WITHDRAW' THEN
        -- The WHERE clause makes the "enough money?" check atomic
        UPDATE wallets
        SET balance = balance - NEW.amount
        WHERE id = NEW.wallet_id AND balance >= NEW.amount;

        IF ROW_COUNT() = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Insufficient wallet balance', MYSQL_ERRNO = 50003;
        END IF;
    END IF;
    -- TRANSFER: intentionally nothing (handled by transfer_money)
END$$
DELIMITER ;
