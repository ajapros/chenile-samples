package com.mycompany.myorg.vehicle.extension.configuration;

import com.mycompany.myorg.vehicle.extension.service.cmd.ExtVehicleAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VehicleExtentionConfiguration {
    @Bean
    ExtVehicleAction vehicleExt() {
        return new ExtVehicleAction();
    }

}
