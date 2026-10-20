package com.mycompany.myorg.p1.worker;

import com.mycompany.myorg.p1.model.ItemIn;
import com.mycompany.myorg.p1.model.ItemOutput;
import org.chenile.orchestrator.process.model.WorkerDto;
import org.chenile.orchestrator.process.utils.base.ExecutorBase;

public class ItemExecutor extends ExecutorBase<ItemIn, ItemOutput> {
    @Override
    protected ItemOutput doStart(WorkerDto worker, ItemIn input) {
        // Transform the typed input into the typed output.
        // throw new UnsupportedOperationException("Implement ItemExecutor.doStart");
        return new ItemOutput();
    }
}
