package org.chenile.samples.scheduler;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import org.chenile.scheduler.model.ScheduledExecutionRequest;
import org.chenile.scheduler.model.SchedulerInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class KubernetesJobSpecFactory {
	private KubernetesJobSpecFactory() {
	}

	static Job buildJob(ScheduledExecutionRequest request, String kubernetesJobName,
			String workerImage, String serviceAccount) {
		SchedulerInfo info = request.getSchedulerInfo();
		Map<String,String> labels = labels(request, info);
		return new JobBuilder()
				.withNewMetadata()
					.withName(kubernetesJobName)
					.withLabels(labels)
					.withAnnotations(info.getJobAnnotations())
				.endMetadata()
				.withNewSpec()
					.withBackoffLimit(info.getRetryCount() == null ? 0 : info.getRetryCount())
					.withActiveDeadlineSeconds(info.getTimeoutSeconds() == null ? null : (long)info.getTimeoutSeconds())
					.withNewTemplate()
						.withNewMetadata()
							.withLabels(labels)
						.endMetadata()
						.withNewSpec()
							.withServiceAccountName(serviceAccount)
							.withRestartPolicy("Never")
							.addNewContainer()
								.withName("worker")
								.withImage(workerImage)
								.withEnv(env(request))
							.endContainer()
						.endSpec()
					.endTemplate()
				.endSpec()
				.build();
	}

	private static Map<String,String> labels(ScheduledExecutionRequest request, SchedulerInfo info) {
		Map<String,String> labels = new LinkedHashMap<>();
		labels.put("app.kubernetes.io/managed-by", "chenile-scheduler");
		labels.put("chenile.scheduler/job-name", info.getJobName());
		labels.put("chenile.scheduler/execution-id", request.getExecutionId());
		if (info.getJobLabels() != null) {
			labels.putAll(info.getJobLabels());
		}
		return labels;
	}

	private static List<EnvVar> env(ScheduledExecutionRequest request) {
		SchedulerInfo info = request.getSchedulerInfo();
		return List.of(
				env("CHENILE_SCHEDULER_EXECUTION_ID", request.getExecutionId()),
				env("CHENILE_SCHEDULER_JOB_NAME", info.getJobName()),
				env("CHENILE_SCHEDULER_WORKER", nullToEmpty(info.getWorker())),
				env("CHENILE_SCHEDULER_PAYLOAD", nullToEmpty(info.payload)),
				env("CHENILE_SCHEDULER_SERVICE_NAME", nullToEmpty(info.serviceName)),
				env("CHENILE_SCHEDULER_OPERATION_NAME", nullToEmpty(info.operationName)));
	}

	private static EnvVar env(String name, String value) {
		return new EnvVarBuilder().withName(name).withValue(value).build();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
