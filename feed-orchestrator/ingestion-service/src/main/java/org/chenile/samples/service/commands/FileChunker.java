package org.chenile.samples.service.commands;

import org.chenile.orchestrator.delegate.ProcessManagerClient;
import org.chenile.orchestrator.process.WorkerStarter;
import org.chenile.orchestrator.process.model.*;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.workflow.api.StateEntityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Map;

public class FileChunker implements WorkerStarter {
    @Autowired
    ProcessManagerClient processManagerClient ;
    @Override
    public void start(Process process, Map<String, String> execDef, WorkerType workerType) {
        StartProcessingPayload payload = new StartProcessingPayload();
        payload.subProcesses = new ArrayList<>();
        SubProcessPayload p = new SubProcessPayload();
        p.processType = "chunk";
        p.childId = process.id + "CHUNK1";
        payload.subProcesses.add(p);
        processManagerClient.splitDone(process.getId(), payload);

    }
}
