package com.mycompany.myorg.vehicle.extension.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "classpath:com/mycompany/myorg/vehicle/extension/bdd",
                "classpath:org/chenile/cucumber/rest"
        },
        plugin = {"pretty"}
)
@ActiveProfiles("unittest")
public class CukesRestTest {
}
