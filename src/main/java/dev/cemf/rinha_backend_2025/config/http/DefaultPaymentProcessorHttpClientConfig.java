package dev.cemf.rinha_backend_2025.config.http;

import dev.cemf.rinha_backend_2025.http.client.DefaultPaymentProcessorHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultPaymentProcessorHttpClientConfig extends AbstractPaymentProcessorHttpClientConfig {
    public DefaultPaymentProcessorHttpClientConfig(@Value("${http.config.default-payment-processor.base-url}") String baseUrl) {
        super(baseUrl);
    }

    @Bean
    public DefaultPaymentProcessorHttpClient defaultPaymentProcessorHttpClient() {
        return buildPaymentProcessorClientBean(DefaultPaymentProcessorHttpClient.class);
    }
}
