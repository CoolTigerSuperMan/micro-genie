package io.microgenie.domain.events;

import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventHandler;
import io.microgenie.application.events.Publisher;
import io.microgenie.aws.KinesisConfig;
import io.microgenie.aws.kinesis.KinesisEventFactory;
import io.microgenie.events.BaseEvent;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/***
 * 
 * @author shawn
 */
public class ApiEventFactory implements Closeable{

	private static Logger LOGGER = LoggerFactory.getLogger(ApiEventFactory.class);
	private static final String CLIENT_ID = "front-end-api";
	
	private final ObjectMapper mapper; 
	private Publisher publisher;
	private KinesisEventFactory eventFactory;

	
	/***
	 * API EventApi Factory
	 * @param configs
	 * @param mapper
	 */
	public ApiEventFactory(final List<KinesisConfig> configs, final ObjectMapper mapper){
		this.mapper = mapper;
		final AmazonKinesisClient amzClient = new AmazonKinesisClient();
		final AmazonDynamoDBClient dynamoDbClient = new AmazonDynamoDBClient();
		final AmazonCloudWatchClient cloudwatchClient = new AmazonCloudWatchClient();
		eventFactory = new KinesisEventFactory(amzClient, configs, dynamoDbClient, cloudwatchClient);
	}
	
	
	
	/**
	 * publish events
	 * @param data
	 */
	public <T extends BaseEvent> void publish(final T data){
		
		try {
			final byte[] bytes = this.mapper.writeValueAsBytes(data);
			final Event event = new Event(data.getTopic(), data.getKey(), bytes);
			this.publisher.submit(event);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	
	
	/***
	 * Publish a list of events
	 * @param events
	 */
	public <T extends BaseEvent> void publish(final List<T> events){
		for(BaseEvent event : events){
			this.publish(event);
		}
	}
	
	
	
	/**
	 * Subscribe to logged events
	 * @param topic
	 * @param consuemrProperties
	 * @param handler
	 */
	public void subscribe(final String topic, final String clientId, EventHandler handler){
		this.eventFactory.subcribe(topic, clientId, handler);
	}
	
	
	
	
	/***
	 * Initialization creates any topics that need be created
	 */
	public void initialize(){
		eventFactory.initialize();
		this.publisher  = this.eventFactory.createPublisher(CLIENT_ID);
	}


	/**
	 * Close Resources
	 */
	@Override
	public void close() throws IOException {
		eventFactory.close();
		publisher.close();
	}
}
