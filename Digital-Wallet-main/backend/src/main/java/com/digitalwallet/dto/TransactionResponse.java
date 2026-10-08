package com.digitalwallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(Long id, Long walletId, String transactionType, BigDecimal amount,
                                  String description, LocalDateTime transactionDate) {
}
