package org.chenile.samples.scheduler.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/resources/features",
		glue = "org.chenile.samples.scheduler.bdd",
		plugin = "pretty")
public class CukesSchedulerSampleTest {}
