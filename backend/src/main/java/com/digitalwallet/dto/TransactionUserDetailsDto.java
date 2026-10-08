package com.digitalwallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One row of the 4-table JOIN query. */
public record TransactionUserDetailsDto(Long transactionId, Long userId, String userName, String userEmail,
                                        Long walletId, String transactionType, BigDecimal amount,
                                        String description, LocalDateTime transactionDate) {
}
