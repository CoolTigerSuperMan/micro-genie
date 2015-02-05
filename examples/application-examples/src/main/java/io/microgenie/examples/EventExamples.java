package io.microgenie.examples;

import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventData;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.EventHandler;
import io.microgenie.aws.admin.KinesisAdmin;
import io.microgenie.aws.kinesis.KinesisEventFactory;
import io.microgenie.examples.ExampleConfig;
import io.microgenie.examples.application.EventHandlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;


/***
 * 
 * @author shawn
 *
 */
public class EventExamples {
	

	private static final AmazonKinesisClient kinesis = new AmazonKinesisClient();
	private static final AmazonCloudWatchClient cloudwatch = new AmazonCloudWatchClient();
	private static final AmazonDynamoDBClient dynamodb = new AmazonDynamoDBClient();

	private static final int EVENT_COUNT = 500;
	
	/***
	 * Run Event Examples
	 * @param args
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws TimeoutException,ExecutionException, IOException, InterruptedException {

		final Properties props  = ExampleConfig.getProperties(ExampleConfig.EVENT_PROPERTY_FILE_NAME);
		final String topicOne = props.getProperty(ExampleConfig.TOPIC_1_NAME);
		final String topicTwo = props.getProperty(ExampleConfig.TOPIC_2_NAME);
		
		/** create streams **/
		final KinesisAdmin admin = new KinesisAdmin(kinesis);
		
		ListenableFuture<?>  t1Future = admin.createTopic(topicOne, 1);
		ListenableFuture<?>  t2Future = admin.createTopic(topicTwo, 1);
		
		/** Block until created **/
		t1Future.get();
		t2Future.get();
		
		
		try (EventFactory events = new KinesisEventFactory(kinesis, dynamodb, cloudwatch, ExampleConfig.OBJECT_MAPPER)) {
		
			/** subscribe **/
			events.subcribe(EventHandlers.DEFAULT_CLIENT_ID, topicOne,  new ExampleEventHandler(topicOne));
			events.subcribe(EventHandlers.DEFAULT_CLIENT_ID, topicTwo, new ExampleEventHandler(topicTwo));

			
			/** Publish Events to both topics **/
			for(int i=0;i<EVENT_COUNT; i++){
				
				final Event topic1Event = Event.create(topicOne, String.valueOf(i), createMap("data", String.format("topic %s event - event item %d", topicOne, i)));
				final Event topic2Event = Event.create(topicTwo, String.valueOf(i), createMap("data", String.format("topic %s event - event item %d", topicTwo, i)));
				
				events.publish(EventHandlers.DEFAULT_CLIENT_ID, topic1Event);
				events.publish(EventHandlers.DEFAULT_CLIENT_ID, topic2Event);
			}
			
			Thread.currentThread().join(10000);
			
		}finally{
			kinesis.shutdown();
			dynamodb.shutdown();
			cloudwatch.shutdown();
		}
	}
	
	
	/**
	 * Event Handler, consumes events
	 * @author shawn
	 */
	public static class ExampleEventHandler implements EventHandler{
		private String handlerId;
		
		public ExampleEventHandler(final String handlerId){
			this.handlerId = handlerId;
		}

		private static final Logger LOGGER = LoggerFactory.getLogger(ExampleEventHandler.class);
		@Override
		public void handle(Event event) {
			final EventData eventData = event.getEventData();
			String body = new String(eventData.getData().get("data").toString());
			LOGGER.info("EventHandlerId: {} - handling event: topic: {} - key: {} - body: {}", this.handlerId, event.getTopic(), event.getPartitionKey(), body);
		}
		@Override
		public void handle(List<Event> events) {
			for(Event event : events){
				handle(event);
			}
		}
	}
	
	
	/***
	 * Create a map with a single key and value
	 * @param key
	 * @param value
	 * @return map
	 */
	public static Map<String, Object> createMap(final String key, final Object value){
		Map<String, Object> map = Maps.newHashMap();
		map.put(key, value);
		return map;
	}
}
