package org.chenile.samples.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {
		"org.chenile.samples.scheduler",
		"org.chenile.configuration"
})
public class SchedulerSampleApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(SchedulerSampleApplication.class, args);
	}
}
