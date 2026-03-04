package com.mycompany.myorg.vehicle.extension.configuration;

import com.mycompany.myorg.vehicle.extension.service.cmd.Tenant1ExtVehicleAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Tenant1VehicleConfiguration {

    @Bean("tenant1VehicleExt")
    Tenant1ExtVehicleAction tenant1VehicleExt() {
        return new Tenant1ExtVehicleAction();
    }
}
