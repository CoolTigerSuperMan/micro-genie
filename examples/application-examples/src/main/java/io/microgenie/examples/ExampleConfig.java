package io.microgenie.examples;

import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.aws.AwsConfig;
import io.microgenie.aws.DynamoDbConfig;
import io.microgenie.aws.KinesisConfig;
import io.microgenie.aws.S3Config;
import io.microgenie.aws.SqsConfig;
import io.microgenie.aws.SqsConsumerConfig;
import io.microgenie.aws.SqsQueueConfig;
import io.microgenie.examples.application.EventHandlers;
import io.microgenie.examples.util.ExampleProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;


public class ExampleConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleConfig.class);
	
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public static final ExampleProperties appProperties = new ExampleProperties();
	
	/** Command Example Properties **/
	public static final String PROPERTY_FILE_NAME = "application.properties";
	public static final String EVENT_PROPERTY_FILE_NAME = "event.examples.properties";
	public static final String DATABASE_PROPERTY_FILE_NAME = "database.examples.properties";
	
	/** Property Keys **/
	public  static final String BUCKET_PROPERTY = "s3.buckets.default";
	public  static final String FILE_SAVED_QUEUE_PROPERTY = "sqs.queues.filesaved";
	
	
	
	/*** Event Example Properties **/
	public static final String TOPIC_1_NAME = "events.topics.topic1";
	public static final String TOPIC_1_SHARDS = "events.topics.topic1.shards";
	
	public static final String TOPIC_2_NAME = "events.topics.topic2";
	public static final String TOPIC_2_SHARDS = "events.topics.topic2.shards";
	
	
	
	
	/*** Database Examples **/
	public static final String DATABASE_SCAN_PACKAGE = "database.scanPackagePrefix";

	
	public static Properties getProperties(final String propertyFileName) throws IOException{
		return appProperties.getProperties(propertyFileName);
	}
	
	
	/***
	 * Create the aws Configuration
	 * @param properties 
	 * @return awsConfig
	 * @throws IOException 
	 */
	public static AwsConfig createConfig(final String propertyFileName) throws IOException {
		
		final Properties props = appProperties.getProperties(propertyFileName);
		final String defaultBucket = props.getProperty(ExampleConfig.BUCKET_PROPERTY);
		final String fileSavedQueueName = props.getProperty(ExampleConfig.FILE_SAVED_QUEUE_PROPERTY);
		final S3Config s3 = new S3Config();
		
		s3.setDefaultDrive(defaultBucket);
		
		SqsQueueConfig queue = new SqsQueueConfig();
		queue.setName(fileSavedQueueName);
		
		final SqsConsumerConfig consumerConfig = new SqsConsumerConfig();
		consumerConfig.setQueue(fileSavedQueueName);
		consumerConfig.setThreads(3);
		consumerConfig.setHandlerInstance(new MessageHandler(){
			@Override
			public void handle(Message message) {
				LOGGER.info("consumed message queue: {} - id: {} body:{}", message.getQueue(), message.getId(), message.getBody());
			}
			@Override
			public void handleBatch(List<Message> messages) {
				for(Message message : messages){
					handle(message);
				}
			}
		});
		final SqsConfig sqs = new SqsConfig();
		sqs.getQueues().add(queue);
		sqs.getConsumers().add(consumerConfig);
		
		final AwsConfig awsConfig = new AwsConfig();
		awsConfig.setS3(s3);
		awsConfig.setSqs(sqs);
		return awsConfig;
	}
	
	
	
	public static AwsConfig createConfigForEventExamples() throws IOException {
		
		/** Event examples property file **/
		final Properties props = appProperties.getProperties(EVENT_PROPERTY_FILE_NAME);
		
		/** Topic 1 **/
		final String topic1Name = props.getProperty(ExampleConfig.TOPIC_1_NAME);
		final String topic1Shards = props.getProperty(ExampleConfig.TOPIC_1_SHARDS);
		
		/** Topic 2 **/
		final String topic2Name = props.getProperty(ExampleConfig.TOPIC_2_NAME);
		final String topic2Shards = props.getProperty(ExampleConfig.TOPIC_2_SHARDS);
		
		final AwsConfig aws = new AwsConfig();
		final List<KinesisConfig> kinesisConfig = Lists.newArrayList();
		
		final KinesisConfig topic1 = new KinesisConfig();
		topic1.setTopic(topic1Name);
		topic1.setShards(Integer.valueOf(topic1Shards));
		
		final KinesisConfig topic2 = new KinesisConfig();
		topic2.setTopic(topic2Name);
		topic2.setShards(Integer.valueOf(topic2Shards));
		
		kinesisConfig.add(topic1);
		kinesisConfig.add(topic2);
		aws.setKinesis(kinesisConfig);
		
		return aws;
	}
	
	
	
	public static AwsConfig createConfigForDatabaseExamples() throws IOException {
		
		final Properties props = appProperties.getProperties(DATABASE_PROPERTY_FILE_NAME);

		/** Scan Package **/
		final String scanPackage = props.getProperty(ExampleConfig.DATABASE_SCAN_PACKAGE);

		final AwsConfig aws = new AwsConfig();
		final  DynamoDbConfig dynamo = new DynamoDbConfig();
		dynamo.setPackagePrefix(scanPackage);
		aws.setDynamo(dynamo);
		
		
		/** Kinesis Topics **/
		
		/** Book Checkout Request **/
		final List<KinesisConfig> kinesisConfigList = new ArrayList<KinesisConfig>();
		final KinesisConfig checkoutBookRequestStream = new KinesisConfig();
		checkoutBookRequestStream.setShards(1);
		checkoutBookRequestStream.setTopic(EventHandlers.TOPIC_CHECKOUT_BOOK_REQUEST);
		checkoutBookRequestStream.setShards(EventHandlers.TOPIC_CHECKOUT_BOOK_REQUEST_SHARDS);
		kinesisConfigList.add(checkoutBookRequestStream);
		
		/** Book Checked out Event **/
		final KinesisConfig bookCheckedOutEventStream = new KinesisConfig();
		bookCheckedOutEventStream.setTopic(EventHandlers.TOPIC_BOOK_CHANGE_EVENT);
		bookCheckedOutEventStream.setShards(EventHandlers.TOPIC_BOOK_CHANGE_EVENT_SHARDS);
		kinesisConfigList.add(bookCheckedOutEventStream);
		aws.setKinesis(kinesisConfigList);
		return aws;
	}
}
