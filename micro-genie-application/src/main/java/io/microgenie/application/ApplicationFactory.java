package io.microgenie.application;

import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.commands.ApplicationCommandFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.http.HttpFactory;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.commands.util.CloseableUtils;

import java.io.Closeable;
import java.io.IOException;



/***
 * Application Command Factory, containing application specific commands
 * @author shawn
 *
 */
public abstract class ApplicationFactory implements Closeable{

	public ApplicationFactory(){}
	
	/** Register application command factories **/
	public abstract void registerFiles(final FileStoreFactory files);
	public abstract void registerQueues(final QueueFactory queues);
	public abstract void registerEvents(final EventFactory events);
	public abstract void registerDatabase(final DatabaseFactory database);
	public abstract void registerHttp(final HttpFactory<String> http);
	public abstract void registerCommands(final ApplicationCommandFactory commands);
	
	
	/** Access to application command factories **/
	public abstract  HttpFactory<String> http();
	public abstract  EventFactory events();
	public abstract  QueueFactory queues();
	public abstract  FileStoreFactory blobs();
	public abstract  DatabaseFactory database();
	public abstract  ApplicationCommandFactory commands();
	

	/** Initialize application command factories **/
	public abstract void initialize();

	
	/**
	 * Close all resources safely
	 */
	@Override
	public void close() throws IOException {
		CloseableUtils.closeQuietly(this.http());
		CloseableUtils.closeQuietly(this.events());
		CloseableUtils.closeQuietly(this.blobs());
		CloseableUtils.closeQuietly(this.http());
		CloseableUtils.closeQuietly(this.database());
		CloseableUtils.closeQuietly(this.commands());
	}
}
