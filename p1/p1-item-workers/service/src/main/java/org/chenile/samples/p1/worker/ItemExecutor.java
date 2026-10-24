package org.chenile.samples.p1.worker;

import org.chenile.samples.p1.model.ItemIn;
import org.chenile.samples.p1.model.ItemOutput;
import org.chenile.orchestrator.process.worker.ExecutorWorkListener;
import org.chenile.orchestrator.process.workers.WorkListenerInput;

public class ItemExecutor extends ExecutorWorkListener<ItemIn, ItemOutput> {
    @Override
    protected ItemOutput doStart(WorkListenerInput<ItemIn> input) {
        ItemOutput output = new ItemOutput();
        output.itemNumber = Long.valueOf(input.payload.index);
        output.processed = true;
        return output;
    }
}
