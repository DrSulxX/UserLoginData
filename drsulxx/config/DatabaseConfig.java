package ottosulaoja.drsulxx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:C:/Users/DrSulxX/Desktop/JavaWorkSpace/db_credentials.properties")
public class DatabaseConfig {

    // User Management Database Properties (now unified)
    // The following fields are used to inject database credentials from an external properties file.
    @Value("${spring.datasource.hikari.username}")
    private String dbUsername;

    @Value("${spring.datasource.hikari.password}")
    private String dbPassword;

    @Value("${spring.datasource.hikari.jdbc-url}")
    private String dbUrl;

    // Getter for Database Username
    // Provides access to the database username.
    public String getDbUsername() {
        return dbUsername;
    }

    // Getter for Database Password
    // Provides access to the database password.
    public String getDbPassword() {
        return dbPassword;
    }

    // Getter for Database URL
    // Provides access to the database URL.
    public String getDbUrl() {
        return dbUrl;
    }
}