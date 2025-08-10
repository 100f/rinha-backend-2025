package dev.cemf.rinha_backend_2025.http.client;

import dev.cemf.rinha_backend_2025.dto.PaymentProcessorHealthResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public non-sealed interface FallbackPaymentProcessorHttpClient extends AbstractPaymentProcessorHttpClient {

    @PostExchange(value = "/payments", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<Void> registerPayment(@RequestBody String payment);
}
