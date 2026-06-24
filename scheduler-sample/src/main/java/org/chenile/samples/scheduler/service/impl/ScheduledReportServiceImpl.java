package org.chenile.samples.scheduler.service.impl;

import org.chenile.samples.scheduler.model.ScheduledReportRequest;
import org.chenile.samples.scheduler.model.ScheduledReportResult;
import org.chenile.samples.scheduler.service.ScheduledReportService;
import org.chenile.samples.scheduler.work.JdbcScheduledWorkItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ScheduledReportServiceImpl implements ScheduledReportService {
	private static final Logger logger = LoggerFactory.getLogger(ScheduledReportServiceImpl.class);
	private final JdbcScheduledWorkItemRepository repository;

	public ScheduledReportServiceImpl(JdbcScheduledWorkItemRepository repository) {
		this.repository = repository;
	}

	@Override
	public ScheduledReportResult generate(ScheduledReportRequest request) {
		String reportName = request == null ? "unknown" : request.reportName;
		String businessDate = request == null ? "unknown" : request.businessDate;
		String idempotencyKey = "scheduled-report:" + reportName + ":" + businessDate;
		String payload = "{ \"reportName\": \"" + reportName + "\", \"businessDate\": \"" + businessDate + "\" }";
		boolean enqueued = repository.enqueue("scheduled-report", Instant.now(), idempotencyKey, payload);
		logger.info("Scheduled report work item {} reportName={} businessDate={} idempotencyKey={}",
				enqueued ? "enqueued" : "already-exists", reportName, businessDate, idempotencyKey);
		return new ScheduledReportResult(reportName, businessDate, enqueued ? "ENQUEUED" : "DUPLICATE");
	}
}
