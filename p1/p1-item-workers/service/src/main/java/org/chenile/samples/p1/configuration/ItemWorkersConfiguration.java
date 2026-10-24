package org.chenile.samples.p1.configuration;

import org.chenile.samples.p1.worker.*;
import org.springframework.context.annotation.*;

@Configuration
public class ItemWorkersConfiguration {
    @Bean(name = "itemExecutor")
    public ItemExecutor executor() {
        return new ItemExecutor();
    }
}
