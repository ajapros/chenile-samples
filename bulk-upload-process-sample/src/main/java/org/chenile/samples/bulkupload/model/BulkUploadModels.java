package org.chenile.samples.bulkupload.model;

public final class BulkUploadModels {
	private BulkUploadModels() {}

	public record BulkUploadProcessInput(String uploadId, String objectKey, int chunkSize, int chunksPerGroup) {}

	public record BulkUploadGroupInput(String uploadId, String objectKey, int groupNumber,
			int startChunkNumber, int endChunkNumber, int chunkSize) {}

	public record BulkUploadChunkInput(String uploadId, String objectKey, int groupNumber,
			int chunkNumber, int startLine, int endLine) {}

	public record BulkUploadChunkResult(String uploadId, int chunkNumber, int totalRows, int successRows, int errorRows) {}

	public record BulkUploadGroupResult(String uploadId, int groupNumber, int totalRows, int successRows, int errorRows) {}

	public record BulkUploadAggregateResult(String uploadId, int totalRows, int successRows, int errorRows, String status) {}

	public record UploadStatus(String uploadId, String processId, String status, int totalRows,
			int successRows, int errorRows, String objectKey, String resultObjectKey) {}

	public record GroupStatus(int groupNumber, int startChunkNumber, int endChunkNumber, String status,
			int totalRows, int successRows, int errorRows) {}

	public record ChunkStatus(int groupNumber, int chunkNumber, int startLine, int endLine, String status,
			int totalRows, int successRows, int errorRows) {}

	public record RowError(int lineNumber, int chunkNumber, String rawLine, String errorMessage) {}

	public record WorkerSummary(String workerType, String status, long count) {}

	public record UploadReport(UploadStatus upload, java.util.List<GroupStatus> groups,
			java.util.List<ChunkStatus> chunks, java.util.List<RowError> rowErrors,
			java.util.List<WorkerSummary> workers) {}

	public record AuditEvent(long id, String uploadId, String processId, String eventType, String workerType,
			String status, String message, String detailsJson, java.time.Instant createdAt) {}
}
