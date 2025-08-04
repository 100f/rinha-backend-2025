package dev.cemf.rinha_backend_2025.config.http.router;

import dev.cemf.rinha_backend_2025.handler.PaymentSummaryAsyncHandler;
import dev.cemf.rinha_backend_2025.handler.RegisterPaymentAsyncHandler;
import dev.cemf.rinha_backend_2025.handler.ServiceHealthAsyncHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> apiRouter(RegisterPaymentAsyncHandler registerPaymentAsyncHandler,
                                                    PaymentSummaryAsyncHandler paymentSummaryAsyncHandler,
                                                    ServiceHealthAsyncHandler healthAsyncHandler) {
        return RouterFunctions.route()
                .POST("/payments", registerPaymentAsyncHandler)
                .GET("/payments-summary", paymentSummaryAsyncHandler)
                .GET("/health", healthAsyncHandler)
                .build();
    }
}
