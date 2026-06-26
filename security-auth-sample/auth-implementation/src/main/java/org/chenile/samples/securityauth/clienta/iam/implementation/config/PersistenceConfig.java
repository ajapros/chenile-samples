package org.chenile.samples.securityauth.clienta.iam.implementation.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
@EntityScan("org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity")
@EnableJpaRepositories("org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository")
public class PersistenceConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration(proxyBeanMethods = false)
    static class JpaInitializationOrderConfiguration extends EntityManagerFactoryDependsOnPostProcessor {

        JpaInitializationOrderConfiguration() {
            super(SpringLiquibase.class);
        }
    }
}
