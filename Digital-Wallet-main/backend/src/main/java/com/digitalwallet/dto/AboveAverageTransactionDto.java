package com.digitalwallet.dto;

import java.math.BigDecimal;

/** One row of the subquery result. */
public record AboveAverageTransactionDto(Long userId, String userName, String email, BigDecimal transactionAmount) {
}
