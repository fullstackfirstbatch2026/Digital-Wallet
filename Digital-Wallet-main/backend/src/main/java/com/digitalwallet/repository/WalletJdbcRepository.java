package com.digitalwallet.repository;

import java.math.BigDecimal;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Calls the MySQL stored procedure transfer_money and the function get_wallet_balance. */
@Repository
public class WalletJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public WalletJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * CALL transfer_money(sender, receiver, amount, description).
     * The procedure manages its own START TRANSACTION / COMMIT / ROLLBACK.
     */
    public void transferMoney(Long senderWalletId, Long receiverWalletId, BigDecimal amount, String description) {
        jdbcTemplate.execute("{call transfer_money(?, ?, ?, ?)}", (CallableStatementCallback<Void>) cs -> {
            cs.setLong(1, senderWalletId);
            cs.setLong(2, receiverWalletId);
            cs.setBigDecimal(3, amount);
            cs.setString(4, description);
            cs.execute();
            return null;
        });
    }

    /** SELECT get_wallet_balance(?) */
    public BigDecimal getWalletBalance(Long walletId) {
        return jdbcTemplate.queryForObject("SELECT get_wallet_balance(?)", BigDecimal.class, walletId);
    }
}
