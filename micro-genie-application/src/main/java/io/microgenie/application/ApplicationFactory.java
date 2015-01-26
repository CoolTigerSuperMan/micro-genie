package io.microgenie.application;

import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.queue.QueueFactory;

import java.io.Closeable;



/***
 * Application Command Factory, containing application specific commands
 * @author shawn
 *
 */
public abstract class ApplicationFactory implements Closeable{

	public ApplicationFactory(){}

	public abstract  EventFactory events();
	public abstract  QueueFactory queues();
	public abstract  FileStoreFactory blobs();
	public abstract <T extends DatabaseFactory>  T database();
}
