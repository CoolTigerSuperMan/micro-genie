package io.microgenie.application.commands;

import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.blob.FileContent;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.commands.BlobSpecs.DefaultFileInputSpec;
import io.microgenie.application.commands.BlobSpecs.SaveBlobInputSpec;
import io.microgenie.application.commands.EventCommandFactory.PublishCommand;
import io.microgenie.application.commands.EventCommandFactory.PublishInputCommand;
import io.microgenie.application.commands.EventCommandFactory.ToEventFunction;
import io.microgenie.application.commands.FileStoreCommnandFactory.DeleteFileCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.DeleteFileInputCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.ReadFileCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.ReadFileInputCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.SaveFileCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.SaveFileInputCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.ToFileFunction;
import io.microgenie.application.commands.FileStoreCommnandFactory.ToFilePathFunction;
import io.microgenie.application.commands.HttpCommandFactory.HttpDeleteCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpDeleteInputCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpGetCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpGetInputCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpPostCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpPostInputCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpPutCommand;
import io.microgenie.application.commands.HttpCommandFactory.HttpPutInputCommand;
import io.microgenie.application.commands.HttpCommandFactory.ToEntityRequestFunction;
import io.microgenie.application.commands.QueueCommandFactory.ToMessageFunction;
import io.microgenie.application.events.Event;
import io.microgenie.application.queue.ProduceInputCommand;
import io.microgenie.application.queue.QueueSpecs.DefaultQueueInputSpec;
import io.microgenie.commands.concurrency.ExecutorRegistry;
import io.microgenie.commands.core.CommandFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


/****
 * Application Asynchronous and chained commands
 * @author shawn
 *
 */
public class ApplicationCommandFactory extends CommandFactory {

	private final FileStoreCommnandFactory files;
	private final HttpCommandFactory http;
	private final QueueCommandFactory queues;
	private final EventCommandFactory events;
	

	public ApplicationCommandFactory(final ApplicationFactory app) {
		this.files = new FileStoreCommnandFactory(app.blobs());
		this.http = new HttpCommandFactory(app.http());
		this.queues = new QueueCommandFactory(app.queues());
		this.events = new EventCommandFactory(app.events());
	}
	
	
	/** File Commands **/
	public SaveFileCommand saveFile(final FileContent content){
		return this.files.save(content);
	}
	public ReadFileCommand readFile(final FilePath path){
		return this.files.read(path);
	}
	public DeleteFileCommand deleteFile(final String path){
		return this.files.delete(FilePath.as(files.getDefaultDrive(), path));
	}
	public DeleteFileCommand deleteFile(final String drive, final String path){
		return this.files.delete(FilePath.as(drive, path));
	}
	public DeleteFileCommand deleteFile(final FilePath path){
		return this.files.delete(path);
	}	
	public <I> SaveFileInputCommand<I> saveFile(final FilePath path, final ToFileFunction<I> toFile){
		final DefaultFileInputSpec<I> defaultSpec = new DefaultFileInputSpec<I>(path.getDrive(), toFile);
		final SaveBlobInputSpec<I> saveSpec = new SaveBlobInputSpec<I>(defaultSpec, path.getPath());		
		return this.files.save(saveSpec);
	}
	public <I> SaveFileInputCommand<I> saveFile(final SaveBlobInputSpec<I> spec){
		return this.files.save(spec);
	}
	public <I> ReadFileInputCommand<I> readFile(final ToFilePathFunction<I> toPath){
		return this.files.read(toPath);
	}
	public <I> DeleteFileInputCommand<I> deleteFile(final ToFilePathFunction<I> toPath){
		return this.files.delete(toPath);
	}
	
	/** Http Commands **/
	public HttpGetCommand get(final String url) throws ExecutionException{
		try{
			return this.http.get(new URL(url));	
		}catch(Exception ex){
			throw new ExecutionException(ex.getMessage(),ex);
		}
	}
	public HttpGetCommand get(final URL url){
		return this.http.get(url);
	}
	public HttpPutCommand put(final URL url, Object entity){
		return this.http.put(url, entity);
	}
	public HttpPutCommand put(final String url, Object entity) throws MalformedURLException{
		return this.http.put(new URL(url), entity);
	}
	public HttpPostCommand post(final URL url, Object entity){
		return this.http.post(url, entity);
	}
	public HttpPostCommand post(final String url, Object entity) throws MalformedURLException{
		return this.http.post(new URL(url), entity);
	}
	public HttpDeleteCommand delete(final URL url){
		return this.http.delete(url);
	}
	public HttpDeleteCommand delete(final String url) throws MalformedURLException{
		return this.http.delete(new URL(url));
	}
	
	
	/** HTTP Input Commands **/
	public <I> HttpGetInputCommand<I> get(final ToEntityRequestFunction<I> toEntityRequest){
		return this.http.getInput(toEntityRequest);
	}
	public <I> HttpPutInputCommand<I> put(final ToEntityRequestFunction<I> toEntityRequest){
		return this.http.putInput(toEntityRequest);
	}
	public <I> HttpPostInputCommand<I> post(final ToEntityRequestFunction<I> toEntityRequest){
		return this.http.postInput(toEntityRequest);
	}
	public <I> HttpDeleteInputCommand<I> delete(final ToEntityRequestFunction<I> toEntityRequest){
		return this.http.deleteInput(toEntityRequest);
	}
	
	
	/** Queue Commands **/
	public <I> ProduceInputCommand<I> produce(final String queue, final ToMessageFunction<I> toMessageFunction){
		return this.queues.produce(queue, toMessageFunction);
	}
	public <I> ProduceInputCommand<I> produce(final DefaultQueueInputSpec<I> spec){
		return this.queues.produce(spec);
	}
	
	/** Event Commands **/
	public PublishCommand publish(final Event event){
		return this.events.submit(event);
	}
	public <I> PublishInputCommand<I> publish(final String topic, final ToEventFunction<I> toEventFunction){
		return this.events.submit(topic, toEventFunction);
	}
	@Override
	public void initialize() {}
	
	@Override
	public void close() throws IOException{
		ExecutorRegistry.INSTANCE.shutdown();
	}
}
