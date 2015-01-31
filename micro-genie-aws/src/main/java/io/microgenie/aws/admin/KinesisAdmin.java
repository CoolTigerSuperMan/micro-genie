package io.microgenie.aws.admin;

import io.microgenie.aws.config.KinesisConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.CreateStreamRequest;
import com.amazonaws.services.kinesis.model.DeleteStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;


/**
 * Administrative client for Amazon kinesis. Kinesis Admin is able to
 * create streams with given shard counts. It is beneficial for the application
 * to perform this tasks as  a pre-startup routine. 
 * <p>
 * This ensures that the application always has the resources in place that it needs 
 * 
 * @author shawn
 */
public class KinesisAdmin {

	private static final int POOL_SIZE = 10;
	
	private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(POOL_SIZE));
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisAdmin.class);
	private static final int DEFAULT_WAIT_TIME_MINUTES = 10;
	
	private final AmazonKinesisClient client;
	private final int waitTimeMinutes;

	public KinesisAdmin(AmazonKinesisClient client){
		this(client, DEFAULT_WAIT_TIME_MINUTES);
	}
	public KinesisAdmin(AmazonKinesisClient client, int waitTimeMinutes){
		this.client = client;
		this.waitTimeMinutes = waitTimeMinutes;
	}
	
	
	
	/***
	 * 
	 * @author shawn
	 *
	 */
	public static class CreateTopicTask implements Runnable{
		private final AmazonKinesisClient client;
		private String topic;
		private int shards;
		public CreateTopicTask(final AmazonKinesisClient client, final String topic, final int shards){
			this.client = client;
			this.topic = topic;
			this.shards = shards;
		}
		@Override
		public void run() {
			this.createTopic(this.topic, this.shards);
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
		 * Wait up to the specified time for the stream to be created
		 * @param topic
		 * @param minutesToWait
		 */
		private void waitForStreamToBecomeAvailable(final String topic, final int minutesToWait) {

			/** Ask for no more than 10 shards at a time -- this is an optional parameter **/
			final int shardsToQuery =  10;
			
			LOGGER.info("Waiting for topic {} to become ACTIVE...", topic);
			
		    final long startTime = System.currentTimeMillis();
		    final long endTime = startTime + (minutesToWait * 60L * 1000L);
		    final long sleepTime = (1000L * 10L);
		    
		    while (System.currentTimeMillis() < endTime) {
		    	try {
		    		Thread.sleep(sleepTime);
		    	} catch (InterruptedException e) {
		    		// Ignore interruption (doesn't impact stream creation)
		    	}
		        try {
		        	
		            final DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
		            describeStreamRequest.setStreamName(topic);
		            describeStreamRequest.setLimit(shardsToQuery);
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
		        	throw new RuntimeException("Stream " + topic + " never became active");
		        }
		    }
		 }
	}
	
	
	
	
	/**
	 * Create the specified topic with the specified number of partitions asynchronously
	 * and return a future
	 */
	public ListenableFuture<?> createTopic(final String topicName, final int partitions) {
		final ListenableFuture<?> future = executorService.submit(new CreateTopicTask(this.client, topicName, partitions));
		return future;
	}
	
	
	
	
	/**
	 * Delete a topic
	 * @param topic
	 */
	public void deleteTopic(final String topic){

        LOGGER.info("Deleting stream {} ", topic);
        final DeleteStreamRequest deleteStreamRequest = new DeleteStreamRequest();
        deleteStreamRequest.setStreamName(topic);
        this.client.deleteStream(deleteStreamRequest);
        LOGGER.info("Stream {} is being deleted", topic);
	}
	
	
	
	


	/***
	 * Initialize kinesis streams
	 * @param kinesis
	 */
	public void initialize(final List<KinesisConfig> kinesis) throws ExecutionException, TimeoutException {
		try{
			this.executeTasks(kinesis);	
		}catch(final InterruptedException ie){
			throw new RuntimeException(ie.getMessage(),ie);
		}
	}
	
	
	/***
	 * Execute tasks concurrently
	 * 
	 * @param configs
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private void executeTasks(final List<KinesisConfig> configs) throws InterruptedException, ExecutionException, TimeoutException{
		
		final List<ListenableFuture<?>> futures = new ArrayList<ListenableFuture<?>>();
		for(KinesisConfig config : configs){
			final ListenableFuture<?> future = executorService.submit(new CreateTopicTask(client, config.getTopic(), config.getShards()));
			futures.add(future);
		}
		for(ListenableFuture<?> future : futures){
			future.get(this.waitTimeMinutes, TimeUnit.MINUTES);
		}
	}
}
