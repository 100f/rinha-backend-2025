package dev.cemf.rinha_backend_2025.handler;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RegisterPaymentAsyncHandler implements HandlerFunction<ServerResponse> {

    private final String paymentsQueueName;
    private final long publisherTimeoutInMs;
    private final RedisReactiveCommands<String, String> commands;

    public RegisterPaymentAsyncHandler(@Value("${config.key-db.queue.pending-registration-payments.name}") String paymentsQueueName,
                                       @Value("${config.key-db.queue.pending-registration-payments.producer-timeout-ms}") long publisherTimeoutInMs,
                                       RedisReactiveCommands<String, String> commands) {
        this.paymentsQueueName = paymentsQueueName;
        this.publisherTimeoutInMs = publisherTimeoutInMs;
        this.commands = commands;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        return request.bodyToMono(String.class)
                .timeout(Duration.ofMillis(publisherTimeoutInMs))
                .flatMap(body -> commands.lpush(paymentsQueueName, body)
                        .onErrorResume(e -> Mono.just(0L)))
                .then(ServerResponse.accepted().build())
                .onErrorResume(e -> ServerResponse.status(500).build()); //em tese isso aqui nunca deve ocorrer, salvo se orquestração estiver errada
    }

}
