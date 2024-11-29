package org.chenile.samples.order.query.service.bdd;

import org.chenile.samples.order.MockSS;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.java.en.Given;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = SpringTestConfig.class,
  properties = {"spring.profiles.active=unittest"})
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public class RestQueryCukesSteps {
	// Create a dummy method so that Cucumber thinks of this as a steps implementation.
	@Given("dummy")
	public void dummy() {
		
	}

    @Given("that user is {string}")
    public void thatUserIs(String user) {
		MockSS.userName = user;
    }
}
