package dev.cemf.rinha_backend_2025.config.keydb;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyDbConfig {

    private final String keyDbUri;

    public KeyDbConfig(@Value("${config.key-db.uri}") String keyDbUri) {
        this.keyDbUri = keyDbUri;
    }

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        return RedisClient.create(keyDbUri);
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> redisConnection(final RedisClient redisClient) {
        return redisClient.connect();
    }

    @Bean
    public RedisReactiveCommands<String, String> redisCommands(final StatefulRedisConnection<String, String> redisConnection) {
        return redisConnection.reactive();
    }
}
