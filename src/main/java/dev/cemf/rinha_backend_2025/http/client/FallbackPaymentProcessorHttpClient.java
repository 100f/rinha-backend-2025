package dev.cemf.rinha_backend_2025.http.client;

import dev.cemf.rinha_backend_2025.dto.Payment;
import dev.cemf.rinha_backend_2025.dto.PaymentProcessorHealthResponse;
import org.springframework.http.MediaType;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public non-sealed interface FallbackPaymentProcessorHttpClient extends AbstractPaymentProcessorHttpClient {

    @PostExchange(value = "/payments", contentType = MediaType.APPLICATION_JSON_VALUE)
    void registerPayment(Payment payment);

    @GetExchange("/payments/service-health")
    PaymentProcessorHealthResponse health();
}
