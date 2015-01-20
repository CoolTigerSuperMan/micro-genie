package io.microgenie.aws.s3;

import io.microgenie.application.blob.FileContent;
import io.microgenie.application.blob.FileContentStream;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.blob.FileStore;
import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.aws.S3Config;

import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

/**
 * @author shawn
 */
public class S3BlobFactory extends FileStoreFactory{

	
	private final String defaultBucket;
	private final S3Config config;
	
	private FileStore fileStore;
	private S3Admin admin;
	
	

	
	
	/**
	 * Constructs an S3 File Store implementation of CommandFactory
	 * @param defaultBucket - default bucket to user if not specified when interacting with s3
	 */
	public S3BlobFactory(final S3Config config){
		this.config = config;
		this.defaultBucket = config.getDefaultDrive();
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
	
	@Override
	public void initialize() {
		final AmazonS3Client s3 = new AmazonS3Client();
		this.fileStore = new S3FileStore(defaultBucket, s3);
		this.admin = new S3Admin(s3);
		this.createBuckets();
	} 
	
	
	/***
	 * Close Thread Group Factory
	 * @throws IOException 
	 */
	@Override
	public void close() throws IOException{
		this.fileStore.close();
	}

	
	/***
	 * Create any bucket configurations
	 */
	private void createBuckets(){
		this.admin.createBucket(this.defaultBucket, CannedAccessControlList.BucketOwnerFullControl);
		for(String bucket : this.config.getBuckets()){
			this.admin.createBucket(bucket, CannedAccessControlList.BucketOwnerFullControl);	
		}
	}
}
