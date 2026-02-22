package com.mycompany.myorg.vehicle.service.store;

import org.chenile.utils.entity.service.EntityStore;
import com.mycompany.myorg.vehicle.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.chenile.base.exception.NotFoundException;
import com.mycompany.myorg.vehicle.configuration.dao.VehicleRepository;
import java.util.Optional;

public class VehicleEntityStore implements EntityStore<Vehicle>{
    @Autowired private VehicleRepository vehicleRepository;

	@Override
	public void store(Vehicle entity) {
        vehicleRepository.save(entity);
	}

	@Override
	public Vehicle retrieve(String id) {
        Optional<Vehicle> entity = vehicleRepository.findById(id);
        if (entity.isPresent()) return entity.get();
        throw new NotFoundException(1500,"Unable to find Vehicle with ID " + id);
	}

}
