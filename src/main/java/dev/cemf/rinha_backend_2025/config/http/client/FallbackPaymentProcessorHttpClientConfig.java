package dev.cemf.rinha_backend_2025.config.http.client;

import dev.cemf.rinha_backend_2025.http.client.FallbackPaymentProcessorHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackPaymentProcessorHttpClientConfig extends AbstractPaymentProcessorHttpClientConfig {

    public FallbackPaymentProcessorHttpClientConfig(@Value("${http.config.fallback-payment-processor.base-url}") String baseUrl,
                                                    @Value("${http.config.response-timeout-ms}") long responseTimeoutMs,
                                                    @Value("${http.config.max-connections}") int maxConnections,
                                                    @Value("${http.config.connection-acquire-timeout-ms}") long connectionAcquireTimeoutInMs) {
        super(baseUrl, responseTimeoutMs, maxConnections, connectionAcquireTimeoutInMs);
    }

    @Bean
    public FallbackPaymentProcessorHttpClient fallbackPaymentProcessorHttpClient() {
        return buildPaymentProcessorClientBean(FallbackPaymentProcessorHttpClient.class);
    }
}
