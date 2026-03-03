package com.mycompany.myorg.vehicle.bdd;

import com.mycompany.myorg.vehicle.SpringTestConfig;
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

/**
* This "steps" file's purpose is to hook up the SpringConfig to the test case.
* It does not contain any methods currently but can be used for writing your own custom BDD steps
* if required. In most cases people don't need additional steps since cucumber-utils provides for
* most of the steps. <br/>
* This class requires a dummy method to keep Cucumber from erring out. (Cucumber needs at least
* one step in a steps file)<br/>
*/
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = SpringTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("unittest")
public class CukesSteps {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Given("dummy")
    public void dummy() {
    }

    @Then("DB has closed base vehicle row with openedBy {string} description {string} assignee {string} assignComment {string} resolveComment {string} closeComment {string}")
    public void dbHasClosedBaseVehicleRow(
            String openedBy,
            String description,
            String assignee,
            String assignComment,
            String resolveComment,
            String closeComment) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select opened_by, description, assignee, assign_comment, resolve_comment, close_comment " +
                        "from vehicle where opened_by = ? and description = ? and close_comment = ?",
                openedBy, description, closeComment);

        assertValue(row, "opened_by", openedBy);
        assertValue(row, "description", description);
        assertValue(row, "assignee", assignee);
        assertValue(row, "assign_comment", assignComment);
        assertValue(row, "resolve_comment", resolveComment);
        assertValue(row, "close_comment", closeComment);

    }

    private void assertValue(Map<String, Object> row, String key, String expected) {
        Object actual = row.get(key);
        String actualText = actual == null ? null : String.valueOf(actual);
        if (expected == null ? actual != null : !expected.equals(actualText)) {
            throw new AssertionError("Expected " + key + "=" + expected + " but found " + actualText);
        }
    }
}
