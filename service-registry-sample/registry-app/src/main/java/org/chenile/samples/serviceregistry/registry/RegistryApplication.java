package org.chenile.samples.serviceregistry.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "org.chenile.configuration",
        "org.chenile.service.registry"
})
@EntityScan(basePackages = "org.chenile.service.registry.model")
@EnableJpaRepositories(basePackages = "org.chenile.service.registry.configuration.dao")
public class RegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }
}
