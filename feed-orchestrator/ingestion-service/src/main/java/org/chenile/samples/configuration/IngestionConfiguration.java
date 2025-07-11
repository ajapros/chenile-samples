package org.chenile.samples.configuration;


import org.chenile.samples.service.commands.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 This is where you will instantiate all the required classes in Spring
*/
@Configuration
public class IngestionConfiguration {


    @Bean
    public WorkerConsumer workerConsumer(){
        return new WorkerConsumer();
    }

    @Bean public WorkerStarterDelegator workerStarterDelegator() {
        return new WorkerStarterDelegator();
    }


    /*

     * The beans below are named appropriately so they can be auto discovered as
     * Process Starters. These are invoked by the post save hook AFTER the Process is
     * saved in the DB with the given state.
     */
    @Bean
    ChunkExecutor chunkExecutor(){
        return new ChunkExecutor();
    }

    @Bean
    FileChunker fileSplitter(){
        return new FileChunker();
    }

    @Bean
    FileAggregator fileAggregator(){
        return new FileAggregator();
    }

    @Bean
    FeedSplitter feedSplitter(){
        return new FeedSplitter();
    }

    @Bean
    FeedAggregator feedAggregator(){
        return new FeedAggregator();
    }


}
