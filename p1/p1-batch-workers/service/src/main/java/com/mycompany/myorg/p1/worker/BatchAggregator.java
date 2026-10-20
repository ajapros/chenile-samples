package com.mycompany.myorg.p1.worker;

import com.mycompany.myorg.p1.model.BatchIn;
import com.mycompany.myorg.p1.model.*;
import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.utils.AggregatedResult;
import org.chenile.orchestrator.process.utils.base.AggregatorBase;
import org.chenile.orchestrator.process.model.payload.SubProcessError;

/** Collects immediate child outputs and errors into a typed result. */
public class BatchAggregator extends AggregatorBase<BatchIn, AggregatedResult<ItemOutput>, ItemOutput> {
    @Override
    protected AggregatedResult<ItemOutput> doStart(AggregatedResult<ItemOutput> out, BatchIn input,
            ItemOutput childOutput, WorkerDto worker, Process child) {
        if (out == null) {
            out = new AggregatedResult<>();
        }
        if (child.errors == null || child.errors.isEmpty()) {
            out.completedChildren++;
        } else {
            out.failedChildren++;
            for (SubProcessError error : child.errors) {
                if (error.errors != null) out.errors.addAll(error.errors);
                if (error.exceptionMessage != null && !error.exceptionMessage.isBlank()) {
                    out.errors.add(error.exceptionMessage);
                }
            }
        }
        if (childOutput != null) {
            out.outputs.add(childOutput);
        }
        return out;
    }
}
