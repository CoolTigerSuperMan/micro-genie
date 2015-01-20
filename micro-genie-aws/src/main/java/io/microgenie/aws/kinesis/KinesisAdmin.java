package io.microgenie.aws.kinesis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.DeleteStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;


/**
 * Administrative client for AWS kinesis
 * @author shawn
 */
public class KinesisAdmin {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisAdmin.class);
	private static final int DEFAULT_WAIT_TIME_MINUTES = 10;
	
	private final AmazonKinesisClient client;

	public KinesisAdmin(AmazonKinesisClient client){
		this.client = client;
	}
	
	
	/**
	 * Create the specified topic with the specified number of partitions
	 */
	public void createTopic(String topicName, int partitions) {

		
		LOGGER.info("Determining if  Kinesis topic: {} already exists...", topicName);
		
		try{
			
			final DescribeStreamRequest describeRequest = new DescribeStreamRequest();
			describeRequest.withStreamName(topicName);
			this.client.describeStream(describeRequest);
			
		}catch(ResourceNotFoundException rnf){
			LOGGER.info("Kinesis stream for topic: {} does not exist, creating now with shard count: {}",topicName, partitions);
			final CreateStreamRequest request = new CreateStreamRequest();
			request.withStreamName(topicName);
			request.withShardCount(partitions);
			this.client.createStream(request);
		    this.waitForStreamToBecomeAvailable(topicName, DEFAULT_WAIT_TIME_MINUTES);
			LOGGER.info("Create topic completed for topic: {}", topicName);
		}    
	}
	
	
	
	/**
	 * Delete a topic
	 * @param topic
	 */
	public void deleteTopic(final String topic){

        LOGGER.info("Deleting stream {} ", topic);
        DeleteStreamRequest deleteStreamRequest = new DeleteStreamRequest();
        deleteStreamRequest.setStreamName(topic);
        this.client.deleteStream(deleteStreamRequest);
        LOGGER.info("Stream {} is being deleted", topic);
	}
	
	
	
	/**
	 * Wait up to the specified time for the stream to be created
	 * @param topic
	 * @param minutesToWait
	 */
	private void waitForStreamToBecomeAvailable(final String topic, final int minutesToWait) {

		LOGGER.info("Waiting for topic {} to become ACTIVE...", topic);

	    long startTime = System.currentTimeMillis();
	    long endTime = startTime + (minutesToWait * 60 * 1000);
	    
	    while (System.currentTimeMillis() < endTime) {
	            
	    	try {
	    		Thread.sleep(1000 * 20);
	    	} catch (InterruptedException e) {
	    		// Ignore interruption (doesn't impact stream creation)
	    	}
	    	
	            
	        try {
	                
	        	/** Ask for no more than 10 shards at a time -- this is an optional parameter **/
	            final DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
	            describeStreamRequest.setStreamName(topic);
	            describeStreamRequest.setLimit(10);
	                
	            /** stream response **/
	            final DescribeStreamResult describeStreamResponse = this.client.describeStream(describeStreamRequest);
	            final String streamStatus = describeStreamResponse.getStreamDescription().getStreamStatus();
	            LOGGER.info("Topic: {} Current state: {}", topic, streamStatus);
	            if (streamStatus.equals("ACTIVE")) {
	            	return;
	            }
	        } catch (AmazonServiceException ase) {
	        	if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) {
	        		throw ase;
	        	}
	        	throw new RuntimeException("Stream " + topic + " never went active");
	        }
	    }
	 }
}
