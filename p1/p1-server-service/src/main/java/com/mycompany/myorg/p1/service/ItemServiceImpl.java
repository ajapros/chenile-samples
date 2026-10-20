package com.mycompany.myorg.p1.service;

import com.mycompany.myorg.p1.api.ItemService;
import com.mycompany.myorg.p1.model.ItemIn;
import org.chenile.orchestrator.process.model.Process;

public class ItemServiceImpl implements ItemService {
    private final org.chenile.orchestrator.process.api.ProcessManager manager;
    public ItemServiceImpl(org.chenile.orchestrator.process.api.ProcessManager manager) { this.manager = manager; }
    @Override public Process start(ItemIn input) {
        if (input == null) throw new org.chenile.base.exception.BadRequestException("ItemIn is required");
        if (org.chenile.core.context.ContextContainer.CONTEXT_CONTAINER.getTenant().isBlank())
            throw new org.chenile.base.exception.BadRequestException("x-chenile-tenant-id is required");
        var dto = new org.chenile.orchestrator.process.model.ProcessDto();
        dto.processDefName = "item";
        dto.args = new com.fasterxml.jackson.databind.ObjectMapper().convertValue(input,
                new com.fasterxml.jackson.core.type.TypeReference<java.util.LinkedHashMap<String,Object>>() {});
        return manager.create(dto).getMutatedEntity();
    }
}
