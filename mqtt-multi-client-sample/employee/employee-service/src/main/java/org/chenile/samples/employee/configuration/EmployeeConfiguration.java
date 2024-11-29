package org.chenile.samples.employee.configuration;

import org.chenile.samples.employee.service.impl.BaseEntityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.chenile.samples.employee.service.EmployeeService;
import org.chenile.samples.employee.service.impl.EmployeeServiceImpl;
import org.chenile.samples.employee.service.healthcheck.EmployeeHealthChecker;

/**
 This is where you will instantiate all the required classes in Spring

*/
@Configuration
public class EmployeeConfiguration {
	@Bean public EmployeeService _employeeService_() {
		return new EmployeeServiceImpl();
	}

	@Bean
	EmployeeHealthChecker employeeHealthChecker(){
    	return new EmployeeHealthChecker();
    }

	@Bean
	BaseEntityHandler baseEntityHandler() { return new BaseEntityHandler();}
}
