package com.mycompany.myorg.vehicle.packager.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/multitenant-service.feature",
        glue = {
                "classpath:com/mycompany/myorg/vehicle/packager/bdd",
                "classpath:org/chenile/cucumber/rest"
        },
        plugin = {"pretty"}
)
public class CukesRestTest {
}
