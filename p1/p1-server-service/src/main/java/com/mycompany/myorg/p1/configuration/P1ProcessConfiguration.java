package com.mycompany.myorg.p1.configuration;

import com.mycompany.myorg.p1.service.*;
import org.chenile.orchestrator.process.api.ProcessManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;

@Configuration
public class P1ProcessConfiguration {
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
            com.fasterxml.jackson.databind.ObjectMapper.class)
    public com.fasterxml.jackson.databind.ObjectMapper processObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }

    @Bean
    public BatchServiceImpl _batchService_(
            @Qualifier("_processStateEntityService_") ProcessManager manager) {
        return new BatchServiceImpl(manager);
    }
    @Bean
    public ItemServiceImpl _itemService_(
            @Qualifier("_processStateEntityService_") ProcessManager manager) {
        return new ItemServiceImpl(manager);
    }
}
