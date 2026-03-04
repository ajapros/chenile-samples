package com.mycompany.myorg.vehicle.packager.bdd;

import com.mycompany.myorg.vehicle.packager.PackagerSpringTestConfig;
import com.mycompany.myorg.vehicle.packager.pubsub.PubSubSharedData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = PackagerSpringTestConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
public class CukesSteps {
    private static final String TENANT0_DB_URL = "jdbc:h2:mem:packager-tenant0";
    private static final String TENANT1_DB_URL = "jdbc:h2:mem:packager-tenant1";

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

    @Then("datasource {string} contains tenant0 extension code {string}")
    public void datasourceContainsTenant0ExtensionCode(String datasource, String code) throws Exception {
        int count = countRows(datasource,
                "select count(*) from vehicle where ext_type = 'tenant0_ext' and tenant0_code = ?",
                code);
        if (count < 1) {
            throw new AssertionError("Expected tenant0 row in datasource " + datasource + " for code=" + code);
        }
    }

    @Then("datasource {string} does not contain tenant0 extension code {string}")
    public void datasourceDoesNotContainTenant0ExtensionCode(String datasource, String code) throws Exception {
        int count = countRows(datasource,
                "select count(*) from vehicle where ext_type = 'tenant0_ext' and tenant0_code = ?",
                code);
        if (count != 0) {
            throw new AssertionError("Expected no tenant0 row in datasource " + datasource + " for code=" + code + " but found " + count);
        }
    }

    @Then("datasource {string} contains tenant1 extension code {string}")
    public void datasourceContainsTenant1ExtensionCode(String datasource, String code) throws Exception {
        int count = countRows(datasource,
                "select count(*) from vehicle where ext_type = 'tenant1_ext' and tenant1_code = ?",
                code);
        if (count < 1) {
            throw new AssertionError("Expected tenant1 row in datasource " + datasource + " for code=" + code);
        }
    }

    @Then("datasource {string} does not contain tenant1 extension code {string}")
    public void datasourceDoesNotContainTenant1ExtensionCode(String datasource, String code) throws Exception {
        int count = countRows(datasource,
                "select count(*) from vehicle where ext_type = 'tenant1_ext' and tenant1_code = ?",
                code);
        if (count != 0) {
            throw new AssertionError("Expected no tenant1 row in datasource " + datasource + " for code=" + code + " but found " + count);
        }
    }

    private int countRows(String datasource, String query, String code) throws Exception {
        String jdbcUrl = switch (datasource) {
            case "tenant0" -> TENANT0_DB_URL;
            case "tenant1" -> TENANT1_DB_URL;
            default -> throw new IllegalArgumentException("Unknown datasource: " + datasource);
        };

        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, code);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt(1);
            }
        }
    }
}
