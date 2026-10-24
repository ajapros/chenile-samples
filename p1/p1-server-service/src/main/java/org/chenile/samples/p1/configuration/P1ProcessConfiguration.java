package org.chenile.samples.p1.configuration;

import org.chenile.pubsub.configuration.PubSubConfiguration;
import org.chenile.pubsub.kafka.configuration.KafkaConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;

@Configuration
@Import({PubSubConfiguration.class, KafkaConfiguration.class})
public class P1ProcessConfiguration {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper processObjectMapper() {
        return new ObjectMapper();
    }

}
