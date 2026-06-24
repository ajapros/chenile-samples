package org.chenile.samples.scheduler;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import org.chenile.scheduler.model.ScheduledExecutionRequest;
import org.chenile.scheduler.model.SchedulerInfo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KubernetesJobSpecFactoryTest {
	@Test
	void mapsSchedulerRequestToKubernetesJob() {
		Job job = KubernetesJobSpecFactory.buildJob(request(), "kubernetes-report-abc123",
				"example/scheduled-report-worker:1.0.0", "chenile-scheduler");
		Map<String,String> env = job.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()
				.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));

		assertEquals("kubernetes-report-abc123", job.getMetadata().getName());
		assertEquals("example/scheduled-report-worker:1.0.0",
				job.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
		assertEquals("chenile-scheduler", job.getSpec().getTemplate().getSpec().getServiceAccountName());
		assertEquals("kubernetes-report", env.get("CHENILE_SCHEDULER_JOB_NAME"));
		assertEquals("scheduledReportService", env.get("CHENILE_SCHEDULER_SERVICE_NAME"));
		assertEquals("generate", env.get("CHENILE_SCHEDULER_OPERATION_NAME"));
	}

	private ScheduledExecutionRequest request() {
		SchedulerInfo info = new SchedulerInfo();
		info.serviceName = "scheduledReportService";
		info.operationName = "generate";
		info.setJobName("kubernetes-report");
		info.setLauncher("kubernetes");
		info.setWorker("scheduled-report-worker");
		info.payload = "{ \"reportName\": \"daily-inventory\" }";
		info.setRetryCount(2);
		info.setTimeoutSeconds(120);
		info.setJobLabels(Map.of("app.kubernetes.io/part-of", "scheduler-sample"));

		ScheduledExecutionRequest request = new ScheduledExecutionRequest();
		request.setSchedulerInfo(info);
		request.setExecutionId("kubernetes-report-1000");
		request.setScheduledFireTime(Instant.ofEpochMilli(1000));
		request.setActualFireTime(Instant.ofEpochMilli(1001));
		request.setAttempt(1);
		return request;
	}
}
