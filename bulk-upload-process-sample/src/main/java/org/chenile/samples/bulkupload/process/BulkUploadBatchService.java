package org.chenile.samples.bulkupload.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.orchestrator.delegate.ProcessManagerClient;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.utils.base.BatchServiceBase;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadProcessInput;
import org.springframework.stereotype.Service;

@Service
public class BulkUploadBatchService extends BatchServiceBase<BulkUploadProcessInput> {
	private final ObjectMapper objectMapper;
	private final ProcessManagerClient client;

	public BulkUploadBatchService(ObjectMapper objectMapper, ProcessManagerClient client) {
		this.objectMapper = objectMapper;
		this.client = client;
	}

	@Override
	protected String getClientName() {
		return "bulk-upload-sample";
	}

	public Process startUpload(BulkUploadProcessInput input) {
		Process process = new Process();
		process.id = input.uploadId();
		process.clientId = getClientName();
		process.processType = "bulkUpload";
		try {
			process.input = objectMapper.writeValueAsString(input);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to serialize bulk upload input", e);
		}
		return client.start(process);
	}
}
