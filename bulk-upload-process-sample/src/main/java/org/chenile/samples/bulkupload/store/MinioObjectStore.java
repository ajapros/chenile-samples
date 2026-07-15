package org.chenile.samples.bulkupload.store;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.InputStream;

public class MinioObjectStore implements ObjectStore {
	private final MinioClient minioClient;
	private final String bucket;

	public MinioObjectStore(MinioClient minioClient, String bucket) {
		this.minioClient = minioClient;
		this.bucket = bucket;
	}

	@Override
	public void put(String key, InputStream inputStream, long size, String contentType) throws Exception {
		minioClient.putObject(PutObjectArgs.builder()
				.bucket(bucket)
				.object(key)
				.stream(inputStream, size, -1)
				.contentType(contentType)
				.build());
	}

	@Override
	public InputStream get(String key) throws Exception {
		return minioClient.getObject(GetObjectArgs.builder()
				.bucket(bucket)
				.object(key)
				.build());
	}
}
