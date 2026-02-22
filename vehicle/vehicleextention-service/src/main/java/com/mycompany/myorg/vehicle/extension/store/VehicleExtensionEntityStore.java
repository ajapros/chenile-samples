package com.mycompany.myorg.vehicle.extension.store;

import java.util.Optional;

import org.chenile.base.exception.NotFoundException;
import org.chenile.utils.entity.service.EntityStore;
import org.springframework.beans.factory.annotation.Autowired;

import com.mycompany.myorg.vehicle.configuration.dao.VehicleRepository;
import com.mycompany.myorg.vehicle.model.Vehicle;
import com.mycompany.myorg.vehicle.model.VehicleExtension;

public class VehicleExtensionEntityStore implements EntityStore<VehicleExtension> {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public void store(VehicleExtension entity) {
        vehicleRepository.save(entity);
    }

    @Override
    public VehicleExtension retrieve(String id) {
        Optional<Vehicle> entity = vehicleRepository.findById(id);
        if (entity.isPresent() && entity.get() instanceof VehicleExtension vehicleExtension) {
            return vehicleExtension;
        }
        throw new NotFoundException(1501, "Unable to find VehicleExtension with ID " + id);
    }
}
