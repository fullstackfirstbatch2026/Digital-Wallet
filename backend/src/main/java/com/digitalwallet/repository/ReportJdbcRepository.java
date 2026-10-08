package com.digitalwallet.repository;

import com.digitalwallet.dto.AboveAverageTransactionDto;
import com.digitalwallet.dto.TransactionUserDetailsDto;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Plain-SQL reports: the JOIN query and the SUBQUERY query (Spring JDBC). */
@Repository
public class ReportJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** JOIN: USERS -> WALLETS -> TRANSACTIONS -> TRANSACTION_TYPES */
    public List<TransactionUserDetailsDto> findTransactionUserDetails() {
        String sql = """
                SELECT t.id               AS transaction_id,
                       u.id               AS user_id,
                       u.name             AS user_name,
                       u.email            AS user_email,
                       w.id               AS wallet_id,
                       tt.type_name       AS transaction_type,
                       t.amount           AS amount,
                       t.description      AS description,
                       t.transaction_date AS transaction_date
                FROM users u
                JOIN wallets w            ON w.user_id = u.id
                JOIN transactions t       ON t.wallet_id = w.id
                JOIN transaction_types tt ON tt.id = t.transaction_type_id
                ORDER BY t.transaction_date DESC, t.id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TransactionUserDetailsDto(
                rs.getLong("transaction_id"),
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getLong("wallet_id"),
                rs.getString("transaction_type"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getTimestamp("transaction_date").toLocalDateTime()));
    }

    /** SUBQUERY: transactions whose amount is greater than the average of ALL transactions. */
    public List<AboveAverageTransactionDto> findAboveAverageTransactions() {
        String sql = """
                SELECT u.id     AS user_id,
                       u.name   AS user_name,
                       u.email  AS email,
                       t.amount AS transaction_amount
                FROM users u
                JOIN wallets w      ON w.user_id = u.id
                JOIN transactions t ON t.wallet_id = w.id
                WHERE t.amount > (SELECT AVG(amount) FROM transactions)
                ORDER BY t.amount DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AboveAverageTransactionDto(
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("email"),
                rs.getBigDecimal("transaction_amount")));
    }
}
