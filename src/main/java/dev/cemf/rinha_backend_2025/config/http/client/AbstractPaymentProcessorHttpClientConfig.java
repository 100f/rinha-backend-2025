package dev.cemf.rinha_backend_2025.config.http.client;

import dev.cemf.rinha_backend_2025.http.client.AbstractPaymentProcessorHttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public abstract class AbstractPaymentProcessorHttpClientConfig {

    private final String baseUrl;
    private final long requestTimeoutInMs;

    public AbstractPaymentProcessorHttpClientConfig(String baseUrl, long requestTimeoutInMs) {
        this.baseUrl = baseUrl;
        this.requestTimeoutInMs = requestTimeoutInMs;
    }

    protected String getBaseUrl () { return this.baseUrl; }

    protected <T extends AbstractPaymentProcessorHttpClient> T buildPaymentProcessorClientBean(Class<T> clazz) {
        final var httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(requestTimeoutInMs));
        final var webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(headers -> headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .build();
        return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(clazz);
    }
}
