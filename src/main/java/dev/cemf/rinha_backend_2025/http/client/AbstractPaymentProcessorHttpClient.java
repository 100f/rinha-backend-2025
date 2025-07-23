package dev.cemf.rinha_backend_2025.http.client;

public sealed interface AbstractPaymentProcessorHttpClient
        permits DefaultPaymentProcessorHttpClient, FallbackPaymentProcessorHttpClient {
}
