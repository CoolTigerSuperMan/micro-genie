package io.microgenie.aws.s3;

import io.microgenie.application.blob.FileContent;
import io.microgenie.application.blob.FileContentStream;
import io.microgenie.application.blob.FileMetadata;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.blob.FileStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * S3 Filestore implementation
 * @author shawn
 *
 */
public class S3FileStore implements FileStore{

	private static final Logger LOGGER = LoggerFactory.getLogger(S3FileStore.class);
	
	private AmazonS3Client client;
	private String bucket;

	
	public S3FileStore(final AmazonS3Client client){
		this.client = client;
	}
	
	public S3FileStore(final String drive, final AmazonS3Client s3Client){
		this.bucket = drive;
		this.client = s3Client;
	}
	

	/**
	 * Save File content to S3
	 */
	@Override
	public FilePath save(FileContent content) {
		
		Preconditions.checkNotNull(content, "FileContent is null, unable to save file");
		Preconditions.checkNotNull(content.getContent(), "the FileContent byte buffer cannot be null, unable to save file");
		
		final FilePath path = Preconditions.checkNotNull(content.getPath(), "FilePath is null, unable to save file"); 
		
		final ObjectMetadata metadata = this.createMetaData(content);
		client.putObject(content.getPath().getDrive(), path.getPath(), content.getContent(), metadata);
		
		LOGGER.debug("Successfully saved file at path: {}", path.toString());

		return path;
	}


	/**
	 * Read a file as {@link FileContent} from the given path. 
	 * <p>
	 * This method requires that the {@link S3FileStore} was constructed
	 * with a default bucket specified as the default drive
	 * 
	 * @param path - The path under the default mount point
	 * @return fileContent - @see {@link FileContent}
	 * @throws IOException
	 */
	@Override
	public FileContent read(final String path) throws IOException {
		LOGGER.debug("reading file at path: {}", path);
		return this.read(this.bucket, path);
	}
	
	
	/**
	 * Read the File contents as {@link FileContent} from the given bucket at the specified path
	 * 
	 * @param drive - Bucket
	 * @param path - The path / key under the given bucket
	 */
	@Override
	public FileContent read(final String drive, final String path) throws IOException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(drive), "drive parameter is required");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "path parameter is required");
		
		LOGGER.debug("Reading file at drive: {} - path: {}", drive, path);
		
		final FileContentStream stream = this.readStream(drive, path);
		final byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(stream.getContent(), stream.getContentLength());
		final ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(bytes);
		return FileContent.create(stream.getPath(), bytes.length, byteArrayStream, stream.getMetadata());
	}

	

	
	/***
	 * Read a file from the given path. This method requires that the {@link S3FileStore} was constructed
	 * with a default bucket specified as the default drive
	 * <p>
	 * <b>The caller is responsible for manipulating and closing the stream</b>
	 * 
	 * @param path - the path under the default mount point where the file is located
	 * @return fileContentStream
	 */
	public FileContentStream readStream(final String path) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path), String.format("S3FileStore was not initialized with a default bucket. Unable to read file from path: %s", path));
		LOGGER.debug("Reading file at drive: {} - path: {}", this.bucket, path);
		return this.readStream(this.bucket, path);
	}
	
	
	/***
	 * Read a file from the given path. This method requires that the {@link S3FileStore} was constructed
	 * with a default bucket specified as the default drive
	 * <p>
	 * <b>The caller is responsible for manipulating and closing the stream</b>
	 * 
	 * @param drive - The S3 bucket
	 * @param path - the path under the default mount point where the file is located
	 * @return fileContentStream
	 */
	@Override
	public FileContentStream readStream(final String drive, final String path) {
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(drive), "drive parameter is required");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "path parameter is required");
		
		LOGGER.debug("Reading file at drive: {} - path: {}", drive, path);
		
		final S3Object s3Object = client.getObject(drive, path);
		final ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
		final FileMetadata metadata = this.createFileMetaData(objectMetadata);
		
		return FileContentStream.create(drive, path, 
				objectMetadata.getContentLength(), 
				s3Object.getObjectContent(),metadata);	
	}
	
	
	
	/***
	 * Deletes the file at the given path using the default bucket location 
	 */
	@Override
	public FilePath delete(String path) {
		LOGGER.debug("deleting file at drive: {} - path: {}", this.bucket, path);
		return this.delete(this.bucket, path);
	}
	
	
	
	/***
	 * Deletes the specified file and returns the FilePath 
	 */
	@Override
	public FilePath delete(final String drive, final String path) {
		final DeleteObjectRequest request = new DeleteObjectRequest(drive, path);
		LOGGER.debug("deleting file at drive: {} - path: {}", drive, path);
		this.client.deleteObject(request);
		return FilePath.as(drive, path);
	}
	
	
	
	/***
	 * Convert {@link FileMetadata} from {@link FileContent#getMetadata()} to an
	 * S3 {@link ObjectMetadata} instance
	 * @param content - {@link FileContent}
	 * @return metaData - {@link ObjectMetadata}
	 */
	private ObjectMetadata createMetaData(final FileContent content){
		
		/** Content Length is always required **/
		final ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(content.getContentLength());
		
		/** set optional metadata values **/
		if(content.getMetadata()!=null){
			final FileMetadata contentMetadata = content.getMetadata();	
			metadata.setContentType(contentMetadata.getContentType());
			metadata.setContentEncoding(contentMetadata.getEncoding());	
			for(Entry<String, String> metaEntry : contentMetadata.getUserAttributes().entrySet()){
				metadata.addUserMetadata(metaEntry.getKey(), metaEntry.getValue());	
			}
		}
		return metadata;
	}
	
	
	/***
	 * Convert {@link FileMetadata} from {@link FileContent#getMetadata()} to an
	 * S3 {@link ObjectMetadata} instance
	 * @param content - {@link FileContent}
	 * @return metaData - {@link ObjectMetadata}
	 */
	private FileMetadata  createFileMetaData(final ObjectMetadata metadata){
		if(metadata==null){
			return null;
		}
		/** Content Length is always required **/
		final FileMetadata fileMetadata = 
				FileMetadata.create(
						metadata.getContentType(),
						metadata.getContentEncoding(),
						metadata.getUserMetadata());
		
		return fileMetadata;
	}
	
	
	@Override
	public void close() throws IOException {
		LOGGER.debug("shutting down S3FileStore");
	}
}
