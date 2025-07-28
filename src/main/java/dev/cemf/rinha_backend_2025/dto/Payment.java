package dev.cemf.rinha_backend_2025.dto;

import java.math.BigDecimal;
import java.time.Instant;

/*
 * Usado para indexação via sorted set (ZSET)
 */
public record Payment(Instant requestedAt, BigDecimal amount, String correlationId) {
}
