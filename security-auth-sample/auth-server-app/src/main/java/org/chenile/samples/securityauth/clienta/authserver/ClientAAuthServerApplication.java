package org.chenile.samples.securityauth.clienta.authserver;

import org.chenile.security.auth.server.config.AuthServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"org.chenile.security.auth.server", "org.chenile.samples.securityauth.clienta.iam.implementation"})
@EnableConfigurationProperties(AuthServerProperties.class)
public class ClientAAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientAAuthServerApplication.class, args);
    }
}
