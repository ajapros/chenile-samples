package org.chenile.samples.bulkupload.store;

import java.io.InputStream;

public interface ObjectStore {
	void put(String key, InputStream inputStream, long size, String contentType) throws Exception;
	InputStream get(String key) throws Exception;
}
