package com.mycompany.myorg.p1.api;

import com.mycompany.myorg.p1.model.BatchIn;
import org.chenile.orchestrator.process.model.Process;

public interface BatchService {
    Process start(BatchIn input);
}
