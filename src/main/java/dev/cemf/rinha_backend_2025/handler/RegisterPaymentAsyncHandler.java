package dev.cemf.rinha_backend_2025.handler;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class RegisterPaymentAsyncHandler implements HandlerFunction<ServerResponse> {

    private final RedisClient redisClient;

    private final StatefulRedisConnection<String, String> connection;

    private final String paymentsQueueName;

    private final RedisReactiveCommands<String, String> commands;

    public RegisterPaymentAsyncHandler(@Value("${config.key-db.uri}") String keyDbUri,
                                       @Value("${config.key-db.payments-queue-name}") String paymentsQueueName) {
        this.redisClient = RedisClient.create(keyDbUri);
        this.paymentsQueueName = paymentsQueueName;
        this.connection = this.redisClient.connect();
        this.commands = this.connection.reactive();
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        return request.bodyToMono(String.class)
                .flatMap(body -> commands.lpush(paymentsQueueName, body))
                .then(ServerResponse.accepted().build());
    }

    @PreDestroy
    public void finish() {
        this.connection.close();
        this.redisClient.shutdown();
    }
}
