package dev.cemf.rinha_backend_2025.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(String correlationId, BigDecimal amount, LocalDateTime requestedAt) {}
