package dev.cemf.rinha_backend_2025.config.http.client;

import dev.cemf.rinha_backend_2025.http.client.AbstractPaymentProcessorHttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

public abstract class AbstractPaymentProcessorHttpClientConfig {

    private final String baseUrl;
    private final long requestTimeoutInMs;
    private final int maxConnections;
    private final long connectionAcquireTimeoutInMs;

    public AbstractPaymentProcessorHttpClientConfig(String baseUrl, long requestTimeoutInMs, int maxConnections, long connectionAcquireTimeoutInMs) {
        this.baseUrl = baseUrl;
        this.requestTimeoutInMs = requestTimeoutInMs;
        this.maxConnections = maxConnections;
        this.connectionAcquireTimeoutInMs = connectionAcquireTimeoutInMs;
    }


    protected <T extends AbstractPaymentProcessorHttpClient> T buildPaymentProcessorClientBean(Class<T> clazz) {
        final var connectionProvider = ConnectionProvider.builder("A pool é vasta")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofMillis(connectionAcquireTimeoutInMs))
                .build();
        final var httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMillis(requestTimeoutInMs))
                .keepAlive(true)
                .compress(true);
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
