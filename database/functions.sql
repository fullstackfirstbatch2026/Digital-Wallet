-- =====================================================================
-- functions.sql : get_wallet_balance(wallet_id)
-- Usage: SELECT get_wallet_balance(1);
-- Returns NULL when the wallet does not exist.
-- =====================================================================
USE digital_wallet;

DROP FUNCTION IF EXISTS get_wallet_balance;

DELIMITER $$
CREATE FUNCTION get_wallet_balance(p_wallet_id BIGINT)
RETURNS DECIMAL(15,2)
READS SQL DATA
BEGIN
    DECLARE v_balance DECIMAL(15,2);

    SELECT balance INTO v_balance
    FROM wallets
    WHERE id = p_wallet_id;

    RETURN v_balance;
END$$
DELIMITER ;
