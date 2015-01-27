package io.microgenie.aws.kinesis;

import io.microgenie.application.events.Event;
import io.microgenie.application.events.Publisher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Implementation using AWS Kinesis
 * @author shawn
 */
public class KinesisProducer implements Publisher{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisProducer.class);

	private final String clientId;
	private final ObjectMapper mapper;
	private final AmazonKinesisClient client;


	/**
	 * Kinesis Publisher
	 * @param client
	 */
	public KinesisProducer(final String clientId, final ObjectMapper mapper, final AmazonKinesisClient client) {
		this.clientId = clientId;
		this.mapper = mapper;
		this.client = client;
	}
	
	
	/**
	 * publish a single message
	 */
	@Override
	public void submit(final Event event) {
		
		try {
			
			Preconditions.checkNotNull(event, "event cannot be null");
			Preconditions.checkNotNull(event.getEventData(), "event data cannot be null");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(event.getTopic()), "Topic is required");
			Preconditions.checkNotNull(event.getPartitionKey(), "Partition key cannot be null");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(event.getPartitionKey().toString()), "PartitionKey is required");
			
			/** serialize the event **/
			byte[] bytes = this.mapper.writeValueAsBytes(event);
	
			final PutRecordRequest putRecordRequest = new PutRecordRequest();
			putRecordRequest.setStreamName(event.getTopic());
			putRecordRequest.setPartitionKey(event.getPartitionKey().toString());
			putRecordRequest.setData(ByteBuffer.wrap(bytes));
			
			//putRecordRequest.setSequenceNumberForOrdering( sequenceNumberOfPreviousRecord );
			
			final PutRecordResult putRecordResult = client.putRecord(putRecordRequest);
			LOGGER.trace("published message to stream: {} partitionKey: {}, sequenceNumberForOrdering: {}, returnedSequenceNumber:{}", 
						putRecordRequest.getStreamName(), 
						putRecordRequest.getPartitionKey(), 
						putRecordRequest.getSequenceNumberForOrdering(), 
						putRecordResult.getSequenceNumber());
		
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	
	
	
	/**
	 * Publish the event batch
	 */
	@Override
	public void submitBatch(List<Event> events) {
		for(Event event: events){
			this.submit(event);
		}
	}



	/**
	 * The clientId of this producer
	 */
	@Override
	public String clientId() {
		return this.clientId;
	}
	

	/***
	 * Close the Producer
	 */
	@Override
	public void close() throws IOException {

	}
}
