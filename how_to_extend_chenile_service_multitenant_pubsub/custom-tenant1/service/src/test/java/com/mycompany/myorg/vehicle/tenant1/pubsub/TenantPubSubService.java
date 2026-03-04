package com.mycompany.myorg.vehicle.tenant1.pubsub;

import org.chenile.core.context.ContextContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("tenantPubSubService")
public class TenantPubSubService {

    @Autowired
    private PubSubSharedData sharedData;

    public Map<String, Object> consume(PubSubPayload payload) {
        sharedData.record(ContextContainer.getInstance().getTenant(), payload.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }
}
