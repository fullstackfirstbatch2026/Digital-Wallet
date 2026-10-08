package com.digitalwallet.dto;

import java.math.BigDecimal;

public record TransferResponse(String message, Long senderWalletId, Long receiverWalletId, BigDecimal amount,
                               BigDecimal senderBalance, BigDecimal receiverBalance) {
}
