package org.chenile.samples.scheduler.model;

public class ScheduledReportResult {
	public String reportName;
	public String businessDate;
	public String status;

	public ScheduledReportResult() {
	}

	public ScheduledReportResult(String reportName, String businessDate, String status) {
		this.reportName = reportName;
		this.businessDate = businessDate;
		this.status = status;
	}
}
