package io.microgenie.application.blob;


import java.io.Closeable;
import java.io.IOException;



/**
 * File Store Command Factory
 * @author shawn
 */
public abstract class FileStoreFactory implements Closeable {

	public FileStoreFactory(){}
	
	public abstract FileContent read(FilePath path) throws IOException;
	public abstract FileContentStream readStream(FilePath path);
	public abstract FilePath delete(FilePath path);
	public abstract FilePath save(FileContent file); 
	
	public abstract String getDefaultDrive();
	
	//public abstract void initialize();
}
