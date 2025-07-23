package dev.cemf.rinha_backend_2025.config.http;

import dev.cemf.rinha_backend_2025.http.client.FallbackPaymentProcessorHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackPaymentProcessorHttpClientConfig extends AbstractPaymentProcessorHttpClientConfig {
    public FallbackPaymentProcessorHttpClientConfig(@Value("${http.config.fallback-payment-processor.base-url}") String baseUrl) {
        super(baseUrl);
    }

    @Bean
    public FallbackPaymentProcessorHttpClient fallbackPaymentProcessorHttpClient() {
        return buildPaymentProcessorClientBean(FallbackPaymentProcessorHttpClient.class);
    }
}
