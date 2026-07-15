package org.chenile.samples.bulkupload.model;

public final class BulkUploadModels {
	private BulkUploadModels() {}

	public record BulkUploadProcessInput(String uploadId, String objectKey, int chunkSize) {}

	public record BulkUploadChunkInput(String uploadId, String objectKey, int chunkNumber, int startLine, int endLine) {}

	public record BulkUploadChunkResult(String uploadId, int chunkNumber, int totalRows, int successRows, int errorRows) {}

	public record BulkUploadAggregateResult(String uploadId, int totalRows, int successRows, int errorRows, String status) {}

	public record UploadStatus(String uploadId, String processId, String status, int totalRows,
			int successRows, int errorRows, String objectKey, String resultObjectKey) {}
}
