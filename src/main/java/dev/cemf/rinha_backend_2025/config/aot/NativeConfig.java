package dev.cemf.rinha_backend_2025.config.aot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(DtoRuntimeHints.class)
public class NativeConfig {
    //NOOP
}
