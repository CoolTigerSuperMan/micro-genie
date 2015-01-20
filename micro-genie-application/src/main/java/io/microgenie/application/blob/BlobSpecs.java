package io.microgenie.application.blob;

import io.microgenie.application.commands.FileStoreCommnandFactory.ToFileFunction;


/***
 * Specifications used for Blob commands 
 * @author shawn
 *
 */
public class BlobSpecs {

	public static class DefaultFileInputSpec<I>{
		private final String drive;
		private final ToFileFunction<I> toFileFunction;
		public String getDrive(){
			return this.drive;
		}
		public ToFileFunction<I> getToFileFunction(){
			return this.toFileFunction;
		}
		/**
		 * A default specification used to create specifications when running commands
		 * @param drive
		 * @param toFileFunction
		 */
		public DefaultFileInputSpec(final String drive, final ToFileFunction<I> toFileFunction) {
			this.drive = drive;
			this.toFileFunction = toFileFunction;
		}
		/**
		 * Create a SaveFileInput Specification each time a command is run, where the path should be different
		 * @param path
		 * @return saveFileInputSpec
		 */
		public SaveBlobInputSpec<I> path(final String path){
			return new SaveBlobInputSpec<I>(this, path);
		}
	}
	
	public static class SaveBlobInputSpec<I>{
		private String path;
		private DefaultFileInputSpec<I> defaultSpec;
		public SaveBlobInputSpec(final DefaultFileInputSpec<I> defaultSpec, final String path){
			this.defaultSpec = defaultSpec;
			this.path = path;
		}
		public String getDrive(){
			return this.defaultSpec.getDrive();
		}
		public String getPath(){
			return this.path;
		}public ToFileFunction<I> getFunction(){
			return this.defaultSpec.getToFileFunction();
		}
	}
}
