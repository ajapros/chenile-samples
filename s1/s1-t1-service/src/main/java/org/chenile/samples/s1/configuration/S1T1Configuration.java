package org.chenile.samples.s1.configuration;

import org.chenile.core.annotation.ConditionalHealthCheckOnTrajectory;
import org.chenile.core.annotation.ConditionalOnTrajectory;
import org.chenile.samples.s1.service.impl.S1T1ServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.chenile.samples.s1.service.S1Service;
import org.chenile.samples.s1.service.impl.S1ServiceImpl;
import org.chenile.samples.s1.service.healthcheck.S1T1HealthChecker;

/**
 In this case we instantiate the two beans with a condition that they are
 applicable for s1Service only for trajectory t1.
 We are not re-defining a new controller. Instead we are making the existing
 controller to behave differently for trajectory t1

*/
@Configuration
public class S1T1Configuration {
	@Bean
	@ConditionalOnTrajectory(id = "t1", service = "s1Service") public S1Service s1T1Service() {
		return new S1T1ServiceImpl();
	}

	@Bean
	@ConditionalHealthCheckOnTrajectory(id="t1", service="s1Service")
    S1T1HealthChecker s1T1HealthChecker(){
    	return new S1T1HealthChecker();
    }
}
