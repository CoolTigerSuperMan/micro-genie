package io.microgenie.application.events;

import io.microgenie.application.events.EventApi;


/**
 * Kinesis EventApi
 * @author shawn
 */
public class Event implements EventApi<String, byte[]>{
	
	private final String topic;
	private final String schema;
	private final String partitionKey;
	private final byte[] data;

	
	public Event(final String topic, final String partitionKey, final byte[] data){
		this(topic, partitionKey, data, null);
	}

	public Event(final String topic, final String partitionKey, final byte[] data, final String schema){
		this.topic = topic;
		this.partitionKey = partitionKey;
		this.data = data;
		this.schema = schema;
	}
	
	@Override
	public String getTopic() {
		return this.topic;
	}
	@Override
	public String getPartitionKey() {
		return this.partitionKey;
	}
	@Override
	public byte[] getBody() {
		return this.data;
	}
	@Override
	public String getSchema() {
		return this.schema;
	}
}
