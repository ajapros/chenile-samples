package org.chenile.samples.bulkupload.configuration;

import org.chenile.orchestrator.delegate.ProcessManagerClient;
import org.chenile.orchestrator.process.api.ProcessManager;
import org.chenile.orchestrator.process.model.Constants;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.model.payload.*;

import java.util.List;

public class LocalProcessManagerClient implements ProcessManagerClient {
	private final ProcessManager processManager;

	public LocalProcessManagerClient(ProcessManager processManager) {
		this.processManager = processManager;
	}

	@Override
	public Process start(Process process) {
		return processManager.create(process).getMutatedEntity();
	}

	@Override
	public Process splitPartiallyDone(String id, StartProcessingPayload payload) {
		return process(id, Constants.Events.SPLIT_PARTIALLY_DONE, payload);
	}

	@Override
	public Process splitDone(String id, StartProcessingPayload payload) {
		return process(id, Constants.Events.SPLIT_DONE, payload);
	}

	@Override
	public Process aggregationDone(String id, AggregationDonePayload payload) {
		return process(id, Constants.Events.AGGREGATION_DONE, payload);
	}

	@Override
	public Process statusUpdate(String id, StatusUpdatePayload payload) {
		return process(id, Constants.Events.STATUS_UPDATE, payload);
	}

	@Override
	public Process doneSuccessfully(String id, DoneSuccessfullyPayload payload) {
		return process(id, Constants.Events.DONE_SUCCESSFULLY, payload);
	}

	@Override
	public Process doneWithErrors(String id, DoneWithErrorsPayload payload) {
		return process(id, Constants.Events.DONE_WITH_ERRORS, payload);
	}

	@Override
	public Process splitDoneWithErrors(String id, DoneWithErrorsPayload payload) {
		return process(id, Constants.Events.SPLIT_DONE_WITH_ERRORS, payload);
	}

	@Override
	public Process aggregationDoneWithErrors(String id, DoneWithErrorsPayload payload) {
		return process(id, Constants.Events.AGGREGATION_DONE_WITH_ERRORS, payload);
	}

	@Override
	public Process process(String id, String event, Object payload) {
		return processManager.processById(id, event, payload).getMutatedEntity();
	}

	@Override
	public List<Process> getSubProcesses(String id, boolean recursive) {
		return processManager.getSubProcesses(id, recursive);
	}
}
