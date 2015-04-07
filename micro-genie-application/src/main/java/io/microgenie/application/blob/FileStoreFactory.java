package io.microgenie.application.blob;


import java.io.Closeable;
import java.io.IOException;



/**
 * File Store Command Factory
 * @author shawn
 */
public abstract class FileStoreFactory implements Closeable {

	public FileStoreFactory(){}
	public abstract FileContent read(final FilePath path) throws IOException;
	public abstract FileContentStream readStream(final FilePath path);
	public abstract FilePath delete(final FilePath path);
	public abstract FilePath save(final FileContent file); 
	public abstract String getDefaultDrive();
}
