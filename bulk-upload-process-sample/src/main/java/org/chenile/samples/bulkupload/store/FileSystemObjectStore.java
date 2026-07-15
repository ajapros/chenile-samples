package org.chenile.samples.bulkupload.store;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemObjectStore implements ObjectStore {
	private final Path root;

	public FileSystemObjectStore(Path root) throws Exception {
		this.root = root;
		Files.createDirectories(root);
	}

	@Override
	public void put(String key, InputStream inputStream, long size, String contentType) throws Exception {
		Path destination = resolve(key);
		Files.createDirectories(destination.getParent());
		Files.copy(inputStream, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public InputStream get(String key) throws Exception {
		return Files.newInputStream(resolve(key));
	}

	private Path resolve(String key) {
		Path resolved = root.resolve(key).normalize();
		if (!resolved.startsWith(root)) {
			throw new IllegalArgumentException("Invalid object key " + key);
		}
		return resolved;
	}
}
