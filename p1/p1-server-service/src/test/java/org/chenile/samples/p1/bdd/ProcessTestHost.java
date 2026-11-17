package org.chenile.samples.p1.bdd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication(
        scanBasePackages = {
            "org.chenile.configuration",
            "org.chenile.orchestrator.process.configuration",
            "org.chenile.samples.p1.configuration"
        })
@EntityScan({
    "org.chenile.orchestrator.process.model",
    "org.chenile.orchestrator.process.configuration.model",
    "org.chenile.scheduler.db"
})
@EnableJpaRepositories({
    "org.chenile.orchestrator.process.configuration.dao", "org.chenile.scheduler.db"
})
public class ProcessTestHost {}
