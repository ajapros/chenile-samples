package org.chenile.samples.configuration;

import org.chenile.orchestrator.process.model.Process;
import org.chenile.orchestrator.process.WorkerStarter;
import org.chenile.orchestrator.process.model.WorkerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class WorkerStarterDelegator implements WorkerStarter {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void start(Process process, Map<String, String> execDef, WorkerType workerType) {
        String componentName = process.processType + camelCase(workerType);
        try {
            WorkerStarter workerStarter = (WorkerStarter) applicationContext.getBean(componentName);
            workerStarter.start(process, execDef,workerType);
        }catch(Exception ignore){
            ignore.printStackTrace();
        }
    }

    private String camelCase(WorkerType workerType){
        String s =  workerType.name().toLowerCase();
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}