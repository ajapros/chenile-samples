package com.mycompany.myorg.vehicle.extension.bdd;

import com.mycompany.myorg.vehicle.extension.VehicleExtensionSpringTestConfig;
import com.mycompany.myorg.vehicle.extension.pubsub.PubSubSharedData;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = VehicleExtensionSpringTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class CukesSteps {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PubSubSharedData pubSubSharedData;

    @Given("dummy")
    public void dummy() {
    }

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

        if (tenant == null || tenant.isBlank()) {
            boolean blankTenantSeen = pubSubSharedData.tenantsSeen.stream().anyMatch(t -> t == null || t.isBlank());
            if (!blankTenantSeen) {
                throw new AssertionError("Expected blank tenant in pub/sub receiver. Seen=" + pubSubSharedData.tenantsSeen);
            }
            return;
        }

        boolean tenantFound = pubSubSharedData.tenantsSeen.stream().anyMatch(tenant::equals);
        if (!tenantFound) {
            throw new AssertionError("Expected tenant " + tenant + " in pub/sub receiver. Seen=" + pubSubSharedData.tenantsSeen);
        }
    }

    @Then("DB has tenant0 extension row with code {string} segment {string} priority {string} workflowNote {string}")
    public void dbHasTenant0ExtensionRow(String code, String segment, String priority, String workflowNote) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select ext_type, tenant0_code, tenant0_segment, tenant0_priority, tenant0_workflow_note, new_column " +
                        "from vehicle where ext_type = 'tenant0_ext' and tenant0_code = ?",
                code
        );

        assertValue(row, "ext_type", "tenant0_ext");
        assertValue(row, "tenant0_code", code);
        assertValue(row, "tenant0_segment", segment);
        assertValue(row, "tenant0_priority", priority);
        assertValue(row, "tenant0_workflow_note", workflowNote);
    }

    private void assertValue(Map<String, Object> row, String key, String expected) {
        Object actual = row.get(key);
        String actualText = actual == null ? null : String.valueOf(actual);
        if (expected == null ? actual != null : !expected.equals(actualText)) {
            throw new AssertionError("Expected " + key + "=" + expected + " but found " + actualText);
        }
    }

}
