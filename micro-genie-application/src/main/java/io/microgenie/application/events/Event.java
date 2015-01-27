package io.microgenie.application.events;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Strings;



/**
 * Kinesis EventApi
 * @author shawn
 */
public class Event{
	
	private final String id = UUID.randomUUID().toString();
	private final String correlationId;
	private final String topic;
	private final String partitionKey;
	private final EventData data;

	
	public Event(final String topic, final String partitionKey, final EventData data){
		this(topic, partitionKey, null, data);	
	}
	
	public Event(final String topic, final String partitionKey, final String correlationId, final EventData data){
		this.topic = topic;
		this.partitionKey = partitionKey;
		this.data = data;	
		if(Strings.isNullOrEmpty(correlationId)){
			this.correlationId = this.id;
		}else{
			this.correlationId = correlationId;
		}
	}
	

	
	public String getId() {
		return id;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	public String getTopic() {
		return this.topic;
	}
	public String getPartitionKey() {
		return this.partitionKey;
	}
	public EventData getEventData() {
		return data;
	}
	
	public static Event create(final String topic, final String key, final Map<String, Object> data){
		final EventData eventData = new EventData(null, data);
		return new Event(topic, key, eventData);
	}
}
