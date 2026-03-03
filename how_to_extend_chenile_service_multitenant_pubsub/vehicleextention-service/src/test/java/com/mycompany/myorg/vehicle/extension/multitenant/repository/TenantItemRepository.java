package com.mycompany.myorg.vehicle.extension.multitenant.repository;

import com.mycompany.myorg.vehicle.extension.multitenant.model.TenantItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantItemRepository extends JpaRepository<TenantItem, Long> {
}
