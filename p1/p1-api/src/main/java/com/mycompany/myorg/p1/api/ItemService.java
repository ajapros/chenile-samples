package com.mycompany.myorg.p1.api;

import com.mycompany.myorg.p1.model.ItemIn;
import org.chenile.orchestrator.process.model.Process;

public interface ItemService {
    Process start(ItemIn input);
}
