package com.mycompany.myorg.p1.configuration;

import com.mycompany.myorg.p1.worker.*;
import org.springframework.context.annotation.*;

@Configuration
public class ItemWorkersConfiguration {
    @Bean(name = "itemExecutor")
    public ItemExecutor executor() {
        return new ItemExecutor();
    }
}
