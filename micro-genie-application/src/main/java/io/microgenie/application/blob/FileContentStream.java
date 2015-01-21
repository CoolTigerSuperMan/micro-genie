package io.microgenie.application.blob;

import java.io.InputStream;

/**
 * Use FileContentStream for reading large files instead
 * of {@link FileContent}
 * <p>
 * FileContentStream is similar to {@link FileContent}
 * except that instead of having all contents  buffered in memory
 * An input stream is contained that can be read in chunks.
 * 
 * @author shawn
 */
public class FileContentStream{
	
	private final FilePath path;
	private final long contentLength;
	private final FileMetadata metadata;
	private final InputStream content;
	
	
	/**
	 * Used to return an open {@link InputStream} for a file
	 * 
	 * @param path - The path to the file on the given drive / mount point
	 * @param contentLength - The content length of the contained {@link InputStream}
	 * @param content - The actual file contents available as an {@link InputStream}
	 * @param metadata - @see {@link FileMetadata}
	 */
	protected FileContentStream(FilePath path, final long contentLength, InputStream content, FileMetadata metadata){
		this.path = path;
		this.contentLength = contentLength;
		this.content = content;
		this.metadata = metadata;
	}
	
	public FilePath getPath() {
		return path;
	}
	public long getContentLength() {
		return contentLength;
	}
	public InputStream getContent() {
		return content;
	}
	public FileMetadata getMetadata() {
		return metadata;
	}
	
	
	
	
	
	
	
	
	
	
	/***
	 * A representation of {@link FileContent} except that the contents are available as an {@link InputStream}
	 * instead of a buffered byte array
	 * 
	 * @param drive - The mounted drive that the path and file reside on
	 * @param path - {@link FilePath}
	 * @param contentLength
	 * @param content
	 * @return fileContentStream
	 */
	public static FileContentStream create(final String drive, final String path, final long contentLength, InputStream content){
		return FileContentStream.create(FilePath.as(drive, path), contentLength, content);
	}
	
	

	/***
	 * A representation of {@link FileContent} except that the contents are available as an {@link InputStream}
	 * instead of a buffered byte array
	 * 
	 * @param path - {@link FilePath}
	 * @param contentLength
	 * @param content - {@link InputStream}
	 * @return fileContentStream
	 */
	public static FileContentStream create(final FilePath path, final long contentLength, InputStream content){
		return new FileContentStream(path, contentLength, content, null);
	}
	
	
	
	/***
	 * A representation of {@link FileContent} except that the contents are available as an {@link InputStream}
	 * instead of a buffered byte array
	 * 
	 * @param path - {@link FilePath}
	 * @param contentLength
	 * @param content - {@link InputStream}
	 * @param metadata - {@link FileMetadata}
	 * @return fileContentStream
	 */
	public static FileContentStream create(final FilePath path, final long contentLength, InputStream content, FileMetadata metadata){
		return new FileContentStream(path, contentLength, content, metadata);
	}
	
	
	/**
	 * 
	 * @param drive
	 * @param path
	 * @param contentLength
	 * @param content
	 * @param metadata
	 * @return fileContentStream
	 */
	public static FileContentStream create(final String drive, final String path, final long contentLength, InputStream content, FileMetadata metadata){
		return new FileContentStream(FilePath.as(drive, path), contentLength, content, metadata);
	}
}

