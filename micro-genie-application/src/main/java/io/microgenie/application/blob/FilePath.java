package io.microgenie.application.blob;


/**
 * A File path, representing the location of {@link FileContent} at a {@link FileStore} endpoint
 * @author shawn
 */
public class FilePath {
	private final String drive;
	private final String path;
	protected FilePath(final String drive, final String path){
		this.drive = drive;
		this.path = path;
	}
	public String getDrive() {
		return drive;
	}
	public String getPath() {
		return path;
	}
	public static FilePath as(String drive, String path) {
		return new FilePath(drive,path);
	}
}
