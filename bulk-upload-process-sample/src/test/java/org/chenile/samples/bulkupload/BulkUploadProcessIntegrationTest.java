package org.chenile.samples.bulkupload;

import org.chenile.orchestrator.process.api.ProcessManager;
import org.chenile.orchestrator.process.model.Constants;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.jdbc.JdbcProcessWorkerRunner;
import org.chenile.orchestrator.process.jdbc.JdbcProcessWorkerRepository;
import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.model.WorkerType;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadProcessInput;
import org.chenile.samples.bulkupload.process.BulkUploadBatchService;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:bulk_upload_process;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"bulk-upload.object-store.type=filesystem",
		"chenile.process.worker.jdbc.run-worker=false"
})
class BulkUploadProcessIntegrationTest {
	@Autowired BulkUploadBatchService batchService;
	@Autowired BulkUploadRepository repository;
	@Autowired ObjectStore objectStore;
	@Autowired JdbcProcessWorkerRunner runner;
	@Autowired JdbcProcessWorkerRepository workerRepository;
	@Autowired @Qualifier("_processStateEntityService_") ProcessManager processManager;

	@Test
	void completesNestedBulkUploadWithAuditAndFinalReport() throws Exception {
		String uploadId = UUID.randomUUID().toString();
		String objectKey = "uploads/" + uploadId + "/input.csv";
		byte[] content = "one,ok\n,error\nthree,ok\nERROR,bad\nfive,ok\nsix,ok\n".getBytes(StandardCharsets.UTF_8);
		objectStore.put(objectKey, new ByteArrayInputStream(content), content.length, "text/csv");
		repository.createUpload(uploadId, uploadId, objectKey);

		batchService.startUpload(new BulkUploadProcessInput(uploadId, objectKey, 2, 2));

		Process process = drain(uploadId);

		Assertions.assertEquals(Constants.States.PROCESSED, process.getCurrentState().getStateId());
		var status = repository.status(uploadId).orElseThrow();
		Assertions.assertEquals(6, status.totalRows());
		Assertions.assertEquals(4, status.successRows());
		Assertions.assertEquals(2, status.errorRows());
		Assertions.assertEquals("SUCCESS_WITH_ERRORS", status.status());
		Assertions.assertNotNull(status.resultObjectKey());

		var report = repository.report(uploadId);
		Assertions.assertEquals(2, report.groups().size());
		Assertions.assertEquals(3, report.chunks().size());
		Assertions.assertEquals(2, report.rowErrors().size());
		Assertions.assertTrue(report.workers().stream()
				.anyMatch(worker -> "SPLITTER".equals(worker.workerType()) && "SUCCESS".equals(worker.status())));
		Assertions.assertTrue(report.workers().stream()
				.anyMatch(worker -> "EXECUTOR".equals(worker.workerType()) && "SUCCESS".equals(worker.status())));
		Assertions.assertTrue(report.workers().stream()
				.anyMatch(worker -> "AGGREGATOR".equals(worker.workerType()) && "SUCCESS".equals(worker.status())));

		var audit = repository.auditEvents(uploadId);
		Assertions.assertTrue(audit.stream().anyMatch(event -> "ROOT_SPLIT_FINISHED".equals(event.eventType())));
		Assertions.assertTrue(audit.stream().anyMatch(event -> "GROUP_SPLIT_FINISHED".equals(event.eventType())));
		Assertions.assertTrue(audit.stream().anyMatch(event -> "CHUNK_FINISHED".equals(event.eventType())));
		Assertions.assertTrue(audit.stream().anyMatch(event -> "GROUP_AGGREGATED".equals(event.eventType())));
		Assertions.assertTrue(audit.stream().anyMatch(event -> "UPLOAD_AGGREGATED".equals(event.eventType())));
	}

	@Test
	void jdbcWorkersClaimDifferentRowsForConcurrentScaling() {
		Assertions.assertTrue(workerRepository.enqueue(workerDto("sample-claim-1", WorkerType.EXECUTOR), "{}"));
		Assertions.assertTrue(workerRepository.enqueue(workerDto("sample-claim-2", WorkerType.EXECUTOR), "{}"));

		var first = workerRepository.claimNext("sample-worker-one", 300, 5).orElseThrow();
		var second = workerRepository.claimNext("sample-worker-two", 300, 5).orElseThrow();

		Assertions.assertNotEquals(first.id, second.id);
		Assertions.assertEquals("sample-worker-one", first.lockedBy);
		Assertions.assertEquals("sample-worker-two", second.lockedBy);
	}

	private Process drain(String uploadId) {
		for (int i = 0; i < 40; i++) {
			runner.processOne();
			Process process = processManager.retrieve(uploadId).getMutatedEntity();
			if (Constants.States.PROCESSED.equals(process.getCurrentState().getStateId())
					|| Constants.States.PROCESSED_WITH_ERRORS.equals(process.getCurrentState().getStateId())) {
				return process;
			}
		}
		return processManager.retrieve(uploadId).getMutatedEntity();
	}

	private WorkerDto workerDto(String id, WorkerType workerType) {
		Process process = new Process();
		process.id = id;
		process.processType = "bulkUploadChunk";
		WorkerDto workerDto = new WorkerDto();
		workerDto.process = process;
		workerDto.workerType = workerType;
		return workerDto;
	}
}
