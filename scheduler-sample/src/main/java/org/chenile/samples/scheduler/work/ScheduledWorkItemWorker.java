package org.chenile.samples.scheduler.work;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "scheduler.sample.worker.enabled", havingValue = "true")
public class ScheduledWorkItemWorker implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(ScheduledWorkItemWorker.class);
	private final JdbcScheduledWorkItemRepository repository;
	private final String workerId;
	private final int lockSeconds;
	private final boolean runOnce;
	private final long pollIntervalMillis;

	public ScheduledWorkItemWorker(JdbcScheduledWorkItemRepository repository,
			@Value("${scheduler.sample.worker.id:${HOSTNAME:local-worker}}") String workerId,
			@Value("${scheduler.sample.worker.lock-seconds:120}") int lockSeconds,
			@Value("${scheduler.sample.worker.run-once:true}") boolean runOnce,
			@Value("${scheduler.sample.worker.poll-interval-millis:5000}") long pollIntervalMillis) {
		this.repository = repository;
		this.workerId = workerId;
		this.lockSeconds = lockSeconds;
		this.runOnce = runOnce;
		this.pollIntervalMillis = pollIntervalMillis;
	}

	@Override
	public void run(String... args) throws Exception {
		do {
			boolean processed = processOne();
			if (runOnce) {
				return;
			}
			if (!processed) {
				Thread.sleep(pollIntervalMillis);
			}
		} while (true);
	}

	boolean processOne() {
		return repository.claimNext(workerId, lockSeconds)
				.map(item -> {
					try {
						logger.info("Processing work item idempotencyKey={} jobName={} payload={}",
								item.idempotencyKey, item.jobName, item.payload);
						repository.markSuccess(item.id);
						return true;
					} catch (Exception e) {
						repository.markFailure(item.id, e.getMessage());
						throw new IllegalStateException("Failed to process scheduled work item " + item.id, e);
					}
				})
				.orElse(false);
	}
}
