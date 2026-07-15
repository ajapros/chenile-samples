package org.chenile.samples.bulkupload.process;

import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.model.payload.SubProcessPayload;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadGroupInput;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadProcessInput;
import org.chenile.orchestrator.process.utils.base.SplitterBase;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("bulkUploadSplitter")
public class BulkUploadSplitter extends SplitterBase<BulkUploadProcessInput, BulkUploadGroupInput> {
	private final BulkUploadRepository repository;
	private final ObjectStore objectStore;

	public BulkUploadSplitter(BulkUploadRepository repository, ObjectStore objectStore) {
		this.repository = repository;
		this.objectStore = objectStore;
	}

	@Override
	protected List<SubProcessPayload> doStart(WorkerDto workerDto, BulkUploadProcessInput input) {
		try {
			repository.markRunning(input.uploadId());
			repository.audit(input.uploadId(), workerDto.process.getId(), "ROOT_SPLIT_STARTED", "SPLITTER",
					"RUNNING", "Splitting upload into nested groups", null);
			int totalLines = repository.rowCount(input.objectKey(), objectStore);
			int chunkSize = Math.max(input.chunkSize(), 1);
			int chunksPerGroup = Math.max(input.chunksPerGroup(), 1);
			int totalChunks = (int) Math.ceil(totalLines / (double) chunkSize);
			List<SubProcessPayload> payloads = new ArrayList<>();
			int groupNumber = 1;
			for (int startChunk = 1; startChunk <= totalChunks; startChunk += chunksPerGroup) {
				int endChunk = Math.min(startChunk + chunksPerGroup - 1, totalChunks);
				repository.saveGroup(input.uploadId(), groupNumber, startChunk, endChunk);
				SubProcessPayload payload = makeSubProcessPayload(
						new BulkUploadGroupInput(input.uploadId(), input.objectKey(), groupNumber, startChunk, endChunk,
								chunkSize),
						"bulkUploadGroup");
				payload.childId = input.uploadId() + "-group-" + groupNumber;
				payload.leaf = false;
				payloads.add(payload);
				groupNumber++;
			}
			repository.audit(input.uploadId(), workerDto.process.getId(), "ROOT_SPLIT_FINISHED", "SPLITTER",
					"SUCCESS", "Created " + payloads.size() + " group subprocesses", null);
			return payloads;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to split upload " + input.uploadId(), e);
		}
	}
}
