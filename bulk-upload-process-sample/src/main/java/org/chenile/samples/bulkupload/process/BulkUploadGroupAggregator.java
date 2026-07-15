package org.chenile.samples.bulkupload.process;

import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.utils.base.AggregatorBase;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadChunkResult;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadGroupInput;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadGroupResult;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.springframework.stereotype.Component;

@Component("bulkUploadGroupAggregator")
public class BulkUploadGroupAggregator
		extends AggregatorBase<BulkUploadGroupInput, BulkUploadGroupResult, BulkUploadChunkResult> {
	private final BulkUploadRepository repository;

	public BulkUploadGroupAggregator(BulkUploadRepository repository) {
		this.repository = repository;
	}

	@Override
	protected BulkUploadGroupResult doStart(BulkUploadGroupResult out, BulkUploadGroupInput input,
			BulkUploadChunkResult childOutput, WorkerDto workerDto, Process process) {
		int total = out == null ? 0 : out.totalRows();
		int success = out == null ? 0 : out.successRows();
		int errors = out == null ? 0 : out.errorRows();
		if (childOutput != null) {
			total += childOutput.totalRows();
			success += childOutput.successRows();
			errors += childOutput.errorRows();
		}
		BulkUploadGroupResult result = new BulkUploadGroupResult(input.uploadId(), input.groupNumber(),
				total, success, errors);
		repository.finishGroup(input.uploadId(), input.groupNumber(), total, success, errors);
		repository.audit(input.uploadId(), workerDto.process.getId(), "GROUP_AGGREGATED", "AGGREGATOR",
				errors == 0 ? "SUCCESS" : "SUCCESS_WITH_ERRORS",
				"Aggregated group " + input.groupNumber(), null);
		return result;
	}
}
