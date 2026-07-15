package org.chenile.samples.bulkupload.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.chenile.core.context.ContextContainer;
import org.chenile.orchestrator.delegate.ProcessManagerClient;
import org.chenile.orchestrator.process.api.ProcessManager;
import org.chenile.orchestrator.process.config.reader.ProcessConfigurator;
import org.chenile.samples.bulkupload.store.FileSystemObjectStore;
import org.chenile.samples.bulkupload.store.MinioObjectStore;
import org.chenile.samples.bulkupload.store.ObjectStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class BulkUploadConfiguration {
	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	ContextContainer contextContainer() {
		return ContextContainer.CONTEXT_CONTAINER;
	}

	@Bean
	@ConditionalOnMissingBean
	ObjectStore objectStore(
			@Value("${bulk-upload.object-store.type:filesystem}") String type,
			@Value("${bulk-upload.object-store.root:${java.io.tmpdir}/chenile-bulk-upload}") String root,
			@Value("${bulk-upload.object-store.endpoint:}") String endpoint,
			@Value("${bulk-upload.object-store.access-key:minioadmin}") String accessKey,
			@Value("${bulk-upload.object-store.secret-key:minioadmin}") String secretKey,
			@Value("${bulk-upload.object-store.bucket:chenile-bulk-upload}") String bucket) throws Exception {
		if ("minio".equalsIgnoreCase(type)) {
			MinioClient client = MinioClient.builder()
					.endpoint(endpoint)
					.credentials(accessKey, secretKey)
					.build();
			if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
				client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
			}
			return new MinioObjectStore(client, bucket);
		}
		return new FileSystemObjectStore(Path.of(root));
	}

	@Bean
	ProcessManagerClient processManagerClient(@Qualifier("_processStateEntityService_") ProcessManager processManager) {
		return new LocalProcessManagerClient(processManager);
	}

	@Bean
	String bulkUploadProcessDefinitions(ProcessConfigurator processConfigurator) {
		processConfigurator.read("bulk-upload-process-def.json");
		return "bulk-upload-process-definitions";
	}
}
