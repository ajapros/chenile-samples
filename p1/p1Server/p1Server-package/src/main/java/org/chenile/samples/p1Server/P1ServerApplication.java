package org.chenile.samples.p1Server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = { "org.chenile.samples.**.configuration",
		"org.chenile.service.registry.configuration",
		"org.chenile.configuration", "org.chenile.orchestrator.process.configuration", "org.chenile.samples.p1.bootstrap" })
@EntityScan({ "org.chenile.samples.**.model", "org.chenile.orchestrator.process.model", "org.chenile.orchestrator.process.configuration.model", "org.chenile.trigger.cron", "org.chenile.service.registry.model" })
@EnableJpaRepositories(basePackages = { "org.chenile.samples.**.configuration.dao", "org.chenile.orchestrator.process.configuration.dao", "org.chenile.trigger.cron", "org.chenile.service.registry.configuration.dao" })
@EnableTransactionManagement
public class P1ServerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(P1ServerApplication.class, args);
	}
}
