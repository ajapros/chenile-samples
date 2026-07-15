package org.chenile.samples.bulkupload.controller;

import org.chenile.orchestrator.process.api.ProcessManager;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadProcessInput;
import org.chenile.samples.bulkupload.model.BulkUploadModels.AuditEvent;
import org.chenile.samples.bulkupload.model.BulkUploadModels.UploadReport;
import org.chenile.samples.bulkupload.model.BulkUploadModels.UploadStatus;
import org.chenile.samples.bulkupload.process.BulkUploadBatchService;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class BulkUploadController {
	private final BulkUploadBatchService batchService;
	private final BulkUploadRepository repository;
	private final ObjectStore objectStore;
	private final ProcessManager processManager;
	private final int defaultChunkSize;
	private final int defaultChunksPerGroup;

	public BulkUploadController(BulkUploadBatchService batchService,
			BulkUploadRepository repository,
			ObjectStore objectStore,
			@Qualifier("_processStateEntityService_") ProcessManager processManager,
			@Value("${bulk-upload.chunk-size:100}") int defaultChunkSize,
			@Value("${bulk-upload.chunks-per-group:10}") int defaultChunksPerGroup) {
		this.batchService = batchService;
		this.repository = repository;
		this.objectStore = objectStore;
		this.processManager = processManager;
		this.defaultChunkSize = defaultChunkSize;
		this.defaultChunksPerGroup = defaultChunksPerGroup;
	}

	@PostMapping(path = "/bulk-uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadStatus upload(@RequestPart("file") MultipartFile file,
			@RequestParam(name = "chunkSize", required = false) Integer chunkSize,
			@RequestParam(name = "chunksPerGroup", required = false) Integer chunksPerGroup) throws Exception {
		String uploadId = UUID.randomUUID().toString();
		String objectKey = "uploads/" + uploadId + "/" + file.getOriginalFilename();
		objectStore.put(objectKey, file.getInputStream(), file.getSize(),
				file.getContentType() == null ? "text/csv" : file.getContentType());
		repository.createUpload(uploadId, uploadId, objectKey);
		batchService.startUpload(new BulkUploadProcessInput(uploadId, objectKey,
				chunkSize == null ? defaultChunkSize : chunkSize,
				chunksPerGroup == null ? defaultChunksPerGroup : chunksPerGroup));
		return repository.status(uploadId).orElseThrow();
	}

	@GetMapping("/bulk-uploads/{uploadId}")
	public UploadStatus status(@PathVariable String uploadId) {
		return repository.status(uploadId).orElseThrow();
	}

	@GetMapping("/bulk-uploads/{uploadId}/processes")
	public List<Process> processTree(@PathVariable String uploadId) {
		return processManager.getSubProcesses(uploadId, true);
	}

	@GetMapping("/bulk-uploads/{uploadId}/report")
	public UploadReport report(@PathVariable String uploadId) {
		return repository.report(uploadId);
	}

	@GetMapping("/bulk-uploads/{uploadId}/audit")
	public List<AuditEvent> audit(@PathVariable String uploadId) {
		return repository.auditEvents(uploadId);
	}
}
