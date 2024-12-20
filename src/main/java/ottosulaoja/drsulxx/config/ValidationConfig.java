package ottosulaoja.drsulxx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

@Configuration
public class ValidationConfig {

    /**
     * Configures a Validator bean for the application.
     * 
     * This Validator will be used to perform validation on Java Beans
     * using annotations such as @NotBlank, @Size, and @Email.
     * 
     * It is used in:
     * - Controller methods with @Valid or @Validated.
     * - Service layer for validating business logic or DTOs.
     * - Custom validation scenarios.
     * 
     * @return a Validator instance.
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}