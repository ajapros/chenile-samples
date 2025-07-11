package org.chenile.samples.configuration;

import org.chenile.orchestrator.process.service.defs.PostSaveHook;
import org.chenile.orchestrator.process.service.defs.ProcessConfigurator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientProcessConfiguration {

    @Bean
    public RemoteWorkerStarterDelegator remoteWorkerStarterDelegator(
            @Qualifier("postSaveHook") PostSaveHook postSaveHook,
            @Qualifier("processConfigurator") ProcessConfigurator processConfigurator
    ){
        RemoteWorkerStarterDelegator workerStarter = new RemoteWorkerStarterDelegator();
        postSaveHook.setWorkerStarter(workerStarter);
        processConfigurator.read("def.json");
        return workerStarter;
    }
}
