package com.mycompany.myorg.vehicle.extension.bdd;

import com.mycompany.myorg.vehicle.extension.VehicleExtensionSpringTestConfig;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = VehicleExtensionSpringTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class CukesSteps {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Given("dummy")
    public void dummy() {
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
