package org.chenile.samples.m1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = { "org.chenile.samples.**.configuration",
		"org.chenile.configuration" })
public class M1Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(M1Application.class, args);
	}
}
