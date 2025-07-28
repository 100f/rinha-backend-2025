package dev.cemf.rinha_backend_2025.consumer;

import dev.cemf.rinha_backend_2025.http.client.DefaultPaymentProcessorHttpClient;
import dev.cemf.rinha_backend_2025.http.client.FallbackPaymentProcessorHttpClient;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static dev.cemf.rinha_backend_2025.helper.JsonHelper.addRequestedAtFieldTo;

@Component
public class RegisterPaymentMessageConsumer {

    private final String pendingRegistrationPaymentsKey;
    private final String defaultRegisteredPaymentsByDateKey;
    private final String fallbackRegisteredPaymentsByDateKey;
    private final int consumersAmount;
    private final int consumersConcurrencyFactor;
    private final RedisReactiveCommands<String, String> commands;
    private final DefaultPaymentProcessorHttpClient defaultPaymentProcessorHttpClient;
    private final FallbackPaymentProcessorHttpClient fallbackPaymentProcessorHttpClient;
    private Disposable runningSubscription;

    public RegisterPaymentMessageConsumer(@Value("${config.key-db.queue.pending-registration-payments.key}") String pendingRegistrationPaymentsQueue,
                                          @Value("${config.key-db.set.payments-by-date.key.default}") String defaultRegisteredPaymentsByDateKey,
                                          @Value("${config.key-db.set.payments-by-date.key.fallback}") String fallbackRegisteredPaymentsByDateKey,
                                          @Value("${config.key-db.queue.pending-registration-payments.consumers-amount}") int consumersAmount,
                                          @Value("${config.key-db.queue.pending-registration-payments.consumers-concurrency-factor}") int consumersConcurrencyFactor,
                                          DefaultPaymentProcessorHttpClient defaultPaymentProcessorHttpClient,
                                          FallbackPaymentProcessorHttpClient fallbackPaymentProcessorHttpClient,
                                          RedisReactiveCommands<String, String> commands) {
        this.pendingRegistrationPaymentsKey = pendingRegistrationPaymentsQueue;
        this.defaultRegisteredPaymentsByDateKey = defaultRegisteredPaymentsByDateKey;
        this.fallbackRegisteredPaymentsByDateKey = fallbackRegisteredPaymentsByDateKey;
        this.consumersAmount = consumersAmount;
        this.consumersConcurrencyFactor = consumersConcurrencyFactor;
        this.defaultPaymentProcessorHttpClient = defaultPaymentProcessorHttpClient;
        this.fallbackPaymentProcessorHttpClient = fallbackPaymentProcessorHttpClient;
        this.commands = commands;
    }


    @PostConstruct
    public void init() {
        this.runningSubscription = createPaymentsProcessingPipeline()
                .subscribe();
    }

    private Flux<Void> createPaymentsProcessingPipeline() {
        return Flux.interval(Duration.of(100, ChronoUnit.MILLIS))
                .publishOn(Schedulers.boundedElastic()) // bloqueantes
                .concatMap(t -> commands.rpop(pendingRegistrationPaymentsKey), consumersConcurrencyFactor)
                .filter(Objects::nonNull)
                .flatMap(this::doProcessPayment, consumersAmount)
                .subscribeOn(Schedulers.parallel()); // cpu-bound
    }


    private Mono<Void> doProcessPayment(String originalPaymentJson) {
        final var timestampedPaymentJson = addRequestedAtFieldTo(originalPaymentJson);
        System.out.println(timestampedPaymentJson);
        return defaultPaymentProcessorHttpClient.registerPayment(timestampedPaymentJson)
                .then(indexPayment(timestampedPaymentJson, defaultRegisteredPaymentsByDateKey))
                .onErrorResume(ignored -> fallbackPaymentProcessorHttpClient.registerPayment(timestampedPaymentJson)
                        .then(indexPayment(timestampedPaymentJson, fallbackRegisteredPaymentsByDateKey))
                        .onErrorResume(ignoredFallback -> requeue(originalPaymentJson)));
    }

    private Mono<Void> indexPayment(String paymentJson, String key) {
        return commands.lpush(key, paymentJson).then();
    }

    private Mono<Void> requeue(String paymentJson) {
        return commands.lpush(pendingRegistrationPaymentsKey, paymentJson).then();
    }

    @PreDestroy
    public void shutdown() {
        this.runningSubscription.dispose();
    }

}
