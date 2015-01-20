package io.microgenie.application.blob;

import java.io.Closeable;
import java.io.IOException;


/** 
 * @author shawn
 */
public interface FileStore extends Closeable {
	
	public FilePath save(FileContent content);
	
	public FileContent read(String path) throws IOException;
	public FileContent read(final String drive, final String path) throws IOException;
	
	public FileContentStream readStream(final String path);
	public FileContentStream readStream(final String drive, final String path);
	
	public FilePath delete(final String path);
	public FilePath delete(final String drive, final String path);
}
