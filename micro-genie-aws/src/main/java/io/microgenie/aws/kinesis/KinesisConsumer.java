package io.microgenie.aws.kinesis;

import io.microgenie.application.events.EventHandler;
import io.microgenie.application.events.Subscriber;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;




/**
 * Kinesis Consumer (Uses KCL)
 * 
 * @author shawn
 */
public class KinesisConsumer implements Subscriber{
	
	private final String topic;
	private final KinesisClientLibConfiguration config;
	private final AmazonKinesisClient client;
	private final ObjectMapper mapper;
	
	private Worker worker;
	private AmazonDynamoDBClient dynamoClient;
	private AmazonCloudWatchClient cloudwatchClient;
	
	
	
	
	private final ExecutorService executor = Executors.newCachedThreadPool();

	
	/***
	 * 
	 * All input parameters are required 
     * 
	 * @param topic
	 * @param config
	 * @param client
	 * @param dynamoClient
	 * @param cloudwatchClient
	 */
	public KinesisConsumer(final String topic, 
			final KinesisClientLibConfiguration config, 
			final AmazonKinesisClient client, 
			final AmazonDynamoDBClient dynamoClient, 
			final AmazonCloudWatchClient cloudwatchClient, 
			final ObjectMapper mapper){

		this.topic =  Preconditions.checkNotNull(topic, "A valid kinesis topic is required");
		this.config = Preconditions.checkNotNull(config, "KinesisClientLibConfiguration is required");
		this.client = Preconditions.checkNotNull(client, "AmazonKinesisClient is required");
		this.dynamoClient = Preconditions.checkNotNull(dynamoClient, "AmazonDynamoDBClient is required");
		this.cloudwatchClient = Preconditions.checkNotNull(cloudwatchClient, "AmazonCloudWatchClient is required");
		
		this.mapper = Preconditions.checkNotNull(mapper, "ObjectMapper is required");
	}
	
	
	/***
	 * Subscribe to a kinesis topic, all consumed events are submitted to the provided handler for processing
	 */
	@Override
	public synchronized void subscribe(final EventHandler handler) {
		Preconditions.checkNotNull(handler, "An eventHandler is required to handle consumed events");
		if(this.worker==null){
			this.worker = new Worker(new KinesisRecordProcessorFactory(this.topic, handler, this.mapper), this.config, this.client, this.dynamoClient, this.cloudwatchClient);
		}
		this.executor.execute(this.worker);
	}
	
	
	
	
	@Override
	public synchronized void stop() {
		
		try {
			this.worker.shutdown();
			this.executor.shutdown();
			if(!executor.awaitTermination(5, TimeUnit.SECONDS)){
				this.executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			try{
				this.executor.shutdown();
			}catch(Exception ex){}
		}
	}
	
	
	@Override
	public String getTopic() {
		return topic;
	}
	@Override
	public void close() throws IOException {
		this.stop();
	}
}
