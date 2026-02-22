package com.mycompany.myorg.vehicle.extension;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@PropertySource("classpath:com/mycompany/myorg/vehicle/TestService.properties")
@SpringBootApplication(scanBasePackages = {
        "org.chenile.configuration",
        "com.mycompany.myorg.vehicle.configuration",
        "com.mycompany.myorg.vehicle.extension.configuration"
})
@EntityScan(basePackages = "com.mycompany.myorg.vehicle.model")
@EnableJpaRepositories(basePackages = "com.mycompany.myorg.vehicle.configuration.dao")
@ActiveProfiles("unittest")
public class VehicleExtensionSpringTestConfig extends SpringBootServletInitializer {
}
