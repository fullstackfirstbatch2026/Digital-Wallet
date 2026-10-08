package com.digitalwallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(Long id, Long userId, String userName, BigDecimal balance, LocalDateTime createdAt) {
}
