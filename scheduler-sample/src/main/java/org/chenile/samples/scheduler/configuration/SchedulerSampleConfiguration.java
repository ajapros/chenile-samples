package org.chenile.samples.scheduler.configuration;

import org.chenile.samples.scheduler.service.ScheduledReportService;
import org.chenile.samples.scheduler.service.impl.ScheduledReportServiceImpl;
import org.chenile.samples.scheduler.work.JdbcScheduledWorkItemRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerSampleConfiguration {
	@Bean
	public ScheduledReportService _scheduledReportService_(JdbcScheduledWorkItemRepository repository) {
		return new ScheduledReportServiceImpl(repository);
	}
}
