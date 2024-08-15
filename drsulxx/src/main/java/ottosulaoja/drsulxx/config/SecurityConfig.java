package ottosulaoja.drsulxx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection for simplicity and faster development
            .sessionManagement(sessionManagement -> 
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Configure API to be stateless (no session management)
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/admin/**").authenticated()  // Ensure admin routes require authentication
                    .requestMatchers("/api/users/bulk/**").permitAll()  // Allow public access for bulk user operations, useful for testing and development
                    .requestMatchers("/api/users/**").permitAll()  // Allow public access for user-related operations, useful for testing and development
                    .requestMatchers("/api/delete/**").permitAll()  // Allow public access for delete operations, useful for testing and development
                    .requestMatchers("/api/users/update/**").permitAll()  // Allow public access for update operations, useful for testing and development
                    .requestMatchers("/api/auth/login").permitAll()  // Allow public access to login endpoint for user authentication
                    .requestMatchers("/api/search/**").permitAll()  // Allow public access to search endpoints
                    .requestMatchers("/error").permitAll()  // Allow public access to error handling page
                    .requestMatchers("/createuser/**", "/home/**", "/search/**", "/login/**").permitAll()  // Allow public access to these endpoints for ease of use during development and testing
                    .anyRequest().authenticated()  // All other requests require authentication
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Add CORS configuration
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login/login.html")  // Custom login page URL for HTML login
                    .loginProcessingUrl("/perform_login")  // URL to submit the login form
                    .defaultSuccessUrl("/home/home.html", true)  // Redirect after successful login
                    .failureUrl("/login/login.html?error=true")  // Redirect on login failure
                    .permitAll()  // Allow access to login pages and processing URL without authentication
            )
            .logout(logout ->
                logout
                    .logoutUrl("/logout")  // URL to trigger logout
                    .logoutSuccessUrl("/login/login.html?logout=true")  // Redirect after logout
                    .permitAll()  // Allow access to logout URL without authentication
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Bean for password encoding
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://192.168.8.100:8080", "http://localhost:8080", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}