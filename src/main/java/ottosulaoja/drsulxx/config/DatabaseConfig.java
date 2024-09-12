package ottosulaoja.drsulxx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:C:/Users/DrSulxX/Desktop/JavaProjectWorkSpace/db_credentials.properties")  // Ensure this path is correct
public class DatabaseConfig {

    // Primary Database Properties
    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.url}")
    private String dbUrl;

    // Backup Database Properties
    @Value("${db.backup.username}")
    private String backupDbUsername;

    @Value("${db.backup.password}")
    private String backupDbPassword;

    @Value("${db.backup.url}")
    private String backupDbUrl;

    // Getters for Primary Database Properties with debug print statements
    public String getDbUsername() {
        System.out.println("Primary DB Username: " + dbUsername);  // Debug output
        return dbUsername;
    }

    public String getDbPassword() {
        System.out.println("Primary DB Password: " + dbPassword);  // Debug output
        return dbPassword;
    }

    public String getDbUrl() {
        System.out.println("Primary DB URL: " + dbUrl);  // Debug output
        return dbUrl;
    }

    // Getters for Backup Database Properties with debug print statements
    public String getBackupDbUsername() {
        System.out.println("Backup DB Username: " + backupDbUsername);  // Debug output
        return backupDbUsername;
    }

    public String getBackupDbPassword() {
        System.out.println("Backup DB Password: " + backupDbPassword);  // Debug output
        return backupDbPassword;
    }

    public String getBackupDbUrl() {
        System.out.println("Backup DB URL: " + backupDbUrl);  // Debug output
        return backupDbUrl;
    }
}
