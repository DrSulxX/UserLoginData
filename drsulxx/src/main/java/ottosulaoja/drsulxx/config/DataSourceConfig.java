package ottosulaoja.drsulxx.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
        "ottosulaoja.drsulxx.repository.usermanagement", // User management repositories
        "ottosulaoja.drsulxx.repository.security",       // Security repositories
        "ottosulaoja.drsulxx.repository.passwordreset"   // Password reset repositories
    },
    entityManagerFactoryRef = "kasutajaHalduseEntityManagerFactory",
    transactionManagerRef = "securityTransactionManager" // Unified transaction manager name
)
public class DataSourceConfig {

    @Bean(name = "kasutajaHalduseDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource kasutajaHalduseDataSource() {
        return DataSourceBuilder.create().type(com.zaxxer.hikari.HikariDataSource.class).build();
    }

    @Bean(name = "kasutajaHalduseEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean kasutajaHalduseEntityManagerFactory(
            @Qualifier("kasutajaHalduseDataSource") DataSource kasutajaHalduseDataSource) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(kasutajaHalduseDataSource);
        emf.setPackagesToScan(
            "ottosulaoja.drsulxx.model.usermanagement",   // User management entities
            "ottosulaoja.drsulxx.model.security",         // Security entities
            "ottosulaoja.drsulxx.model.passwordreset"     // Password reset entities
        );
        emf.setPersistenceUnitName("kasutaja_halduse_db");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");  // Validate schema but do not create/alter tables
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");

        emf.setJpaPropertyMap(properties);

        return emf;
    }

    @Bean(name = "securityTransactionManager") // Unified transaction manager bean name
    public PlatformTransactionManager securityTransactionManager(
            @Qualifier("kasutajaHalduseEntityManagerFactory") EntityManagerFactory kasutajaHalduseEntityManagerFactory) {
        return new JpaTransactionManager(kasutajaHalduseEntityManagerFactory);
    }
}