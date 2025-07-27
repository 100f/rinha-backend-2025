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
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class RegisterPaymentMessageConsumer {

    private final String paymentsQueueName;
    private final String registeredPaymentsQueueName;
    private final int consumersAmount;
    private final int consumersConcurrencyFactor;
    private final RedisReactiveCommands<String, String> commands;
    private final DefaultPaymentProcessorHttpClient defaultPaymentProcessorHttpClient;
    private final FallbackPaymentProcessorHttpClient fallbackPaymentProcessorHttpClient;
    private Disposable runningSubscription;

    public RegisterPaymentMessageConsumer(@Value("${config.key-db.queue.pending-registration-payments.name}") String pendingRegistrationPaymentsQueue,
                                          @Value("${config.key-db.queue.registered-payments.name}") String registeredPaymentsQueue,
                                          @Value("${config.key-db.queue.pending-registration-payments.consumers-amount}") int consumersAmount,
                                          @Value("${config.key-db.queue.pending-registration-payments.consumers-concurrency-factor}") int consumersConcurrencyFactor,
                                          DefaultPaymentProcessorHttpClient defaultPaymentProcessorHttpClient,
                                          FallbackPaymentProcessorHttpClient fallbackPaymentProcessorHttpClient,
                                          RedisReactiveCommands<String, String> commands) {
        this.paymentsQueueName = pendingRegistrationPaymentsQueue;
        this.registeredPaymentsQueueName = registeredPaymentsQueue;
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
                .concatMap(t -> commands.rpop(paymentsQueueName), consumersConcurrencyFactor)
                .filter(Objects::nonNull)
                .flatMap(this::doProcessPayment, consumersAmount)
                .subscribeOn(Schedulers.parallel()); // cpu-bound
    }

    private Mono<Void> doProcessPayment(String paymentJson) {
        return defaultPaymentProcessorHttpClient.registerPayment(paymentJson)
                .then(sendToRegisteredPaymentsQueue(paymentJson))
                .onErrorResume(ignored -> fallbackPaymentProcessorHttpClient.registerPayment(paymentJson)
                        .then(sendToRegisteredPaymentsQueue(paymentJson))
                        .onErrorResume(ignoredFallback -> requeue(paymentJson)));
    }

    private Mono<Void> requeue(String paymentJson) {
        return commands.lpush(paymentsQueueName, paymentJson).then();
    }

    private Mono<Void> sendToRegisteredPaymentsQueue(String paymentJson) {
        return commands.lpush(registeredPaymentsQueueName, paymentJson).then();
    }

    @PreDestroy
    public void shutdown() {
        this.runningSubscription.dispose();
    }

}
