//
//package com.shagwood.examples;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
//import com.amazonaws.services.kinesis.AmazonKinesisClient;
//import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
//import com.google.common.base.Charsets;
//import io.microgenie.aws.kinesis.KinesisAdmin;
//import io.microgenie.aws.kinesis.KinesisConsumer;
//import io.microgenie.aws.kinesis.KinesisMessage;
//import io.microgenie.aws.kinesis.KinesisProducer;
//import com.shagwood.integration.Consumer;
//import com.shagwood.integration.Message;
//import com.shagwood.integration.MessageHandler;
//import com.shagwood.integration.Publisher;
//import com.shagwood.integration.admin.PubSubAdmin;
//
//
///**
// * Kinesis Examples 
// * @author shawn
// */
//public class KinesisExample {
//
//	private static final String EXAMPLE_APPLICATION = "example-application";
//	private static final String EXAMPLE_TOPIC = "example-topic";
//	private static final int EXAMPLE_TOPIIC_PARTITIONS = 3;
//	
//	//private static final AWSCredentialsProvider CREDS = new DefaultAWSCredentialsProviderChain();
//		
//		
//	
//	private static final AWSCredentialsProvider CREDS = new AWSCredentialsProvider(){
//		@Override public void refresh() {}
//        @Override public AWSCredentials getCredentials() {
//            return new BasicAWSCredentials("AKIAIJFP7QJZXVFTNICQ", "DJeKZP/w60n82O4FPKHIqDYRxbsF99J8DavF+oqO");
//        }
//	};
//	 
//	
//	
//	
//	/**
//	 * Run the kinesis example
//	 * @param args
//	 */
//	public static void main(String[] args){
//		
//		final AmazonKinesisClient client = KinesisExample.createClient();
//		Publisher publisher = null;
//		Consumer consumer = null;
//		
//		try{
//			
//			/** Create the partitioned topic **/
//			KinesisExample.createTopic(client, EXAMPLE_TOPIC, EXAMPLE_TOPIIC_PARTITIONS);
//			
//			/** create the publisher **/
//			publisher = new KinesisProducer(client);
//			
//			/** Start the consumer **/
//			consumer = new KinesisConsumer(EXAMPLE_TOPIC, new ExampleHandler(), KinesisExample.createConfiguration());
//			consumer.start();
//			
//			/** Publish Messages **/
//			for(int i=0;i<500; i++){
//				publisher.put(new KinesisMessage(EXAMPLE_TOPIC, new Integer(i), new String("This is message with key " + i).getBytes(Charsets.UTF_8)));
//			}
//		}catch(Exception ex){
//			KinesisExample.deleteTopic(client, EXAMPLE_TOPIC);
//		}finally{
//			if(consumer!=null){
//				consumer.stop();
//			}
//		}
//	}
//	
//	
//	
//	/**
//	 * Create the kinesis topic
//	 * @param client
//	 */
//	public static void createTopic(AmazonKinesisClient client, final String topic, final int partitions){
//		final PubSubAdmin admin = new KinesisAdmin(client);
//		admin.createTopic(topic, partitions);
//	}
//	
//	
//	
//	/**
//	 * Delete the example topic
//	 * @param client
//	 * @param topic
//	 */
//	public static void deleteTopic(AmazonKinesisClient client, final String topic){
//		final PubSubAdmin admin = new KinesisAdmin(client);
//		admin.deleteTopic(topic);
//	}
//	
//	
//	
//	/**
//	 * Create the amazon aws client
//	 * @return
//	 */
//	public static AmazonKinesisClient createClient(){
//		final AmazonKinesisClient client = new AmazonKinesisClient(new BasicAWSCredentials("", ""));
//		return client;
//	}
//	
//	
//	
//	/**
//	 * Create Kinesis Configuration
//	 * @return
//	 */
//	public static KinesisClientLibConfiguration createConfiguration(){
//		final KinesisClientLibConfiguration config = new KinesisClientLibConfiguration(EXAMPLE_APPLICATION, EXAMPLE_TOPIC, CREDS, "worker-1");
//		return config;
//	}
//	
//	
//	
//	/**
//	 * An Example Message Handler
//	 * @author shawn
//	 */
//	public static class ExampleHandler implements MessageHandler{
//		private static final Logger LOGGER = LoggerFactory.getLogger(ExampleHandler.class);
//		@Override
//		public void handleMessage(Message data) {
//			LOGGER.info("Topic: {} - Key: {}", data.getTopic(), data.getPartitionKey());
//		}
//	}
//}
