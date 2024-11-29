package org.chenile.samples.secmonolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = { "org.chenile.samples.**.configuration",
		"org.chenile.configuration" })
public class SecmonolithApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SecmonolithApplication.class, args);
	}
}
