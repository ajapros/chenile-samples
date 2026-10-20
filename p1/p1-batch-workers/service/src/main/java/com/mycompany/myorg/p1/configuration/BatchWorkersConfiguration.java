package com.mycompany.myorg.p1.configuration;

import com.mycompany.myorg.p1.worker.*;
import org.springframework.context.annotation.*;

@Configuration
public class BatchWorkersConfiguration {
    @Bean(name = "batchSplitter")
    public BatchSplitter splitter() {
        return new BatchSplitter();
    }

    @Bean(name = "batchAggregator")
    public BatchAggregator aggregator() {
        return new BatchAggregator();
    }
}
