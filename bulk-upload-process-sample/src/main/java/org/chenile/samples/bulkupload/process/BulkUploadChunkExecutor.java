package org.chenile.samples.bulkupload.process;

import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.utils.base.ExecutorBase;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadChunkInput;
import org.chenile.samples.bulkupload.model.BulkUploadModels.BulkUploadChunkResult;
import org.chenile.samples.bulkupload.store.BulkUploadRepository;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component("bulkUploadChunkExecutor")
public class BulkUploadChunkExecutor extends ExecutorBase<BulkUploadChunkInput, BulkUploadChunkResult> {
	private final BulkUploadRepository repository;
	private final ObjectStore objectStore;

	public BulkUploadChunkExecutor(BulkUploadRepository repository, ObjectStore objectStore) {
		this.repository = repository;
		this.objectStore = objectStore;
	}

	@Override
	protected BulkUploadChunkResult doStart(WorkerDto workerDto, BulkUploadChunkInput input) {
		repository.audit(input.uploadId(), workerDto.process.getId(), "CHUNK_STARTED", "EXECUTOR",
				"RUNNING", "Executing chunk " + input.chunkNumber(), null);
		repository.startChunk(input.uploadId(), input.chunkNumber());
		int total = 0;
		int success = 0;
		int errors = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(objectStore.get(input.objectKey())))) {
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (lineNumber < input.startLine() || lineNumber > input.endLine()) {
					continue;
				}
				total++;
				String error = validate(line);
				boolean rowSuccess = error == null;
				if (rowSuccess) {
					success++;
				} else {
					errors++;
				}
				repository.rowResult(input.uploadId(), input.chunkNumber(), lineNumber, line, rowSuccess, error);
			}
			repository.finishChunk(input.uploadId(), input.chunkNumber(), total, success, errors);
			repository.audit(input.uploadId(), workerDto.process.getId(), "CHUNK_FINISHED", "EXECUTOR",
					errors == 0 ? "SUCCESS" : "SUCCESS_WITH_ERRORS",
					"Finished chunk " + input.chunkNumber(), null);
			return new BulkUploadChunkResult(input.uploadId(), input.chunkNumber(), total, success, errors);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to execute chunk " + input.chunkNumber(), e);
		}
	}

	private String validate(String line) {
		if (line == null || line.isBlank()) {
			return "Blank line";
		}
		if (line.toUpperCase().contains("ERROR")) {
			return "Line contains ERROR marker";
		}
		if (line.split(",", -1)[0].isBlank()) {
			return "First CSV column is required";
		}
		return null;
	}
}
