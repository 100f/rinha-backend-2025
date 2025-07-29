package dev.cemf.rinha_backend_2025.dto;

import java.math.BigDecimal;

public record PaymentSummaryResponse(long totalRequests, BigDecimal totalAmount) {
}
