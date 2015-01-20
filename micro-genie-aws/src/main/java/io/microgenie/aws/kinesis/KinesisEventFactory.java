package io.microgenie.aws.kinesis;


import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.Publisher;
import io.microgenie.application.events.Subscriber;
import io.microgenie.aws.KinesisConfig;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;



/***
 * 
 * @author shawn
 *
 */
public class KinesisEventFactory extends EventFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisEventFactory.class);
	
	private final AmazonKinesisClient kinesisClient;
	private final List<KinesisConfig> kinesisConfigs;
	private final AmazonDynamoDBClient dynamoDbClient;
	private final AmazonCloudWatchClient cloudwatchClient;
	
	
	
	private final KinesisAdmin admin;
	
	private final static String WORKER_ID_TEMPLATE = "kinesis-%s-%s";
	
	public KinesisEventFactory(final AmazonKinesisClient kinesisClient, final List<KinesisConfig> kinesisConfigs){
		this(kinesisClient, kinesisConfigs, new AmazonDynamoDBClient(), new AmazonCloudWatchClient());
	}
	
	
	public KinesisEventFactory(final AmazonKinesisClient kinesisClient, final List<KinesisConfig> kinesisConfigs, final AmazonDynamoDBClient dynamoDbClient, final AmazonCloudWatchClient cloudwatchClient){
		this.kinesisClient = kinesisClient;
		this.kinesisConfigs = kinesisConfigs;
		this.dynamoDbClient = dynamoDbClient;
		this.cloudwatchClient = cloudwatchClient;
		this.admin = new KinesisAdmin(this.kinesisClient);
	}



	
	@Override
	public  Subscriber createSubscriber(final String topic, final Properties properties) {
		LOGGER.debug("creating kinsis subscriber for topic {}", topic);
		final String clientId = properties.getProperty(EventFactory.CLIENT_ID);
		final KinesisClientLibConfiguration config = createConsumerConfig(topic, clientId);
		final Subscriber subscriber = new KinesisConsumer(topic, config, this.kinesisClient, this.dynamoDbClient, this.cloudwatchClient);
		return subscriber;
	}


	@Override
	public Publisher createPublisher(final Properties properties) {
		LOGGER.debug("creating kinsis publisher");
		final Publisher publisher = new KinesisProducer(properties.getProperty(EventFactory.CLIENT_ID), this.kinesisClient);
		return publisher;
	}



	/***
	 * Initialize the Kinesis Event factory and ensure topics are created
	 */
	@Override
	public void initialize() {
		this.createTopics();
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
}
