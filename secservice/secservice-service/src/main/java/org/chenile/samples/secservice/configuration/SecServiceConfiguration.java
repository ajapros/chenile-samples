package org.chenile.samples.secservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.chenile.samples.secservice.service.SecService;
import org.chenile.samples.secservice.service.impl.SecServiceImpl;
import org.chenile.samples.secservice.service.healthcheck.SecServiceHealthChecker;

/**
 This is where you will instantiate all the required classes in Spring

*/
@Configuration
public class SecServiceConfiguration {
	@Bean public SecService _secService_() {
		return new SecServiceImpl();
	}

	@Bean
	SecServiceHealthChecker secServiceHealthChecker(){
    	return new SecServiceHealthChecker();
    }
}
