package com.mycompany.myorg.vehicle.extension.configuration;

import com.mycompany.myorg.vehicle.extension.service.cmd.Tenant0ExtVehicleAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VehicleExtentionConfiguration {
    @Bean("tenant0VehicleExt")
    Tenant0ExtVehicleAction vehicleExt() {
        return new Tenant0ExtVehicleAction();
    }

}
