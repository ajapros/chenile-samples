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

    @Then("DB has extension vehicle row with extType {string} openedBy {string} description {string} policy {string} fitness {string}")
    public void dbHasExtensionVehicleRow(String extType, String openedBy, String description, String policy, String fitness) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select ext_type, opened_by, description, assignee, assign_comment, resolve_comment, close_comment, insurance_policy_number, fitness_expiry " +
                        "from vehicle where ext_type = ? and insurance_policy_number = ? and fitness_expiry = ?",
                extType,
                policy,
                fitness
        );

        assertValue(row, "ext_type", extType);
        assertValue(row, "opened_by", openedBy);
        assertValue(row, "description", description);
        assertValue(row, "insurance_policy_number", policy);
        assertValue(row, "fitness_expiry", fitness);

        assertNullValue(row, "assignee");
        assertNullValue(row, "assign_comment");
        assertNullValue(row, "resolve_comment");
        assertNullValue(row, "close_comment");
    }

    private void assertValue(Map<String, Object> row, String key, String expected) {
        Object actual = row.get(key);
        String actualText = actual == null ? null : String.valueOf(actual);
        if (expected == null ? actual != null : !expected.equals(actualText)) {
            throw new AssertionError("Expected " + key + "=" + expected + " but found " + actualText);
        }
    }

    private void assertNullValue(Map<String, Object> row, String key) {
        if (row.get(key) != null) {
            throw new AssertionError("Expected " + key + " to be null but found " + row.get(key));
        }
    }
}
