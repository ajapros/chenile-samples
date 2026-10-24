package org.chenile.samples.p1Worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = { "org.chenile.samples.**.configuration",
        "org.chenile.service.registry.configuration",
		"org.chenile.configuration" })
public class P1WorkerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(P1WorkerApplication.class, args);
	}
}
