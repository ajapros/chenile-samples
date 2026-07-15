package org.chenile.samples.bulkupload;

import org.chenile.orchestrator.process.api.ProcessManager;
import org.chenile.orchestrator.process.model.Constants;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.jdbc.JdbcProcessWorkerRunner;
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
	@Autowired @Qualifier("_processStateEntityService_") ProcessManager processManager;

	@Test
	void completesBulkUploadWithChunkErrorsAndAggregateSummary() throws Exception {
		String uploadId = UUID.randomUUID().toString();
		String objectKey = "uploads/" + uploadId + "/input.csv";
		byte[] content = "one,ok\n,error\nthree,ok\nERROR,bad\n".getBytes(StandardCharsets.UTF_8);
		objectStore.put(objectKey, new ByteArrayInputStream(content), content.length, "text/csv");
		repository.createUpload(uploadId, uploadId, objectKey);

		batchService.startUpload(new BulkUploadProcessInput(uploadId, objectKey, 2));

		for (int i = 0; i < 10; i++) {
			runner.processOne();
			Process process = processManager.retrieve(uploadId).getMutatedEntity();
			if (Constants.States.PROCESSED.equals(process.getCurrentState().getStateId())
					|| Constants.States.PROCESSED_WITH_ERRORS.equals(process.getCurrentState().getStateId())) {
				break;
			}
		}

		Process process = processManager.retrieve(uploadId).getMutatedEntity();
		Assertions.assertEquals(Constants.States.PROCESSED, process.getCurrentState().getStateId());
		var status = repository.status(uploadId).orElseThrow();
		Assertions.assertEquals(4, status.totalRows());
		Assertions.assertEquals(2, status.successRows());
		Assertions.assertEquals(2, status.errorRows());
		Assertions.assertEquals("SUCCESS_WITH_ERRORS", status.status());
		Assertions.assertNotNull(status.resultObjectKey());
	}
}
