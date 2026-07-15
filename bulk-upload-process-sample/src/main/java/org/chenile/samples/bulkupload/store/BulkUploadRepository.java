package org.chenile.samples.bulkupload.store;

import org.chenile.samples.bulkupload.model.BulkUploadModels.UploadStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
	}

	public void markRunning(String uploadId) {
		jdbcTemplate.update("""
				update bulk_upload_file set status = 'RUNNING', updated_at = current_timestamp where id = ?
				""", uploadId);
	}

	public void saveChunk(String uploadId, int chunkNumber, int startLine, int endLine) {
		try {
			jdbcTemplate.update("""
					insert into bulk_upload_chunk
					(upload_id, chunk_number, start_line, end_line, status, total_rows, success_rows, error_rows, updated_at)
					values (?, ?, ?, ?, 'PENDING', 0, 0, 0, current_timestamp)
					""", uploadId, chunkNumber, startLine, endLine);
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

	public int rowCount(String objectKey, ObjectStore objectStore) throws Exception {
		try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(objectStore.get(objectKey)))) {
			int count = 0;
			while (reader.readLine() != null) {
				count++;
			}
			return count;
		}
	}
}
