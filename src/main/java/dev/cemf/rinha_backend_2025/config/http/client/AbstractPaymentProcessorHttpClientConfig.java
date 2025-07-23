package dev.cemf.rinha_backend_2025.config.http.client;

import dev.cemf.rinha_backend_2025.http.client.AbstractPaymentProcessorHttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public abstract class AbstractPaymentProcessorHttpClientConfig {

    private final String baseUrl;

    public AbstractPaymentProcessorHttpClientConfig(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected String getBaseUrl () { return this.baseUrl; }

    protected <T extends AbstractPaymentProcessorHttpClient> T buildPaymentProcessorClientBean(Class<T> clazz) {
        final var webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .build();
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(clazz);
    }
}
