package org.chenile.samples.scheduler.service;

import org.chenile.samples.scheduler.model.ScheduledReportRequest;
import org.chenile.samples.scheduler.model.ScheduledReportResult;

public interface ScheduledReportService {
	ScheduledReportResult generate(ScheduledReportRequest request);
}
