package io.microgenie.application.commands;


import io.microgenie.application.blob.FileContent;
import io.microgenie.application.blob.FileContentStream;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.blob.FileStore;
import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.commands.BlobSpecs.SaveBlobInputSpec;
import io.microgenie.commands.concurrency.ExecutorRegistry;
import io.microgenie.commands.core.CommandFactory;
import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.FunctionCommands.Func2;
import io.microgenie.commands.core.GenieInputCommand;
import io.microgenie.commands.core.GenieRunnableCommand;
import io.microgenie.commands.core.Inputs.Input;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListeningExecutorService;



/**
 * File Store Command Factory
 * @author shawn
 */
public class FileStoreCommnandFactory extends CommandFactory {


	private static final Logger LOGGER = LoggerFactory.getLogger(FileStoreCommnandFactory.class);
	
	
	private final FileStoreFactory files;
	
	

	/**
	 * File Commands
	 * @param files
	 */
	public FileStoreCommnandFactory(final FileStoreFactory files){
		this.files = files;
	}
	public  SaveFileCommand save(final FileContent content){
		return new SaveFileCommand(this.files, content, ExecutorRegistry.INSTANCE.get(content.getPath().getDrive()));
	}
	public  ReadFileCommand read(final FilePath path){
		return new ReadFileCommand(this.files, path, ExecutorRegistry.INSTANCE.get(path.getDrive()));
	}
	public  DeleteFileCommand delete(final FilePath path){
		return new DeleteFileCommand(this.files, path, ExecutorRegistry.INSTANCE.get(path.getDrive()));
	}
	
	
	/**
	 * Create a file input command for saving a file based on input
	 * @param fileInputSpec
	 * @return the command to be executed
	 */
	public <I> SaveFileInputCommand<I> save(final SaveBlobInputSpec<I> fileInputSpec){
		final FilePath path = this.toFilePath(fileInputSpec.getDrive(), fileInputSpec.getPath());
		return new SaveFileInputCommand<I>(this.files, path, fileInputSpec.getFunction(), ExecutorRegistry.INSTANCE.get(path.getDrive()));
	}
	public <I> ReadFileInputCommand<I> read(final ToFilePathFunction<I> toPath){
		return new ReadFileInputCommand<I>(this.files, toPath, ExecutorRegistry.INSTANCE.get(null));
	}
	public <I> DeleteFileInputCommand<I> delete(final ToFilePathFunction<I> toPath){
		return new DeleteFileInputCommand<I>(this.files, toPath, ExecutorRegistry.INSTANCE.get(null));
	}
	
	
	public interface ToFileFunction<I> extends Func2<I, FilePath, FileContent>{}
	public interface ToFilePathFunction<I> extends Func1<I, FilePath>{}


	/**
	 * A Base file store command
	 * @author shawn
	 *
	 * @param <R>
	 */
	public static abstract class FileStoreCommand<R> extends GenieRunnableCommand<R> implements FileStore{
		private final FileStoreFactory files;
		private R defaultValue;
		protected FileStoreCommand(final FileStoreFactory files, final ListeningExecutorService executor) {
			this(files,FileStoreCommand.class.getName(),executor, null);
		}
		protected FileStoreCommand(final FileStoreFactory files, final String key, final ListeningExecutorService executor) {
			this(files, key, executor, null);
		}
		protected FileStoreCommand(final FileStoreFactory files, final String key, final ListeningExecutorService executor, final R defaultValue) {
			super(key, executor);
			this.files = files;
			this.defaultValue = defaultValue;
		}
		@Override
		public FilePath save(FileContent content) {
			return this.files.save(content);
		}
		@Override
		public FileContent read(String path) throws IOException {
			return this.files.read(FilePath.as(files.getDefaultDrive(), path));
		}
		@Override
		public FileContent read(String drive, String path) throws IOException {
			return this.files.read(FilePath.as(drive, path));
		}
		@Override
		public FileContentStream readStream(String path) {
			return this.files.readStream(FilePath.as(files.getDefaultDrive(), path));
		}
		@Override
		public FileContentStream readStream(String drive, String path) {
			return this.files.readStream(FilePath.as(drive, path));
		}
		@Override
		public FilePath delete(String path) {
			return this.files.delete(FilePath.as(files.getDefaultDrive(), path));
		}
		@Override
		public FilePath delete(String drive, String path) {
			return this.files.delete(FilePath.as(drive, path));
		}
		
		@Override
		protected void success(R result) {}
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected R fallback() {
			return this.defaultValue;
		}
	}
	
	
	
	public static class ReadFileCommand extends FileStoreCommand<FileContent>{
		private final FilePath path;
		protected ReadFileCommand(final FileStoreFactory files, final FilePath path, final ListeningExecutorService executor) {
			super(files, executor);
			this.path = path;
		}
		@Override
		public FileContent run() throws ExecutionException {
			try {
				return super.read(path.getDrive(), path.getPath());
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
	}
	public static class SaveFileCommand extends FileStoreCommand<FilePath>{
		private final FileContent content;
		protected SaveFileCommand(final FileStoreFactory files, final FileContent content, final ListeningExecutorService executor) {
			super(files, executor);
			this.content = content;
		}
		@Override
		public FilePath run() throws ExecutionException {
			try{
				return super.save(this.content);	
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
	}
	public static class DeleteFileCommand extends FileStoreCommand<FilePath>{
		private final FilePath path;
		protected DeleteFileCommand(final FileStoreFactory files, final FilePath path, final ListeningExecutorService executor) {
			super(files, executor);
			this.path = path;
		}
		@Override
		public FilePath run() throws ExecutionException {
			try{
				return super.delete(path.getDrive(), path.getPath());	
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
	}
	
	
	/**
	 * A base file store command that accepts chained input
	 * @author shawn
	 *
	 * @param <I> - The input type
	 * @param <O> - The output type
	 */
	public static abstract class FileStoreInputCommand<I,O> extends GenieInputCommand<I,O> implements FileStore{
		private final FileStoreFactory files;
		private O defaultValue;
		public FileStoreInputCommand(final FileStoreFactory files, final ListeningExecutorService executor) {
			this(files, files.getDefaultDrive(), executor, null);
		}
		public FileStoreInputCommand(final FileStoreFactory files, final String key, final ListeningExecutorService executor, final O defaultValue) {
			super(key, executor);
			this.files = files;
			this.defaultValue = defaultValue;
		}
		@Override
		public FilePath save(final FileContent content) {
			return this.files.save(content);
		}
		@Override
		public FileContent read(final String path) throws IOException {
			return this.files.read(FilePath.as(files.getDefaultDrive(), path));
		}
		@Override
		public FileContent read(final String drive, final String path) throws IOException {
			return this.files.read(FilePath.as(drive, path));
		}
		@Override
		public FileContentStream readStream(final String path) {
			return this.files.readStream(FilePath.as(files.getDefaultDrive(), path));
		}
		@Override
		public FileContentStream readStream(final String drive, final String path) {
			return this.files.readStream(FilePath.as(drive, path));
		}
		@Override
		public FilePath delete(final String path){
			return this.files.delete(FilePath.as(files.getDefaultDrive(), path));
		}
		@Override
		public FilePath delete(final String drive, final String path){
			return this.files.delete(FilePath.as(drive, path));
		}
		@Override
		protected void success(O result) {}
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected O fallback() {
			return this.defaultValue;
		}
	}
	
	
	
	
	/**
	 * SaveFileInputCommand<I> - where I is the input type to be received
	 * @author shawn
	 * @param <I> - The input type
	 */
	public class SaveFileInputCommand<I> extends FileStoreInputCommand<I, FilePath>{
		private final ToFileFunction<I> toFile;
		private final FilePath path;
		
		public SaveFileInputCommand(final FileStoreFactory files, final FilePath path, final ToFileFunction<I> toFile, final ListeningExecutorService executor) {
			super(files, SaveFileInputCommand.class.getName(), executor, null);
			this.toFile = toFile;
			this.path = path;
		}
		@Override
		protected FilePath run(final I input) {
			final FileContent content = this.toFile.run(Input.with(input, path));
			this.save(content);
			LOGGER.debug("saved file at path: {}", content.getPath().toString());
			return content.getPath();
		}
	}
	
	
	
	/**
	 * ReadFileInputCommand<I> where I type is being input to build the file path
	 * @author shawn
	 * @param <I> - The input type
	 */
	public static class ReadFileInputCommand<I> extends FileStoreInputCommand<I, FileContent> {
		
		private static final Logger LOGGER = LoggerFactory.getLogger(ReadFileInputCommand.class);
		private final ToFilePathFunction<I> toPath;
		public ReadFileInputCommand(final FileStoreFactory files, final ToFilePathFunction<I> toPath, final ListeningExecutorService executor) {
			super(files, executor);
			this.toPath = toPath;
		}
		@Override
		protected FileContent run(I input) {
			try {
				final FilePath path = toPath.run(Input.with(input));
				LOGGER.debug("reading file at path: {}", path.toString());
				return this.read(path.getDrive(), path.getPath());
			} catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
	
	
	public static class DeleteFileInputCommand<I> extends FileStoreInputCommand<I, FilePath> {
		
		private static final Logger LOGGER = LoggerFactory.getLogger(ReadFileInputCommand.class);
		private final ToFilePathFunction<I> toPath;
		public DeleteFileInputCommand(final FileStoreFactory files, final ToFilePathFunction<I> toPath, final ListeningExecutorService executor) {
			super(files, executor);
			this.toPath = toPath;
		}
		@Override
		protected FilePath run(I input) {
			try {
				final FilePath path = toPath.run(Input.with(input));
				LOGGER.debug("reading file at path: {}", path.toString());
				return this.delete(path.getDrive(), path.getPath());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
	
	
	
	
	
	
	/***
	 * @author shawn
	 *
	 * @param <I>
	 */
	public static abstract class ReadFileStreamInputCommand<I> extends FileStoreInputCommand<I, FileContentStream>{
		private static final Logger LOGGER = LoggerFactory.getLogger(ReadFileStreamInputCommand.class);
		public ReadFileStreamInputCommand(final FileStoreFactory files, final ListeningExecutorService executor) {
			super(files, executor);
		}
		
		protected abstract FilePath createPath(I input);
		
		@Override
		public FileContentStream run(I input) {
			final FilePath path = this.createPath(input);
			try{
				return this.readStream(path.getDrive(), path.getPath());	
			}catch(Exception ex){
				LOGGER.error(ex.getMessage(),ex);
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}
	
	
	/**
	 * Uses the default drive if drive is null or empty
	 * @param drive
	 * @param path
	 * @return filePath
	 */
	private FilePath toFilePath(final String drive, final String path){
		if(Strings.isNullOrEmpty(drive)){
			return FilePath.as(this.files.getDefaultDrive(), path);
		}else{
			return FilePath.as(drive, path);
		}
	}
	
	
	public String getDefaultDrive(){
		return this.files.getDefaultDrive();
	}
	
	
	@Override
	public void close() throws IOException {}
	@Override
	public void initialize() {}
}
