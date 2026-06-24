package org.chenile.samples.scheduler.work;

import java.time.Instant;

public class ScheduledWorkItem {
	public String id;
	public String jobName;
	public Instant scheduledFireTime;
	public String idempotencyKey;
	public String payload;
	public String status;
	public int attempt;
	public String lockedBy;
	public Instant lockedUntil;
	public Instant createdAt;
	public Instant updatedAt;
	public Instant finishedAt;
	public String errorMessage;
}
