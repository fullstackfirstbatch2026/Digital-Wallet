-- =====================================================================
-- procedures.sql : transfer_money
--
-- CALL transfer_money(sender_wallet_id, receiver_wallet_id, amount, description);
--
-- Custom error numbers (read by the Spring Boot backend):
--   50001 wallet not found | 50002 invalid amount
--   50003 insufficient balance | 50004 same wallet / invalid transfer
-- =====================================================================
USE digital_wallet;

DROP PROCEDURE IF EXISTS transfer_money;

DELIMITER $$
CREATE PROCEDURE transfer_money(
    IN p_sender_wallet_id   BIGINT,
    IN p_receiver_wallet_id BIGINT,
    IN p_amount             DECIMAL(15,2),
    IN p_description        VARCHAR(255)
)
BEGIN
    DECLARE v_wallet_count     INT;
    DECLARE v_sender_balance   DECIMAL(15,2);
    DECLARE v_transfer_type_id INT;

    -- Step 11: if ANY statement fails, undo everything and re-raise the error
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Step 5 + basic validation (before touching any data)
    IF p_amount IS NULL OR p_amount <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Transfer amount must be greater than zero', MYSQL_ERRNO = 50002;
    END IF;

    IF p_sender_wallet_id = p_receiver_wallet_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sender and receiver wallets cannot be the same', MYSQL_ERRNO = 50004;
    END IF;

    -- Step 10: everything below runs in ONE database transaction
    START TRANSACTION;

    -- Step 4: lock both wallet rows (always in id order -> no deadlocks) and make sure both exist
    SELECT COUNT(*) INTO v_wallet_count
    FROM wallets
    WHERE id IN (p_sender_wallet_id, p_receiver_wallet_id)
    FOR UPDATE;

    IF v_wallet_count <> 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Sender or receiver wallet not found', MYSQL_ERRNO = 50001;
    END IF;

    -- Step 6: sufficient balance?
    SELECT balance INTO v_sender_balance FROM wallets WHERE id = p_sender_wallet_id;
    IF v_sender_balance < p_amount THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient wallet balance', MYSQL_ERRNO = 50003;
    END IF;

    SELECT id INTO v_transfer_type_id FROM transaction_types WHERE type_name = 'TRANSFER';
    IF v_transfer_type_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Transaction type TRANSFER is missing', MYSQL_ERRNO = 50004;
    END IF;

    -- Steps 7 + 8: move the money
    UPDATE wallets SET balance = balance - p_amount WHERE id = p_sender_wallet_id;
    UPDATE wallets SET balance = balance + p_amount WHERE id = p_receiver_wallet_id;

    -- Step 9: two history rows (debit side + credit side).
    -- The trigger IGNORES TRANSFER rows, so balances are NOT updated twice.
    INSERT INTO transactions (wallet_id, transaction_type_id, amount, description)
    VALUES (p_sender_wallet_id, v_transfer_type_id, p_amount,
            CONCAT(COALESCE(p_description, 'Money transfer'), ' (sent to wallet ', p_receiver_wallet_id, ')'));

    INSERT INTO transactions (wallet_id, transaction_type_id, amount, description)
    VALUES (p_receiver_wallet_id, v_transfer_type_id, p_amount,
            CONCAT(COALESCE(p_description, 'Money transfer'), ' (received from wallet ', p_sender_wallet_id, ')'));

    COMMIT;
END$$
DELIMITER ;
