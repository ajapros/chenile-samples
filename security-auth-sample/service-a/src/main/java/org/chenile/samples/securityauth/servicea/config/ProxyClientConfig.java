package org.chenile.samples.securityauth.servicea.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProxyClientConfig {

    @Bean
    RestClient serviceBRestClient(@Value("${sample.security.service-b-uri:http://localhost:8082}") String serviceBUri) {
        return RestClient.builder()
                .baseUrl(serviceBUri)
                .build();
    }
}
