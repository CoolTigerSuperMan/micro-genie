package io.microgenie.application.events;

import java.nio.ByteBuffer;


/**
 * Kinesis EventApi
 * @author shawn
 */
public class Event{
	
	private final String topic;
	private final String schema;
	private final String partitionKey;
	private final ByteBuffer buffer;

	public Event(final String topic, final String partitionKey, final byte[] data){
		this(topic, partitionKey, data, null);
	}
	public Event(final String topic, final String partitionKey, final byte[] data, final String schema){
			this.topic = topic;
			this.partitionKey = partitionKey;
			this.buffer = ByteBuffer.wrap(data);
			this.schema = schema;
	}
	public String getTopic() {
		return this.topic;
	}
	public String getPartitionKey() {
		return this.partitionKey;
	}
	public byte[] getBody() {
		return this.buffer.array();
	}
	public String getSchema() {
		return this.schema;
	}
}
