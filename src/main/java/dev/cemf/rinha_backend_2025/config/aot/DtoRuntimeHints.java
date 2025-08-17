package dev.cemf.rinha_backend_2025.config.aot;

import dev.cemf.rinha_backend_2025.dto.Payment;
import dev.cemf.rinha_backend_2025.dto.PaymentProcessorHealthResponse;
import dev.cemf.rinha_backend_2025.dto.PaymentProcessorSummary;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class DtoRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerType(hints, Payment.class);
        registerType(hints, PaymentProcessorSummary.class);
        registerType(hints, PaymentProcessorHealthResponse.class);
    }

    private void registerType(RuntimeHints hints, Class<?> clazz) {
        hints.reflection().registerType(clazz, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS);
    }
}
