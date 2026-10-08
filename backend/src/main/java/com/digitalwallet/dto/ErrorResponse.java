package com.digitalwallet.dto;

import java.time.LocalDateTime;

/** Consistent JSON error body: {"timestamp": "...", "status": 400, "message": "..."} */
public record ErrorResponse(LocalDateTime timestamp, int status, String message) {
}
