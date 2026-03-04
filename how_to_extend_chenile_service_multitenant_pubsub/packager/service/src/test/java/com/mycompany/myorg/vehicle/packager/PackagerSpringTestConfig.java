package com.mycompany.myorg.vehicle.packager;

import org.chenile.configuration.multids.MultiTenantDataSourceConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@PropertySource("classpath:com/mycompany/myorg/vehicle/TestService.properties")
@SpringBootApplication(scanBasePackages = {
        "org.chenile",
        "com.mycompany"
})
@EntityScan(basePackages = "com.mycompany.myorg.vehicle")
@EnableJpaRepositories(basePackages = "com.mycompany.myorg.vehicle")
@Import(MultiTenantDataSourceConfiguration.class)
public class PackagerSpringTestConfig extends SpringBootServletInitializer {
}
