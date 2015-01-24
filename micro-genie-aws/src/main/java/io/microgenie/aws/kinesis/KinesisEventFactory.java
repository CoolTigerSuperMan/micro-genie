package io.microgenie.aws.kinesis;


import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.EventHandler;
import io.microgenie.application.events.Publisher;
import io.microgenie.application.events.Subscriber;
import io.microgenie.aws.KinesisConfig;

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
	
	
	private final AmazonKinesisClient kinesisClient;
	private final List<KinesisConfig> kinesisConfigs;
	private final AmazonDynamoDBClient dynamoDbClient;
	private final AmazonCloudWatchClient cloudwatchClient;
	

	private Map<String, Publisher> publishers = Maps.newHashMap();
	private final Map<String, Subscriber> subscribers = Maps.newHashMap();

	private final KinesisAdmin admin;

	
	public KinesisEventFactory(final AmazonKinesisClient kinesisClient,final KinesisAdmin admin, final List<KinesisConfig> kinesisConfigs, final AmazonDynamoDBClient dynamoDbClient, final AmazonCloudWatchClient cloudwatchClient){
		this.kinesisClient = kinesisClient;
		this.kinesisConfigs = kinesisConfigs;
		this.dynamoDbClient = dynamoDbClient;
		this.cloudwatchClient = cloudwatchClient;
		this.admin = new KinesisAdmin(this.kinesisClient);
	}
	@Override
	public void publish(Event event) {
		this.publish(DEFAULT_CLIENT_ID, event);
	}
	@Override
	public void publish(List<Event> events) {
		this.publish(DEFAULT_CLIENT_ID, events);
	}
	@Override
	public void publish(final String clientId, Event event) {
		Publisher publisher = this.createPublisher(clientId);
		publisher.submit(event);
	}
	@Override
	public void publish(final String clientId, List<Event> events) {
		Publisher publisher = this.createPublisher(clientId);
		publisher.submitBatch(events);
	}
	
	
	
	/****
	 * Create subscriber if it has not already been created
	 */
	@Override
	public synchronized  Subscriber createSubscriber(final String topic, final String clientId) {
		
		final String clientIdToUse = Strings.isNullOrEmpty(clientId)? DEFAULT_CLIENT_ID : clientId;
		
		Subscriber subscriber = this.subscribers.get(topic);
		if(subscriber == null){
			LOGGER.debug("creating kinsis subscriber for topic {} - clientId: {}", topic, clientIdToUse);
			final KinesisClientLibConfiguration config = createConsumerConfig(topic, clientIdToUse);
			subscriber = new KinesisConsumer(topic, config, this.kinesisClient, this.dynamoDbClient, this.cloudwatchClient);
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
			publisher = new KinesisProducer(clientIdToUse, this.kinesisClient);
			publishers.put(clientIdToUse, publisher);
		}
		return publisher;
	}



	/***
	 * Initialize the Kinesis Event factory and ensure topics are created
	 */
	@Override
	public void initialize() {
		this.createTopics();
	}



	@Override
	public void subcribe(String topic, String clientId, EventHandler handler) {
		final Subscriber subscriber = this.createSubscriber(topic, clientId);
		subscriber.subscribe(handler);
	}
	
	
	
	
	/***
	 * Create consumer configurations
	 */
	private KinesisClientLibConfiguration createConsumerConfig(final String topic, final String clientId) {
		
		final String kinesisApplication = String.format(WORKER_ID_TEMPLATE, topic, clientId);
		final KinesisClientLibConfiguration clientConfig = 
				new KinesisClientLibConfiguration(kinesisApplication, topic, new DefaultAWSCredentialsProviderChain(), UUID.randomUUID().toString());
		return clientConfig;		
	}



	/***
	 * Create Topics
	 */
	private void createTopics() {
		for(KinesisConfig kinesisConfig : kinesisConfigs){
			this.admin.createTopic(kinesisConfig.getTopic(), kinesisConfig.getShards());
		}
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
		for(java.util.Map.Entry<String, Publisher> subscriptionEntry : this.publishers.entrySet()){
			subscriptionEntry.getValue().close();
		}
		this.publishers.clear();
	}
}
