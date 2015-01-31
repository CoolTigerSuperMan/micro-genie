package io.microgenie.application.events;

import io.microgenie.application.StateChangeConfiguration;

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
	

	public abstract void publish(final Event event);
	public abstract void publish(final List<Event> events);
	
	public abstract void publish(final String clientId, final Event event);
	public abstract void publish(final String clientId, final List<Event> events);

	public abstract Publisher createPublisher(final String clientId);
	public abstract Subscriber createSubscriber(final String clientId, final String topic);
	
	public abstract StateChangePublisher createChangePublisher(final String clientId, final StateChangeConfiguration stateChangeConfig);
	
	
	/***
	 * Subscribe to the given topic, providing an event handler that will process all
	 * consumed events for this Subscriber
	 * 
	 * @param topic
	 * @param clientId
	 * @param handler
	 */
	public abstract void subcribe(final String clientId, final String topic, final EventHandler handler);
	

	
	/**
	 * Creates a publisher
	 * @param clientId - The clientId of the publisher to get
	 * @return publisher
	 */
	public synchronized Publisher getPublisher(final String clientId) {
		return this.createPublisher(clientId);
	}
}
