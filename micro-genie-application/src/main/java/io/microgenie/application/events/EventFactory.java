package io.microgenie.application.events;

import java.io.Closeable;
import java.util.List;



/***
 * EventApi Command Factory 
 * @author shawn
 */
public abstract class EventFactory implements Closeable {

	/***
	 * Constructor
	 */
	public EventFactory(){}
	
	
	public abstract void initialize();
	
	public abstract void publish(final Event event);
	public abstract void publish(final List<Event> events);
	
	public abstract void publish(final String clientId, final Event event);
	public abstract void publish(final String clientId, final List<Event> events);

	
	public abstract Publisher createPublisher(final String clientId);
	public abstract Subscriber createSubscriber(final String topic, final String clientId);
	
	
	public abstract void subcribe(final String topic, final String clientId, EventHandler handler);
	
	/**
	 * Creates a subscription command
	 * @param topic - The topic to subscribe to
	 * @param properties - The Properties to initialize the event subscriber
	 */
	

	
	/**
	 * Creates a publisher
	 * @param properties - The properties required to initialize the publisher
	 * @return publisher
	 */
	public synchronized Publisher getPublisher(final String clientId) {
		return this.createPublisher(clientId);
	}
}
