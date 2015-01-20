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
	
	public abstract void submit(final Message message);
	public abstract void submitBatch(final List<Message> message);
	public abstract void consume(final String queue, final MessageHandler handler);
	public abstract void initialize();
	
	
	/**
	 * Publish A message 
	 * @param message
	 */
	public void produce(Message message){
		this.getProducer().submit(message);
	}
}
