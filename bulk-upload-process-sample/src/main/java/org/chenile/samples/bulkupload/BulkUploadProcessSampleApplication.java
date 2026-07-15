package org.chenile.samples.bulkupload;

import org.chenile.configuration.process.JdbcProcessStarterConfiguration;
import org.chenile.orchestrator.process.configuration.ProcessConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"org.chenile.orchestrator.process.model"})
@EnableJpaRepositories(basePackages = {"org.chenile.orchestrator.process.configuration.dao"})
@Import({ProcessConfiguration.class, JdbcProcessStarterConfiguration.class})
public class BulkUploadProcessSampleApplication {
	public static void main(String[] args) {
		SpringApplication.run(BulkUploadProcessSampleApplication.class, args);
	}
}
