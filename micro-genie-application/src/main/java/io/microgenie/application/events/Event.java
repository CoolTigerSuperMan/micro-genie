package io.microgenie.application.events;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Kinesis EventApi
 * @author shawn
 */
public class Event implements EventApi<String, byte[]>{
	
	private final String topic;
	private final String schema;
	private final String partitionKey;
	private final ByteArrayOutputStream byteStream;

	
	public Event(final String topic, final String partitionKey, final byte[] data){
		this(topic, partitionKey, data, null);
	}

	public Event(final String topic, final String partitionKey, final byte[] data, final String schema){
		try{
			this.topic = topic;
			this.partitionKey = partitionKey;
			this.byteStream = new ByteArrayOutputStream(data.length);
			this.byteStream.write(data);
			this.schema = schema;
		}catch(IOException io){
			throw new RuntimeException(io.getMessage(),io);
		}
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
		return this.byteStream.toByteArray();
	}
	@Override
	public String getSchema() {
		return this.schema;
	}
}
