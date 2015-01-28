package io.microgenie.service.commands;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.EventHandler;
import io.microgenie.application.events.Subscriber;
import io.microgenie.aws.kinesis.KinesisEventFactory;
import io.microgenie.service.AppConfiguration;
import io.microgenie.service.commands.CommandConfiguration.KinesisConsumerConfig;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

/***
 * 
 * @author shawn
 *
 */
public class KinesisConsumerCommand extends ConfiguredCommand<AppConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisConsumerCommand.class);
	public KinesisConsumerCommand() {
		this("kinesisconsumer", "A command to consume from a kinesis topic");
	}
	protected KinesisConsumerCommand(String name, String description) {
		super(name, description);
	}
	/***
	 * Run the command to consume from a kinesis topic. This requires a configuration file that is able to load 
	 * the an instance of KinesisConsumerConfig
	 */
	@Override
	protected void run(final Bootstrap<AppConfiguration> bootstrap,final Namespace namespace, final AppConfiguration configuration) throws Exception {

		Preconditions.checkNotNull(configuration.getCommands(), "the commands section must be configured in your yml configuration file");
		Preconditions.checkNotNull(configuration.getCommands().getKinesisConsumer(), "the kinesis consumer must be configured in your configuration file to run the kinesis consumer");
		Preconditions.checkNotNull(configuration.getCommands().getKinesisConsumer().getClientId(), "Kinesis consumer client id is required");
		Preconditions.checkNotNull(configuration.getCommands().getKinesisConsumer().getTopic(), "Kinesis consumer topic is required in your yaml configuration file");

		final AmazonKinesisClient kinesis = new AmazonKinesisClient();
		final AmazonDynamoDBClient dynamodb = new AmazonDynamoDBClient();
		final AmazonCloudWatchClient cloudwatch = new AmazonCloudWatchClient();
		
		/** start the kinesis consumer **/
		 
		try(EventFactory events = new KinesisEventFactory(kinesis, dynamodb, cloudwatch, bootstrap.getObjectMapper())){
			
			final KinesisConsumerConfig kinesisConfig = configuration.getCommands().getKinesisConsumer();
			final TopicConsumer consumer = new TopicConsumer(events.createSubscriber(kinesisConfig.getTopic(), kinesisConfig.getClientId()), bootstrap.getObjectMapper());
			final Thread thread = new Thread(consumer);
			thread.start();
			thread.join();	
			
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
		}finally{
			kinesis.shutdown();
			dynamodb.shutdown();
			cloudwatch.shutdown();
		}
	}

	
	
	
	/**
	 * @author shawn
	 */
	public static final class TopicConsumer implements Runnable{
		private static final Logger LOGGER = LoggerFactory.getLogger(TopicConsumer.class);
		private final Subscriber subscriber;
		private final ObjectMapper mapper;

		private final AtomicBoolean running = new AtomicBoolean();

		public TopicConsumer(final Subscriber subscriber, final ObjectMapper mapper) {
			Preconditions.checkNotNull(subscriber, "No nulls allowed - subscriber was null");
			Preconditions.checkNotNull(subscriber, "No nulls allowed - objectMapper was null");
			this.subscriber = subscriber;
			this.mapper = mapper;
		}

		
		/***
		 * Run the consumer
		 */
		@Override
		public void run() {
			try{
				if(this.running.getAndSet(true)){
					LOGGER.info("Consumer is already in a running state, ignoring the call to run");
					return;
				}
				subscriber.subscribe(new LogHandler(mapper));
				while(running.get()){
					try {
						Thread.sleep(2000); //Check back every two seconds
					} catch (InterruptedException e) {
						running.set(false);
						LOGGER.error(e.getMessage(), e);
					}
				}	
				LOGGER.info("exisiting consumer gracefully for consumption of topic: {}", subscriber.getTopic());
			}finally{
				subscriber.stop(); // Stop the consuming thread
				running.set(false);
			}
		}
		
		
		
		/**
		 * stop the consumer
		 * @throws InterruptedException 
		 */
		public void stop() throws InterruptedException {
			this.running.set(false);
		}
		
		
		/***
		 * Append events to the local log
		 * 
		 * @author shawn
		 *
		 */
		public static class LogHandler implements EventHandler {
			private ObjectMapper mapper;

			public LogHandler(final ObjectMapper mapper) {
				this.mapper = mapper;
			}
			@Override
			public void handle(Event event) {
				try {
					LOGGER.info("consumed kinesis event - eventId: {} - topic: {} - key: {} - eventData: {}",
							event.getId(), event.getTopic(), event.getPartitionKey(), this.mapper.writeValueAsString(event.getEventData()));
				} catch (Exception ex) {
					LOGGER.error(ex.getMessage(), ex);
				}
			}
			@Override
			public void handle(List<Event> events) {
				for (Event event : events) {
					handle(event);
				}
			}
		}
	}
}
