package io.microgenie.aws.s3;

import io.microgenie.application.blob.FileContent;
import io.microgenie.application.blob.FileContentStream;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.blob.FileStore;
import io.microgenie.application.blob.FileStoreFactory;

import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3Client;

/**
 * 
 * S3 {@link FileStore} factory, for interacting with Blob content on S3
 * primarily through {@link S3FileStore} and {@link S3Admin}
 * 
 * @author shawn
 */
public class S3BlobFactory extends FileStoreFactory{

	
	private final String defaultBucket;
	private FileStore fileStore;
	private AmazonS3Client s3; 

	
	/**
	 * Constructs an S3 File Store implementation of CommandFactory
	 * @param s3Client
	 * @param defaultBucket - default bucket
	 */
	public S3BlobFactory(final AmazonS3Client s3Client, final String defaultBucket){
		this.s3 = s3Client;
		this.defaultBucket = defaultBucket;
		this.fileStore = new S3FileStore(defaultBucket, s3);
	}
	

	@Override
	public FileContent read(FilePath path) throws IOException {
		return this.fileStore.read(path.getDrive(),path.getPath());
	}
	@Override
	public FileContentStream readStream(FilePath path) {
		return this.fileStore.readStream(path.getDrive(), path.getPath());
	}
	@Override
	public FilePath delete(FilePath path) {
		return this.fileStore.delete(path.getDrive(), path.getPath());
	}
	@Override
	public FilePath save(FileContent file) {
		return this.fileStore.save(file);
	}
	

	/***
	 * Get the default bucket
	 */
	@Override
	public String getDefaultDrive() {
		return this.defaultBucket;
	}
	
	
	
	/***
	 * Close Thread Group Factory
	 * @throws IOException 
	 */
	@Override
	public void close() throws IOException{
		this.fileStore.close();
	}
}
