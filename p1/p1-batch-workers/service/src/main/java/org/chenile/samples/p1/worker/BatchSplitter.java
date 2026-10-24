package org.chenile.samples.p1.worker;

import java.util.ArrayList;
import java.util.List;
import org.chenile.samples.p1.model.BatchIn;
import org.chenile.samples.p1.model.ItemIn;
import org.chenile.orchestrator.process.model.payload.SubProcessPayload;
import org.chenile.orchestrator.process.worker.SplitterWorkListener;
import org.chenile.orchestrator.process.workers.WorkListenerInput;

public class BatchSplitter extends SplitterWorkListener<BatchIn, Object> {
    @Override
    protected List<SubProcessPayload> doStart(WorkListenerInput<BatchIn> input) {
        List<SubProcessPayload> payloads = new ArrayList<>();
        for (int i = 0; i < input.payload.count; i++) {
            payloads.add(makeSubProcessPayload(new ItemIn(i), "item"));
        }
        return payloads;
    }
}
