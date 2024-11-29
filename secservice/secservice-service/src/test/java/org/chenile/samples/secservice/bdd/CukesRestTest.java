package org.chenile.samples.secservice.bdd;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.chenile.security.test.BaseSecurityTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features",
    glue = {"classpath:org/chenile/samples/secservice/bdd", "classpath:org/chenile/cucumber/rest",
            "classpath:org/chenile/cucumber/security/rest"},
    plugin = {"pretty"}
)
@ActiveProfiles("unittest")
public  class CukesRestTest {

}

