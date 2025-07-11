package org.chenile.samples.service.commands;

import org.chenile.orchestrator.delegate.ProcessManagerClient;
import org.chenile.orchestrator.process.WorkerStarter;
import org.chenile.orchestrator.process.model.*;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.workflow.api.StateEntityService;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Map;

public class FeedSplitter implements WorkerStarter {
    /**
     * This gives a configuration option to change the #files as part of
     * individual test cases. See TestFeeds for the usage of this parameter.
     */
    public static int numFiles = 1;
    @Autowired
    ProcessManagerClient processManagerClient ;
    @Override
    public void start(Process process, Map<String, String> execDef, WorkerType workerType) {
        StartProcessingPayload payload = new StartProcessingPayload();
        payload.subProcesses = new ArrayList<>();
        for (int i = 0; i < numFiles;i++) {
            SubProcessPayload p = new SubProcessPayload();
            p.processType = "file";
            p.childId = process.id + "FILE" + (i+1);
            p.args = """
                    { "filename" : "file%d" }
                    """.formatted(i+1);
            // Assert if the splitter args are passed from def.json to this method
            Assert.assertEquals("splitter_value",execDef.get("splitter_key"));
            payload.subProcesses.add(p);
        }
        processManagerClient.splitDone(process.getId(), payload);

    }
}
