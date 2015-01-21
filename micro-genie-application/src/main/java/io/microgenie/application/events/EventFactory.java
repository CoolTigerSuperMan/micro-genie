package io.microgenie.application.events;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;



/***
 * EventApi Command Factory 
 * @author shawn
 */
public abstract class EventFactory implements Closeable {

	
	public static final String CLIENT_ID = "clientId";
	
	private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<String, Subscriber>();
	private Publisher publisher;

	
	
	
	/***
	 * Constructor
	 */
	public EventFactory(){}
	
	
	public abstract void initialize();
	
	
	public Event publish(final Event event){
		if(this.publisher==null){
			this.createPublisher();
		}
		publisher.submit(event);
		return event;
	}
	
	
	private synchronized void createPublisher() {
		if(this.publisher==null){
			Properties props = new Properties();
			props.put(CLIENT_ID, "defaultClient"); //For now, default client
			this.publisher = this.createPublisher(props);
		}
	}


	/** Publish the batch of events **/
	public void publish(final List<Event> events){
		publisher.submitBatch(events);
	}
	
	
	
	
	public abstract Publisher createPublisher(final Properties properties);
	public abstract Subscriber createSubscriber(final String topic, final Properties properties);
	
	
	
	
	/**
	 * Creates a subscription command
	 * @param topic - The topic to subscribe to
	 * @param properties - The Properties to initialize the event subscriber
	 */
	public synchronized void subcribe(final String topic, Properties properties, EventHandler handler) {

		if(!this.subscribers.containsKey(topic)){
			final Subscriber subscriber = this.createSubscriber(topic, properties);
			subscriber.subscribe(handler);
			this.subscribers.put(topic, subscriber);
		}
	}
	
	
	
	/**
	 * Creates a publisher
	 * @param properties - The properties required to initialize the publisher
	 * @return publisher
	 */
	public synchronized Publisher getPublisher(final String clientId, Properties properties) {
		return this.createPublisher(properties);
	}
	
	
	


	/**
	 * Shutdown all publishers, subscribers and thread pool threads
	 */
	@Override
	public void close() throws IOException {
		if(this.publisher!=null){
			this.publisher.close();
		}
		for(Entry<String, Subscriber>  subscriber: this.subscribers.entrySet()){
			subscriber.getValue().close();	
		}
	}
}
