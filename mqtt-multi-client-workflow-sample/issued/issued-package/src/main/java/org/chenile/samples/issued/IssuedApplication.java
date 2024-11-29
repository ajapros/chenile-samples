package org.chenile.samples.issued;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = { "org.chenile.samples.**.configuration",
		"org.chenile.configuration" })
@EntityScan("org.chenile.samples.**.model")
@EnableJpaRepositories(basePackages = "org.chenile.samples.**.configuration.dao")
@EnableTransactionManagement
public class IssuedApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(IssuedApplication.class, args);
	}
}
