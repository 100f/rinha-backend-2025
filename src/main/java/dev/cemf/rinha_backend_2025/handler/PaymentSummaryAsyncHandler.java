package dev.cemf.rinha_backend_2025.handler;

import dev.cemf.rinha_backend_2025.dto.PaymentProcessorSummary;
import io.lettuce.core.Range;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class PaymentSummaryAsyncHandler implements HandlerFunction<ServerResponse> {

    private final RedisReactiveCommands<String, String> commands;
    private final String defaultRegisteredPaymentsByDateKey;
    private final String fallbackRegisteredPaymentsByDateKey;

    public PaymentSummaryAsyncHandler(@Value("${config.key-db.set.payments-by-date.key.default}") String defaultRegisteredPaymentsByDateKey,
                                      @Value("${config.key-db.set.payments-by-date.key.fallback}") String fallbackRegisteredPaymentsByDateKey,
                                      RedisReactiveCommands<String, String> commands) {
        this.defaultRegisteredPaymentsByDateKey = defaultRegisteredPaymentsByDateKey;
        this.fallbackRegisteredPaymentsByDateKey = fallbackRegisteredPaymentsByDateKey;
        this.commands = commands;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        final var fromMono = parseParamToMillis(request, "from", 0L);
        final var toMono = parseParamToMillis(request, "to", Long.MAX_VALUE);
        return Mono.zip(fromMono, toMono)
                .flatMap(tuple -> {
                    final var from = tuple.getT1();
                    final var to = tuple.getT2();
                    final var range = Range.create(from, to);
                    final var defaultSummary = getProcessorSummary(defaultRegisteredPaymentsByDateKey, range);
                    final var fallbackSummary = getProcessorSummary(fallbackRegisteredPaymentsByDateKey, range);
                    return Mono.zip(defaultSummary, fallbackSummary)
                            .map(summaryTuple -> Map.of("default", summaryTuple.getT1(), "fallback", summaryTuple.getT2()))
                            .flatMap(paymentsSummary -> ServerResponse.ok().bodyValue(paymentsSummary));
                })
                .onErrorResume(DateTimeParseException.class, ignored ->
                        ServerResponse.badRequest().bodyValue("Formato de data inválido."))
                .onErrorResume(ignored ->
                        ServerResponse.status(500).bodyValue("Erro interno desconhecido."));
    }

    private Mono<PaymentProcessorSummary> getProcessorSummary(String key, Range<Long> range) {
        return commands.zrangebyscore(key, range)
                .collectList()
                .map(entries -> {
                    var totalAmount = BigDecimal.ZERO;
                    var processedPayments = 0L;
                    for (final var entry : entries) {
                        final var colonPos = entry.indexOf(":");
                        if (colonPos > 0) {
                            try {
                                totalAmount = totalAmount.add(new BigDecimal(entry.substring(0, colonPos)));
                                processedPayments++;
                            }
                            catch (NumberFormatException ignored) {}
                        }
                    }
                    return new PaymentProcessorSummary(processedPayments, totalAmount);
                })
                .defaultIfEmpty(new PaymentProcessorSummary(0L, BigDecimal.ZERO));
    }

    private Mono<Long> parseParamToMillis(ServerRequest request, String field, long defaultValue) {
        return Mono.fromCallable(() -> request.queryParam(field)
                .map(Instant::parse)
                .map(instant -> instant.atZone(ZoneOffset.UTC))
                .map(ZonedDateTime::toInstant)
                .map(Instant::toEpochMilli)
                .orElse(defaultValue));
    }
}
