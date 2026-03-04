package com.mycompany.myorg.vehicle.tenant1.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/service.feature",
        glue = {
                "classpath:com/mycompany/myorg/vehicle/tenant1/bdd",
                "classpath:org/chenile/cucumber/rest"
        },
        plugin = {"pretty"}
)
@ActiveProfiles("unittest")
public class CukesRestTest {
}
