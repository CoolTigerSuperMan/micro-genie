package io.microgenie.aws.kinesis;


import io.microgenie.application.StateChangeConfiguration;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.EventHandler;
import io.microgenie.application.events.Publisher;
import io.microgenie.application.events.StateChangePublisher;
import io.microgenie.application.events.Subscriber;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;



/***
 * KinesisEventfactory provides a common pub/sub interface for the application. 
 * <p> 
 * The capability to create and return a {@link Publisher} and / or a {@link Subscriber}
 * is also available
 * 
 * @author shawn
 *
 */
public class KinesisEventFactory extends EventFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisEventFactory.class);

	private static final String WORKER_ID_TEMPLATE = "kinesis-%s-%s";
	private static final String DEFAULT_CLIENT_ID = "default-client";
	
	private final ObjectMapper mapper;
	
	private final AmazonKinesisClient kinesisClient;	
	private final AmazonDynamoDBClient dynamoDbClient;
	private final AmazonCloudWatchClient cloudwatchClient;

	
	private final Map<String, Publisher> publishers = Maps.newHashMap();
	private final Map<String, Subscriber> subscribers = Maps.newHashMap();

	
	public KinesisEventFactory(final AmazonKinesisClient kinesisClient, final AmazonDynamoDBClient dynamoDbClient, final AmazonCloudWatchClient cloudwatchClient, final ObjectMapper mapper){
		this.mapper = mapper;
		this.kinesisClient = kinesisClient;
		this.dynamoDbClient = dynamoDbClient;
		this.cloudwatchClient = cloudwatchClient;
	}
	
	@Override
	public void publish(final Event event) {
		this.publish(DEFAULT_CLIENT_ID, event);
	}
	@Override
	public void publish(final List<Event> events) {
		this.publish(DEFAULT_CLIENT_ID, events);
	}
	@Override
	public void publish(final String clientId, final Event event) {
		final Publisher publisher = this.createPublisher(clientId);
		publisher.submit(event);
	}
	@Override
	public void publish(final String clientId, final List<Event> events) {
		Publisher publisher = this.createPublisher(clientId);
		publisher.submitBatch(events);
	}
	
	
	
	
	/****
	 * Create subscriber if it has not already been created
	 */
	@Override
	public synchronized  Subscriber createSubscriber(final String clientId, final String topic) {
		
		final String clientIdToUse = Strings.isNullOrEmpty(clientId)? DEFAULT_CLIENT_ID : clientId;
		Subscriber subscriber = this.subscribers.get(topic);
		if(subscriber == null){
			LOGGER.debug("creating kinsis subscriber for topic {} - clientId: {}", topic, clientIdToUse);
			final KinesisClientLibConfiguration config = createConsumerConfig(clientIdToUse, topic);
			subscriber = new KinesisConsumer(topic, config, this.kinesisClient, this.dynamoDbClient, this.cloudwatchClient, this.mapper);
			this.subscribers.put(topic, subscriber);
		}
		return subscriber;
	}

	

	
	/***
	 * Create publisher, if it has not already been created
	 */
	@Override
	public synchronized Publisher createPublisher(final String clientId) {
		
		final String clientIdToUse = Strings.isNullOrEmpty(clientId) ? DEFAULT_CLIENT_ID : clientId;
		
		Publisher publisher = publishers.get(clientIdToUse);
		if(publisher==null){
			LOGGER.debug("creating kinsis publisher");
			publisher = new KinesisProducer(clientIdToUse, this.mapper, this.kinesisClient);
			publishers.put(clientIdToUse, publisher);
		}
		return publisher;
	}




	@Override
	public StateChangePublisher createChangePublisher(final String clientId, StateChangeConfiguration stateChangeConfig) {
		final Publisher publisher = this.createPublisher(clientId);
		final StateChangePublisher changePublisher = new StateChangePublisher(stateChangeConfig, publisher);
		return changePublisher;
	}
	
	

	@Override
	public void subcribe(final String clientId, final String topic, final EventHandler handler) {
		final Subscriber subscriber = this.createSubscriber(clientId, topic);
		subscriber.subscribe(handler);
	}
	
	
	
	
	/***
	 * Create consumer configurations
	 */
	private KinesisClientLibConfiguration createConsumerConfig(final String clientId, final String topic) {
		
		final String kinesisApplication = String.format(WORKER_ID_TEMPLATE, topic, clientId);
		final KinesisClientLibConfiguration clientConfig = 
				new KinesisClientLibConfiguration(kinesisApplication, topic, new DefaultAWSCredentialsProviderChain(), UUID.randomUUID().toString());
		return clientConfig;		
	}



	
	/***
	 * Close all publishers and subscribers
	 */
	@Override
	public void close() throws IOException{
		
		for(java.util.Map.Entry<String, Subscriber> subscriptionEntry : this.subscribers.entrySet()){
			subscriptionEntry.getValue().stop();
		}
		this.subscribers.clear();
		for(java.util.Map.Entry<String, Publisher> publisherEntry : this.publishers.entrySet()){
			publisherEntry.getValue().close();
		}
		this.publishers.clear();
	}
}
