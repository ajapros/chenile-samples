package com.mycompany.myorg.vehicle.configuration.dao;

import com.mycompany.myorg.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository  public interface VehicleRepository extends JpaRepository<Vehicle,String> {}
