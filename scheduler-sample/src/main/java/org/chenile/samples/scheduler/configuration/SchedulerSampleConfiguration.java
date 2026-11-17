package org.chenile.samples.scheduler.configuration;

import org.chenile.samples.scheduler.service.ScheduledReportService;
import org.chenile.samples.scheduler.service.impl.ScheduledReportServiceImpl;
import org.chenile.samples.scheduler.work.JdbcScheduledWorkItemRepository;
import org.chenile.samples.scheduler.work.ScheduledWorkItemWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SchedulerSampleConfiguration {
	@Bean
	public JdbcScheduledWorkItemRepository scheduledWorkItemRepository(JdbcTemplate jdbcTemplate,
			PlatformTransactionManager transactionManager) {
		return new JdbcScheduledWorkItemRepository(jdbcTemplate, transactionManager);
	}

	@Bean
	public ScheduledReportService _scheduledReportService_(JdbcScheduledWorkItemRepository repository) {
		return new ScheduledReportServiceImpl(repository);
	}

	/** The worker profile: claims and processes work items instead of serving and scheduling. */
	@Bean
	@ConditionalOnProperty(name = "scheduler.sample.worker.enabled", havingValue = "true")
	public ScheduledWorkItemWorker scheduledWorkItemWorker(JdbcScheduledWorkItemRepository repository,
			@Value("${scheduler.sample.worker.id:${HOSTNAME:local-worker}}") String workerId,
			@Value("${scheduler.sample.worker.lock-seconds:120}") int lockSeconds,
			@Value("${scheduler.sample.worker.run-once:true}") boolean runOnce,
			@Value("${scheduler.sample.worker.poll-interval-millis:5000}") long pollIntervalMillis) {
		return new ScheduledWorkItemWorker(repository, workerId, lockSeconds, runOnce, pollIntervalMillis);
	}
}
