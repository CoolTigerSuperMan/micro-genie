package io.microgenie.application.queue;

import java.io.Closeable;
import java.util.List;

/***
 * QueueFactory
 * @author shawn
 */
public abstract class QueueFactory implements Closeable {

	/***
	 * Creates the default ThreadGroupFactory
	 */
	public QueueFactory(){}
	
	
	public abstract Producer getProducer();
	public abstract Consumer getConsumer(final String queue);
	
	public abstract void produce(final Message message);
	public abstract void produceBatch(final List<Message> messages);
	
	public abstract void consume(String queue, int threadCount, MessageHandler handler);

}
