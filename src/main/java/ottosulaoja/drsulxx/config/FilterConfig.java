package ottosulaoja.drsulxx.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    // No need to explicitly define the LoggingFilter bean here
    // Spring Boot will automatically detect and register it due to the @Component annotation in LoggingFilter
}