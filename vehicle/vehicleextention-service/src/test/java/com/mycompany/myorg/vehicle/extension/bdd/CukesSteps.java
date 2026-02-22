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

    @Then("DB has vehicle extension row with policy {string} fitness {string} and type {string}")
    public void dbHasVehicleExtensionRow(String policy, String fitness, String type) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from vehicle where insurance_policy_number = ? and fitness_expiry = ? and vehicle_type = ?",
                Integer.class,
                policy,
                fitness,
                type
        );
        if (count == null || count < 1) {
            throw new AssertionError("No matching row found in vehicle table for extension payload");
        }
    }
}
