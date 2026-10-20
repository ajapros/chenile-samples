package com.mycompany.myorg.p1.bdd;

@org.junit.runner.RunWith(io.cucumber.junit.Cucumber.class)
@io.cucumber.junit.CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.mycompany.myorg.p1.bdd", "org.chenile.cucumber.rest"},
        plugin = "pretty")
public class CukesRestTest extends org.chenile.mqtt.test.KafkaBaseTest {
    public static String bootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }
}
