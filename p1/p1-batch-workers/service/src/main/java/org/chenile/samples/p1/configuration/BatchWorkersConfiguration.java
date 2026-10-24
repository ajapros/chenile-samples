package org.chenile.samples.p1.configuration;

import org.chenile.samples.p1.worker.*;
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
