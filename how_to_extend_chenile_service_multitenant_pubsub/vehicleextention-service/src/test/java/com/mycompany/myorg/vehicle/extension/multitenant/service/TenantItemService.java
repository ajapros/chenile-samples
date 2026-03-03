package com.mycompany.myorg.vehicle.extension.multitenant.service;

import com.mycompany.myorg.vehicle.extension.multitenant.model.TenantItem;
import com.mycompany.myorg.vehicle.extension.multitenant.repository.TenantItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("tenantItemService")
public class TenantItemService {

    @Autowired
    private TenantItemRepository repository;

    public Map<String, Object> items() {
        List<TenantItem> items = repository.findAll();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", items);
        return payload;
    }
}
