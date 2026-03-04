package com.mycompany.myorg.vehicle.tenant1.bdd;

import com.mycompany.myorg.vehicle.tenant1.VehicleTenant1SpringTestConfig;
import com.mycompany.myorg.vehicle.tenant1.pubsub.PubSubSharedData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = VehicleTenant1SpringTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class CukesSteps {
    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Then("DB has tenant1 extension row with code {string} segment {string} priority {string} workflowNote {string}")
    public void dbHasTenant1ExtensionRow(String code, String segment, String priority, String workflowNote) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select ext_type, tenant1_code, tenant1_segment, tenant1_priority, tenant1_workflow_note " +
                        "from vehicle where ext_type = 'tenant1_ext' and tenant1_code = ?",
                code
        );

        assertValue(row, "ext_type", "tenant1_ext");
        assertValue(row, "tenant1_code", code);
        assertValue(row, "tenant1_segment", segment);
        assertValue(row, "tenant1_priority", priority);
        assertValue(row, "tenant1_workflow_note", workflowNote);
    }

    private void assertValue(Map<String, Object> row, String key, String expected) {
        Object actual = row.get(key);
        String actualText = actual == null ? null : String.valueOf(actual);
        if (expected == null ? actual != null : !expected.equals(actualText)) {
            throw new AssertionError("Expected " + key + "=" + expected + " but found " + actualText);
        }
    }
}
