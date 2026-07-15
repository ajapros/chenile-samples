package org.chenile.samples.bulkupload.process;

import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.model.payload.SubProcessPayload;
import org.chenile.orchestrator.process.utils.base.SplitterBase;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadChunkInput;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadProcessInput;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("bulkUploadSplitter")
public class BulkUploadSplitter extends SplitterBase<BulkUploadProcessInput, BulkUploadChunkInput> {
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
			int totalLines = repository.rowCount(input.objectKey(), objectStore);
			int chunkSize = Math.max(input.chunkSize(), 1);
			List<SubProcessPayload> payloads = new ArrayList<>();
			int chunkNumber = 1;
			for (int start = 1; start <= totalLines; start += chunkSize) {
				int end = Math.min(start + chunkSize - 1, totalLines);
				repository.saveChunk(input.uploadId(), chunkNumber, start, end);
				SubProcessPayload payload = makeSubProcessPayload(
						new BulkUploadChunkInput(input.uploadId(), input.objectKey(), chunkNumber, start, end),
						"bulkUploadChunk");
				payload.childId = input.uploadId() + "-chunk-" + chunkNumber;
				payload.leaf = true;
				payloads.add(payload);
				chunkNumber++;
			}
			return payloads;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to split upload " + input.uploadId(), e);
		}
	}
}
