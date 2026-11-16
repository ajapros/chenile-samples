package org.chenile.samples.p1.bdd;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.chenile.mqtt.test.KafkaBaseTest;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"org.chenile.samples.p1.bdd", "org.chenile.cucumber.rest"},
        plugin = "pretty")
public class CukesRestTest extends KafkaBaseTest {
    public static String bootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }
}
