package io.microgenie.application.events;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;



/**
 * Kinesis EventApi
 * @author shawn
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Event{
	
	private String id = UUID.randomUUID().toString();
	private String correlationId;
	private String topic;
	private String partitionKey;
	private EventData eventData;

	
	protected Event(){};
	
	public Event(final String topic, final String partitionKey, final EventData eventData){
		this(topic, partitionKey, null, eventData);	
	}
	
	public Event(final String topic, final String partitionKey, final String correlationId, final EventData data){
		this.topic = topic;
		this.partitionKey = partitionKey;
		this.eventData = data;	
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
		return eventData;
	}
	
	public static Event create(final String topic, final String key, final Map<String, Object> data){
		final EventData eventData = new EventData(null, data);
		return new Event(topic, key, eventData);
	}
}
