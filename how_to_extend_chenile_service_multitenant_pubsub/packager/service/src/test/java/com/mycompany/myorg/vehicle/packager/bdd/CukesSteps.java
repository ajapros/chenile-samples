package com.mycompany.myorg.vehicle.packager.bdd;

import com.mycompany.myorg.vehicle.packager.PackagerSpringTestConfig;
import com.mycompany.myorg.vehicle.packager.pubsub.PubSubSharedData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = PackagerSpringTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
public class CukesSteps {
    @Autowired
    private PubSubSharedData pubSubSharedData;

    @Given("I reset pubsub capture for {int} event")
    public void resetPubsubCaptureForEvent(int count) {
        pubSubSharedData.reset(count);
    }

    @Then("pubsub receives message containing {string} and tenant {string}")
    public void pubsubReceivesMessageContainingAndTenant(String text, String tenant) throws InterruptedException {
        boolean arrived = pubSubSharedData.latch.await(5, TimeUnit.SECONDS);
        if (!arrived) {
            throw new AssertionError("Timed out waiting for pub/sub message");
        }

        boolean messageFound = pubSubSharedData.messages.stream().anyMatch(m -> m != null && m.contains(text));
        if (!messageFound) {
            throw new AssertionError("No pub/sub message contains text: " + text + ". Messages=" + pubSubSharedData.messages);
        }

        boolean tenantFound = pubSubSharedData.tenantsSeen.stream().anyMatch(tenant::equals);
        if (!tenantFound) {
            throw new AssertionError("Expected tenant " + tenant + " in pub/sub receiver. Seen=" + pubSubSharedData.tenantsSeen);
        }
    }
}
