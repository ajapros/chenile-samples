package com.mycompany.myorg.vehicle.extension.configuration;

import com.mycompany.myorg.vehicle.extension.service.cmd.ClientAbcExtVehicleAction;
import com.mycompany.myorg.vehicle.extension.service.cmd.ExtVehicleAction;
import com.mycompany.myorg.vehicle.model.VehicleExtension;
import com.mycompany.myorg.vehicle.model.VehicleExtensionClientXyz;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class VehicleExtentionConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        SimpleModule module = new SimpleModule();

        JsonMapper jm = JsonMapper.builder()
                .disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(module)
                .registerSubtypes(new NamedType(VehicleExtension.class, "client_abc_ext"))
                .registerSubtypes(new NamedType(VehicleExtensionClientXyz.class, "client_xyz_ext"))
                .build();

        JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter = new JacksonJsonHttpMessageConverter(jm);
        builder.withJsonConverter(jacksonJsonHttpMessageConverter).build();
    }

    @Bean
    ExtVehicleAction vehicleExt() {
        return new ExtVehicleAction();
    }

    @Bean("tenant1VehicleExt")
    ClientAbcExtVehicleAction tenant1VehicleExt() {
        return new ClientAbcExtVehicleAction();
    }
}
