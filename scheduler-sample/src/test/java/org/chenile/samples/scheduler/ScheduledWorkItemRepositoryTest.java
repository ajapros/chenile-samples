package org.chenile.samples.scheduler;

import org.chenile.samples.scheduler.work.JdbcScheduledWorkItemRepository;
import org.chenile.samples.scheduler.work.ScheduledWorkItem;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduledWorkItemRepositoryTest {
	@Test
	void enqueueIsIdempotentByKey() {
		EmbeddedDatabase database = database();
		try {
			JdbcScheduledWorkItemRepository repository = repository(database);
			Instant scheduledAt = Instant.parse("2026-06-24T00:00:00Z");

			assertTrue(repository.enqueue("scheduled-report", scheduledAt, "report:daily:2026-06-24", "{}"));
			assertFalse(repository.enqueue("scheduled-report", scheduledAt, "report:daily:2026-06-24", "{}"));

			assertEquals(1L, repository.backlogCount());
		} finally {
			database.shutdown();
		}
	}

	@Test
	void claimLocksOneWorkItemForOneWorker() {
		EmbeddedDatabase database = database();
		try {
			JdbcScheduledWorkItemRepository repository = repository(database);
			Instant scheduledAt = Instant.parse("2026-06-24T00:00:00Z");
			repository.enqueue("scheduled-report", scheduledAt, "report:daily:2026-06-24", "{}");

			Optional<ScheduledWorkItem> workerOne = repository.claimNext("worker-one", 120);
			Optional<ScheduledWorkItem> workerTwo = repository.claimNext("worker-two", 120);

			assertTrue(workerOne.isPresent());
			assertFalse(workerTwo.isPresent());
			assertEquals("RUNNING", workerOne.get().status);
			assertEquals("worker-one", workerOne.get().lockedBy);
		} finally {
			database.shutdown();
		}
	}

	@Test
	void expiredRunningWorkCanBeReclaimed() {
		EmbeddedDatabase database = database();
		try {
			JdbcScheduledWorkItemRepository repository = repository(database);
			JdbcTemplate jdbcTemplate = new JdbcTemplate(database);
			Instant scheduledAt = Instant.parse("2026-06-24T00:00:00Z");
			repository.enqueue("scheduled-report", scheduledAt, "report:daily:2026-06-24", "{}");
			ScheduledWorkItem firstClaim = repository.claimNext("worker-one", -1).orElseThrow();

			Optional<ScheduledWorkItem> secondClaim = repository.claimNext("worker-two", 120);

			assertTrue(secondClaim.isPresent());
			assertEquals(firstClaim.id, secondClaim.get().id);
			assertEquals("worker-two", secondClaim.get().lockedBy);
			assertEquals(2, jdbcTemplate.queryForObject(
					"select attempt from chenile_scheduler_work_item where id = ?", Integer.class, firstClaim.id));
		} finally {
			database.shutdown();
		}
	}

	private JdbcScheduledWorkItemRepository repository(EmbeddedDatabase database) {
		return new JdbcScheduledWorkItemRepository(new JdbcTemplate(database),
				new DataSourceTransactionManager(database));
	}

	private EmbeddedDatabase database() {
		return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.addScript("chenile-scheduler-work-schema.sql")
				.build();
	}
}
