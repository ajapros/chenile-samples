package org.chenile.samples.bulkupload.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.utils.base.AggregatorBase;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadAggregateResult;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadChunkResult;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadProcessInput;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component("bulkUploadAggregator")
public class BulkUploadAggregator extends AggregatorBase<BulkUploadProcessInput, BulkUploadAggregateResult, BulkUploadChunkResult> {
	private final BulkUploadRepository repository;
	private final ObjectStore objectStore;
	private final ObjectMapper objectMapper;

	public BulkUploadAggregator(BulkUploadRepository repository, ObjectStore objectStore, ObjectMapper objectMapper) {
		this.repository = repository;
		this.objectStore = objectStore;
		this.objectMapper = objectMapper;
	}

	@Override
	protected BulkUploadAggregateResult doStart(BulkUploadAggregateResult out, BulkUploadProcessInput input,
			BulkUploadChunkResult childOutput, WorkerDto workerDto, Process process) {
		int total = out == null ? 0 : out.totalRows();
		int success = out == null ? 0 : out.successRows();
		int errors = out == null ? 0 : out.errorRows();
		if (childOutput != null) {
			total += childOutput.totalRows();
			success += childOutput.successRows();
			errors += childOutput.errorRows();
		}
		String status = errors == 0 ? "SUCCESS" : "SUCCESS_WITH_ERRORS";
		BulkUploadAggregateResult aggregate = new BulkUploadAggregateResult(input.uploadId(), total, success, errors, status);
		String resultObjectKey = "results/" + input.uploadId() + "/summary.json";
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(aggregate);
			objectStore.put(resultObjectKey, new ByteArrayInputStream(bytes), bytes.length, "application/json");
			repository.finishUpload(input.uploadId(), total, success, errors, resultObjectKey);
			return aggregate;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to aggregate upload " + input.uploadId(), e);
		}
	}
}
