package org.chenile.samples.s1.bdd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.chenile.samples.s1.SpringTestConfig;

import cucumber.api.java.en.Given;


/**
 * Dummy steps for hooking up spring config
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringTestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public class CukesSteps {

    @Given("dummy")
    public void dummy() {
    }
}
