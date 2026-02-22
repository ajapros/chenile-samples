package com.mycompany.myorg.vehicle;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import org.chenile.utils.entity.service.EntityStore;
import com.mycompany.myorg.vehicle.model.Vehicle;


@Configuration
@PropertySource("classpath:com/mycompany/myorg/vehicle/TestService.properties")
@SpringBootApplication(scanBasePackages = { "org.chenile.configuration", "com.mycompany.myorg.vehicle.configuration" })
@ActiveProfiles("unittest")
public class SpringTestConfig extends SpringBootServletInitializer{
	
}

