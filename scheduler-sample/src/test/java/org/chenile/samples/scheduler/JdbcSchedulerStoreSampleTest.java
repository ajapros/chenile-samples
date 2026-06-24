package org.chenile.samples.scheduler;

import org.chenile.scheduler.model.ScheduledExecutionRecord;
import org.chenile.scheduler.model.ScheduledExecutionRequest;
import org.chenile.scheduler.model.SchedulerExecutionStatus;
import org.chenile.scheduler.model.SchedulerInfo;
import org.chenile.scheduler.store.JdbcSchedulerExecutionStore;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcSchedulerStoreSampleTest {
	@Test
	void recordsExecutionStatusInJdbc() {
		EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.addScript("chenile-scheduler-schema.sql")
				.build();
		try {
			JdbcSchedulerExecutionStore store = new JdbcSchedulerExecutionStore(new JdbcTemplate(database));
			ScheduledExecutionRequest request = request();

			assertTrue(store.tryStartExecution(request));
			assertFalse(store.tryStartExecution(request));
			store.markSuccess(request.getExecutionId(), request.getAttempt(), "local");

			ScheduledExecutionRecord record = store.findByExecutionId(request.getExecutionId()).orElseThrow();
			assertEquals(SchedulerExecutionStatus.SUCCESS, record.getStatus());
			assertEquals("jdbc-report", record.getJobName());
		} finally {
			database.shutdown();
		}
	}

	private ScheduledExecutionRequest request() {
		SchedulerInfo info = new SchedulerInfo();
		info.setJobName("jdbc-report");
		info.setLauncher("local");
		ScheduledExecutionRequest request = new ScheduledExecutionRequest();
		request.setSchedulerInfo(info);
		request.setExecutionId("jdbc-report-1000");
		request.setScheduledFireTime(Instant.ofEpochMilli(1000));
		request.setActualFireTime(Instant.ofEpochMilli(1001));
		request.setAttempt(1);
		return request;
	}
}
