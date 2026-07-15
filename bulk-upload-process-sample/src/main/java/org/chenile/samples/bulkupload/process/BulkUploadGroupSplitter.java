package org.chenile.samples.bulkupload.process;

import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.model.payload.SubProcessPayload;
import org.chenile.orchestrator.process.utils.base.SplitterBase;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadChunkInput;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadGroupInput;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("bulkUploadGroupSplitter")
public class BulkUploadGroupSplitter extends SplitterBase<BulkUploadGroupInput, BulkUploadChunkInput> {
	private final BulkUploadRepository repository;
	private final ObjectStore objectStore;

	public BulkUploadGroupSplitter(BulkUploadRepository repository, ObjectStore objectStore) {
		this.repository = repository;
		this.objectStore = objectStore;
	}

	@Override
	protected List<SubProcessPayload> doStart(WorkerDto workerDto, BulkUploadGroupInput input) {
		try {
			repository.startGroup(input.uploadId(), input.groupNumber());
			repository.audit(input.uploadId(), workerDto.process.getId(), "GROUP_SPLIT_STARTED", "SPLITTER",
					"RUNNING", "Splitting group " + input.groupNumber() + " into chunks", null);
			int totalLines = repository.rowCount(input.objectKey(), objectStore);
			List<SubProcessPayload> payloads = new ArrayList<>();
			for (int chunkNumber = input.startChunkNumber(); chunkNumber <= input.endChunkNumber(); chunkNumber++) {
				int startLine = ((chunkNumber - 1) * input.chunkSize()) + 1;
				int endLine = Math.min(startLine + input.chunkSize() - 1, totalLines);
				if (startLine > totalLines) {
					break;
				}
				repository.saveChunk(input.uploadId(), input.groupNumber(), chunkNumber, startLine, endLine);
				SubProcessPayload payload = makeSubProcessPayload(
						new BulkUploadChunkInput(input.uploadId(), input.objectKey(), input.groupNumber(), chunkNumber,
								startLine, endLine),
						"bulkUploadChunk");
				payload.childId = input.uploadId() + "-group-" + input.groupNumber() + "-chunk-" + chunkNumber;
				payload.leaf = true;
				payloads.add(payload);
			}
			repository.audit(input.uploadId(), workerDto.process.getId(), "GROUP_SPLIT_FINISHED", "SPLITTER",
					"SUCCESS", "Created " + payloads.size() + " chunk subprocesses", null);
			return payloads;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to split upload group " + input.groupNumber(), e);
		}
	}
}
