package org.chenile.samples.scheduler.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.chenile.samples.scheduler.SchedulerSampleApplication;
import org.chenile.samples.scheduler.work.JdbcScheduledWorkItemRepository;
import org.chenile.samples.scheduler.work.ScheduledWorkItemWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** The test schedule fires every second, so the scenarios do not wait for the real 30-second cron. */
@CucumberContextConfiguration
@SpringBootTest(classes = SchedulerSampleApplication.class, properties =
		"chenile.scheduler.json.package=classpath*:org/chenile/samples/scheduler/test-schedules/*.json")
public class SchedulerSampleSteps {
	@Autowired JdbcScheduledWorkItemRepository repository;
	@Autowired JdbcTemplate jdbc;

	@Before
	public void clearWorkItems() {
		jdbc.update("delete from chenile_scheduler_work_item");
	}

	@Then("within {int} seconds a work item with key {string} is pending")
	public void pending(int seconds, String key) throws InterruptedException {
		long deadline = System.currentTimeMillis() + seconds * 1000L;
		while (status(key) == null && System.currentTimeMillis() < deadline) Thread.sleep(100);
		assertEquals("PENDING", status(key));
	}

	@Then("after {int} more seconds there is still {int} pending work item(s)")
	public void stillPending(int seconds, int count) throws InterruptedException {
		Thread.sleep(seconds * 1000L);
		assertEquals(count, repository.backlogCount());
	}

	@When("a worker processes one work item")
	public void process() {
		assertTrue(new ScheduledWorkItemWorker(repository, "test-worker", 120, true, 0).processOne());
	}

	@Then("the work item with key {string} has status {string}")
	public void status(String key, String status) {
		assertEquals(status, status(key));
	}

	private String status(String key) {
		return jdbc.query("select status from chenile_scheduler_work_item where idempotency_key = ?",
				rs -> rs.next() ? rs.getString(1) : null, key);
	}
}
