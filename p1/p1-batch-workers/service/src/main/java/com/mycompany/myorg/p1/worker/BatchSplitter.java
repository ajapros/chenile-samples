package com.mycompany.myorg.p1.worker;

import com.mycompany.myorg.p1.model.*;
import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.model.payload.SubProcessPayload;
import org.chenile.orchestrator.process.utils.base.SplitterBase;
import com.mycompany.myorg.p1.model.BatchIn;
import java.util.List;
import java.util.ArrayList;

public class BatchSplitter extends SplitterBase<BatchIn, Object> {
    @Override
    protected List<SubProcessPayload> doStart(WorkerDto worker, BatchIn input) {
        // Return makeSubProcessPayload(typedChildInput, childProcessType) for every child.
        List<SubProcessPayload> payloads = new ArrayList<>();
        for(int i = 0; i < input.count;i++) {
            payloads.add(makeSubProcessPayload(new ItemIn(i),"item"));
        }
        return payloads;
        // throw new UnsupportedOperationException("Implement BatchSplitter.doStart");
    }
}
