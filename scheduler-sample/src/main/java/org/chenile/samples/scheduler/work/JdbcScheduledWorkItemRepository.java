package org.chenile.samples.scheduler.work;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcScheduledWorkItemRepository {
	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate transactionTemplate;

	public JdbcScheduledWorkItemRepository(JdbcTemplate jdbcTemplate,
			PlatformTransactionManager transactionManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public boolean enqueue(String jobName, Instant scheduledFireTime, String idempotencyKey, String payload) {
		Instant now = Instant.now();
		try {
			jdbcTemplate.update("""
					insert into chenile_scheduler_work_item
					(id, job_name, scheduled_fire_time, idempotency_key, payload, status, attempt, created_at, updated_at)
					values (?, ?, ?, ?, ?, 'PENDING', 0, ?, ?)
					""",
					UUID.randomUUID().toString(),
					jobName,
					Timestamp.from(scheduledFireTime),
					idempotencyKey,
					payload,
					Timestamp.from(now),
					Timestamp.from(now));
			return true;
		} catch (DuplicateKeyException e) {
			return false;
		}
	}

	public Optional<ScheduledWorkItem> claimNext(String workerId, int lockSeconds) {
		return transactionTemplate.execute(status -> {
			Instant now = Instant.now();
			Instant lockedUntil = now.plusSeconds(lockSeconds);
			Optional<ScheduledWorkItem> item = jdbcTemplate.query("""
					select id, job_name, scheduled_fire_time, idempotency_key, payload, status, attempt,
					       locked_by, locked_until, created_at, updated_at, finished_at, error_message
					  from chenile_scheduler_work_item
					 where status = 'PENDING'
					    or (status = 'RUNNING' and locked_until < ?)
					 order by created_at
					 limit 1
					 for update skip locked
					""", rs -> rs.next() ? Optional.of(map(rs)) : Optional.empty(), Timestamp.from(now));
			if (item.isEmpty()) {
				return Optional.empty();
			}
			ScheduledWorkItem workItem = item.get();
			int nextAttempt = workItem.attempt + 1;
			jdbcTemplate.update("""
					update chenile_scheduler_work_item
					   set status = 'RUNNING', attempt = ?, locked_by = ?, locked_until = ?, updated_at = ?,
					       error_message = null
					 where id = ?
					""",
					nextAttempt,
					workerId,
					Timestamp.from(lockedUntil),
					Timestamp.from(now),
					workItem.id);
			workItem.status = "RUNNING";
			workItem.attempt = nextAttempt;
			workItem.lockedBy = workerId;
			workItem.lockedUntil = lockedUntil;
			workItem.updatedAt = now;
			workItem.errorMessage = null;
			return Optional.of(workItem);
		});
	}

	public void markSuccess(String id) {
		Instant now = Instant.now();
		jdbcTemplate.update("""
				update chenile_scheduler_work_item
				   set status = 'SUCCESS', finished_at = ?, updated_at = ?, locked_until = null, error_message = null
				 where id = ?
				""", Timestamp.from(now), Timestamp.from(now), id);
	}

	public void markFailure(String id, String errorMessage) {
		Instant now = Instant.now();
		jdbcTemplate.update("""
				update chenile_scheduler_work_item
				   set status = 'FAILED', finished_at = ?, updated_at = ?, locked_until = null, error_message = ?
				 where id = ?
				""", Timestamp.from(now), Timestamp.from(now), errorMessage, id);
	}

	public long backlogCount() {
		return jdbcTemplate.queryForObject("""
				select count(*)
				  from chenile_scheduler_work_item
				 where status = 'PENDING'
				    or (status = 'RUNNING' and locked_until < ?)
				""", Long.class, Timestamp.from(Instant.now()));
	}

	private ScheduledWorkItem map(ResultSet rs) throws SQLException {
		ScheduledWorkItem item = new ScheduledWorkItem();
		item.id = rs.getString("id");
		item.jobName = rs.getString("job_name");
		item.scheduledFireTime = toInstant(rs.getTimestamp("scheduled_fire_time"));
		item.idempotencyKey = rs.getString("idempotency_key");
		item.payload = rs.getString("payload");
		item.status = rs.getString("status");
		item.attempt = rs.getInt("attempt");
		item.lockedBy = rs.getString("locked_by");
		item.lockedUntil = toInstant(rs.getTimestamp("locked_until"));
		item.createdAt = toInstant(rs.getTimestamp("created_at"));
		item.updatedAt = toInstant(rs.getTimestamp("updated_at"));
		item.finishedAt = toInstant(rs.getTimestamp("finished_at"));
		item.errorMessage = rs.getString("error_message");
		return item;
	}

	private Instant toInstant(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toInstant();
	}
}
