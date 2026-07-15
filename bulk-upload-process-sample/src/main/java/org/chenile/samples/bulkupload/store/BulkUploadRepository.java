package org.chenile.samples.bulkupload.store;

import org.chenile.samples.bulkupload.model.BulkUploadModels.AuditEvent;
import org.chenile.samples.bulkupload.model.BulkUploadModels.ChunkStatus;
import org.chenile.samples.bulkupload.model.BulkUploadModels.GroupStatus;
import org.chenile.samples.bulkupload.model.BulkUploadModels.UploadStatus;
import org.chenile.samples.bulkupload.model.BulkUploadModels.RowError;
import org.chenile.samples.bulkupload.model.BulkUploadModels.UploadReport;
import org.chenile.samples.bulkupload.model.BulkUploadModels.WorkerSummary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BulkUploadRepository {
	private final JdbcTemplate jdbcTemplate;

	public BulkUploadRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void createUpload(String uploadId, String processId, String objectKey) {
		try {
			jdbcTemplate.update("""
					insert into bulk_upload_file
					(id, process_id, object_key, status, total_rows, success_rows, error_rows, created_at, updated_at)
					values (?, ?, ?, 'SUBMITTED', 0, 0, 0, current_timestamp, current_timestamp)
					""", uploadId, processId, objectKey);
		} catch (DuplicateKeyException ignored) {
			// Upload IDs are idempotency keys for the sample.
		}
		audit(uploadId, processId, "UPLOAD_SUBMITTED", null, "SUBMITTED",
				"Upload accepted for processing", "{\"objectKey\":\"" + objectKey + "\"}");
	}

	public void markRunning(String uploadId) {
		jdbcTemplate.update("""
				update bulk_upload_file set status = 'RUNNING', updated_at = current_timestamp where id = ?
				""", uploadId);
	}

	public void saveChunk(String uploadId, int chunkNumber, int startLine, int endLine) {
		saveChunk(uploadId, 1, chunkNumber, startLine, endLine);
	}

	public void saveGroup(String uploadId, int groupNumber, int startChunkNumber, int endChunkNumber) {
		try {
			jdbcTemplate.update("""
					insert into bulk_upload_group
					(upload_id, group_number, start_chunk_number, end_chunk_number, status, total_rows, success_rows, error_rows, updated_at)
					values (?, ?, ?, ?, 'PENDING', 0, 0, 0, current_timestamp)
					""", uploadId, groupNumber, startChunkNumber, endChunkNumber);
		} catch (DuplicateKeyException ignored) {
			// Root splitters can be retried; groups are keyed by upload and group number.
		}
	}

	public void startGroup(String uploadId, int groupNumber) {
		jdbcTemplate.update("""
				update bulk_upload_group
				   set status = 'RUNNING', updated_at = current_timestamp
				 where upload_id = ? and group_number = ?
				""", uploadId, groupNumber);
	}

	public void finishGroup(String uploadId, int groupNumber, int totalRows, int successRows, int errorRows) {
		jdbcTemplate.update("""
				update bulk_upload_group
				   set status = case when ? = 0 then 'SUCCESS' else 'SUCCESS_WITH_ERRORS' end,
				       total_rows = ?, success_rows = ?, error_rows = ?, updated_at = current_timestamp
				 where upload_id = ? and group_number = ?
				""", errorRows, totalRows, successRows, errorRows, uploadId, groupNumber);
	}

	public void saveChunk(String uploadId, int groupNumber, int chunkNumber, int startLine, int endLine) {
		try {
			jdbcTemplate.update("""
					insert into bulk_upload_chunk
					(upload_id, group_number, chunk_number, start_line, end_line, status, total_rows, success_rows, error_rows, updated_at)
					values (?, ?, ?, ?, ?, 'PENDING', 0, 0, 0, current_timestamp)
					""", uploadId, groupNumber, chunkNumber, startLine, endLine);
		} catch (DuplicateKeyException ignored) {
			// Splitters can be retried; chunk rows are keyed by upload and chunk number.
		}
	}

	public void startChunk(String uploadId, int chunkNumber) {
		jdbcTemplate.update("""
				update bulk_upload_chunk
				   set status = 'RUNNING', updated_at = current_timestamp
				 where upload_id = ? and chunk_number = ?
				""", uploadId, chunkNumber);
	}

	public void rowResult(String uploadId, int chunkNumber, int lineNumber, String rawLine, boolean success, String error) {
		try {
			jdbcTemplate.update("""
					insert into bulk_upload_row_result
					(upload_id, chunk_number, line_number, raw_line, success, error_message, created_at)
					values (?, ?, ?, ?, ?, ?, current_timestamp)
					""", uploadId, chunkNumber, lineNumber, rawLine, success, error);
		} catch (DuplicateKeyException ignored) {
			// Executor retries should not duplicate row-level outcomes.
		}
	}

	public void finishChunk(String uploadId, int chunkNumber, int totalRows, int successRows, int errorRows) {
		jdbcTemplate.update("""
				update bulk_upload_chunk
				   set status = case when ? = 0 then 'SUCCESS' else 'SUCCESS_WITH_ERRORS' end,
				       total_rows = ?, success_rows = ?, error_rows = ?, updated_at = current_timestamp
				 where upload_id = ? and chunk_number = ?
				""", errorRows, totalRows, successRows, errorRows, uploadId, chunkNumber);
	}

	public void finishUpload(String uploadId, int totalRows, int successRows, int errorRows, String resultObjectKey) {
		jdbcTemplate.update("""
				update bulk_upload_file
				   set status = case when ? = 0 then 'SUCCESS' else 'SUCCESS_WITH_ERRORS' end,
				       total_rows = ?, success_rows = ?, error_rows = ?, result_object_key = ?, updated_at = current_timestamp
				 where id = ?
				""", errorRows, totalRows, successRows, errorRows, resultObjectKey, uploadId);
	}

	public Optional<UploadStatus> status(String uploadId) {
		return jdbcTemplate.query("""
				select id, process_id, status, total_rows, success_rows, error_rows, object_key, result_object_key
				  from bulk_upload_file
				 where id = ?
				""", rs -> rs.next()
				? Optional.of(new UploadStatus(rs.getString("id"), rs.getString("process_id"), rs.getString("status"),
						rs.getInt("total_rows"), rs.getInt("success_rows"), rs.getInt("error_rows"),
						rs.getString("object_key"), rs.getString("result_object_key")))
				: Optional.empty(), uploadId);
	}

	public List<GroupStatus> groups(String uploadId) {
		return jdbcTemplate.query("""
				select group_number, start_chunk_number, end_chunk_number, status, total_rows, success_rows, error_rows
				  from bulk_upload_group
				 where upload_id = ?
				 order by group_number
				""", (rs, rowNum) -> new GroupStatus(rs.getInt("group_number"), rs.getInt("start_chunk_number"),
				rs.getInt("end_chunk_number"), rs.getString("status"), rs.getInt("total_rows"),
				rs.getInt("success_rows"), rs.getInt("error_rows")), uploadId);
	}

	public List<ChunkStatus> chunks(String uploadId) {
		return jdbcTemplate.query("""
				select group_number, chunk_number, start_line, end_line, status, total_rows, success_rows, error_rows
				  from bulk_upload_chunk
				 where upload_id = ?
				 order by group_number, chunk_number
				""", (rs, rowNum) -> new ChunkStatus(rs.getInt("group_number"), rs.getInt("chunk_number"),
				rs.getInt("start_line"), rs.getInt("end_line"), rs.getString("status"), rs.getInt("total_rows"),
				rs.getInt("success_rows"), rs.getInt("error_rows")), uploadId);
	}

	public List<RowError> rowErrors(String uploadId) {
		return jdbcTemplate.query("""
				select line_number, chunk_number, raw_line, error_message
				  from bulk_upload_row_result
				 where upload_id = ? and success = false
				 order by line_number
				""", (rs, rowNum) -> new RowError(rs.getInt("line_number"), rs.getInt("chunk_number"),
				rs.getString("raw_line"), rs.getString("error_message")), uploadId);
	}

	public List<WorkerSummary> workerSummary(String uploadId) {
		return jdbcTemplate.query("""
				select worker_type, status, count(*) as item_count
				  from chenile_process_work_item
				 where process_id = ?
				    or process_id like ?
				 group by worker_type, status
				 order by worker_type, status
				""", (rs, rowNum) -> new WorkerSummary(rs.getString("worker_type"),
				rs.getString("status"), rs.getLong("item_count")), uploadId, uploadId + "-%");
	}

	public UploadReport report(String uploadId) {
		UploadStatus uploadStatus = status(uploadId).orElseThrow();
		return new UploadReport(uploadStatus, groups(uploadId), chunks(uploadId), rowErrors(uploadId),
				workerSummary(uploadId));
	}

	public void audit(String uploadId, String processId, String eventType, String workerType,
			String status, String message, String detailsJson) {
		jdbcTemplate.update("""
				insert into bulk_upload_audit_event
				(upload_id, process_id, event_type, worker_type, status, message, details_json, created_at)
				values (?, ?, ?, ?, ?, ?, ?, current_timestamp)
				""", uploadId, processId, eventType, workerType, status, message, detailsJson);
	}

	public List<AuditEvent> auditEvents(String uploadId) {
		return jdbcTemplate.query("""
				select id, upload_id, process_id, event_type, worker_type, status, message, details_json, created_at
				  from bulk_upload_audit_event
				 where upload_id = ?
				 order by created_at, id
				""", (rs, rowNum) -> new AuditEvent(rs.getLong("id"), rs.getString("upload_id"),
				rs.getString("process_id"), rs.getString("event_type"), rs.getString("worker_type"),
				rs.getString("status"), rs.getString("message"), rs.getString("details_json"),
				toInstant(rs.getTimestamp("created_at"))), uploadId);
	}

	public int rowCount(String objectKey, ObjectStore objectStore) throws Exception {
		try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(objectStore.get(objectKey)))) {
			int count = 0;
			while (reader.readLine() != null) {
				count++;
			}
			return count;
		}
	}

	private Instant toInstant(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toInstant();
	}
}
