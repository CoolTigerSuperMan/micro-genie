package io.microgenie.application.blob;

import java.io.ByteArrayInputStream;



/**
 * Content
 * @author shawn
 */
public class FileContent {

	private final FilePath path;
	private final long contentLength;
	private final ByteArrayInputStream content;
	
	private FileMetadata metadata;
	
	protected FileContent(FilePath path, long contentLength, ByteArrayInputStream content){
		this(path, contentLength, content, null);
	}
	protected FileContent(FilePath path, long contentLength, ByteArrayInputStream content, FileMetadata metadata){
		this.path = path;
		this.contentLength = contentLength;
		this.content = content;
		this.metadata = metadata;
	}
	
	public ByteArrayInputStream getContent() {
		return content;
	}
	public FilePath getPath() {
		return path;
	}
	public FileMetadata getMetadata() {
		return metadata;
	}
	public long getContentLength() {
		return contentLength;
	}
	public FileContent withMetadata(FileMetadata metadata) {
		this.metadata = metadata;
		return this;
	}
	
	
	

	
	
	/***
	 * Represents the contents of a file and metadata attributes of the file  
	 * 
	 * @param drive - Drive mount the resides on
	 * @param path - The path on the mount point
	 * @param contentLength - ContentLength, should represent the length of the content byte array
	 * @param content - The ByteArray length
	 * @return fileContent - {@link FileContent}
	 */
	public static FileContent create(final String drive, final String path, long contentLength, ByteArrayInputStream content){
		return FileContent.create(FilePath.as(drive, path), contentLength, content);
	}
	
	
	/***
	 * Represents the contents of a file and metadata attributes of the file  
	 * 
	 * @param path - The path to the file {@link FilePath}
	 * @param contentLength - ContentLength, should represent the length of the content byte array
	 * @param content - The ByteArray length
	 * @return fileContent - {@link FileContent}
	 */
	public static FileContent create(FilePath path, long contentLength, ByteArrayInputStream content){
		return FileContent.create(path, contentLength, content, null);
	}
	
	
	/***
	 * 
	 * Represents the contents of a file and metadata attributes of the file  
	 * 
	 * @param drive - Drive mount the resides on
	 * @param path - The path on the mount point
	 * @param contentLength - Length of the content byte array
	 * @param content - The actual content as a {@link ByteArrayInputStream}
	 * @param metadata - File metadata
	 * @return fileContent
	 */
	public static FileContent create(final String drive, final String path, long contentLength, ByteArrayInputStream content, FileMetadata metadata){
		return FileContent.create(FilePath.as(drive, path), contentLength, content,  metadata);
	}
	
	
	/**
	 * Represents the contents of a file and metadata attributes of the file  
	 * 
	 * @param path - The path to the file {@link FilePath}
	 * @param contentLength - ContentLength, should represent the length of the content byte array
	 * @param content - The ByteArray length
	 * @param metadata - {@link FileMetadata} File meta data or file attributes
	 * @return fileContent - {@link FileContent}
	 */
	public static FileContent create(FilePath path, long contentLength, ByteArrayInputStream content, FileMetadata metadata){
		return new FileContent(path, contentLength, content, metadata);
	}
}
