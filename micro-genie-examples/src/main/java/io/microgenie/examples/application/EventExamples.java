package io.microgenie.examples.application;

import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventHandler;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.AwsConfig;
import io.microgenie.examples.ExampleConfig;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class EventExamples {
	
	
	
	/***
	 * Run Event Examples
	 * @param args
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws TimeoutException,ExecutionException, IOException, InterruptedException {

		final int eventCount = 1000;
		
		
		final AwsConfig config  = ExampleConfig.createConfigForEventExamples();
		final Properties props  = ExampleConfig.getProperties(ExampleConfig.EVENT_PROPERTY_FILE_NAME);
		
		final String topicOne = props.getProperty(ExampleConfig.TOPIC_1_NAME);
		final String topicTwo = props.getProperty(ExampleConfig.TOPIC_2_NAME);
		
		try (ApplicationFactory app = new AwsApplicationFactory(config, false)) {
			app.initialize();	
			app.events().subcribe(topicOne, EventHandlers.DEFAULT_CLIENT_ID, new ExampleEventHandler(topicOne));
			app.events().subcribe(topicTwo, EventHandlers.DEFAULT_CLIENT_ID, new ExampleEventHandler(topicTwo));

			/** Publish Events to both topics **/
			for(int i=0;i<eventCount; i++){
				
				final Event topic1Event = new Event(topicOne, String.valueOf(i), String.format("topic %s event - event item %d", topicOne, i).getBytes());
				final Event topic2Event = new Event(topicTwo, String.valueOf(i), String.format("topic %s event - event item %d", topicTwo, i).getBytes());
				app.events().publish(EventHandlers.DEFAULT_CLIENT_ID, topic1Event);
				app.events().publish(EventHandlers.DEFAULT_CLIENT_ID, topic2Event);
			}
			
			//finish consuming
			Thread.sleep(10000);
		}
	}
	
	
	public static class ExampleEventHandler implements EventHandler{
		private String handlerId;
		
		public ExampleEventHandler(final String handlerId){
			this.handlerId = handlerId;
		}

		private static final Logger LOGGER = LoggerFactory.getLogger(ExampleEventHandler.class);
		@Override
		public void handle(Event event) {
			String body = new String(event.getBody(), Charsets.UTF_8);
			LOGGER.info("EventHandlerId: {} - handling event: topic: {} - key: {} - body: {}", this.handlerId, event.getTopic(), event.getPartitionKey(), body);
		}
		@Override
		public void handle(List<Event> events) {
			for(Event event : events){
				handle(event);
			}
		}
	}
}
