package org.chenile.samples.p1.worker;

import org.chenile.samples.p1.model.BatchIn;
import org.chenile.samples.p1.model.*;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.utils.AggregatedResult;
import org.chenile.orchestrator.process.worker.AggregatorWorkListener;
import org.chenile.orchestrator.process.model.payload.SubProcessError;
import org.chenile.orchestrator.process.workers.WorkListenerInput;

/** Collects immediate child outputs and errors into a typed result. */
public class BatchAggregator extends AggregatorWorkListener<BatchIn, AggregatedResult<ItemOutput>, ItemOutput> {
    @Override
    protected AggregatedResult<ItemOutput> doStart(
            AggregatedResult<ItemOutput> out,
            WorkListenerInput<BatchIn> input,
            ItemOutput childOutput,
            Process child) {
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
