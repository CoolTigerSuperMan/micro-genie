package io.microgenie.service.commands;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.EventHandler;
import io.microgenie.application.events.Subscriber;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.service.AppConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

/***
 * 
 * @author shawn
 *
 */
public class KinesisConsumerCommand extends ConfiguredCommand<AppConfiguration> {

	public KinesisConsumerCommand() {
		this("kinesisconsumer", "A command to consume from a kinesis topic");
	}
	protected KinesisConsumerCommand(String name, String description) {
		super(name, description);
	}
	@Override
	protected void run(final Bootstrap<AppConfiguration> bootstrap,final Namespace namespace, final AppConfiguration configuration) throws Exception {

		final String clientId = Preconditions.checkNotNull(System.getProperty("clientId"), "Command Line argument 'clientId' is required");
		final String topic = Preconditions.checkNotNull(System.getProperty("topic"), "Command Line argument 'topic' is required");
		
		try(ApplicationFactory app = new AwsApplicationFactory(configuration.getAws(), bootstrap.getObjectMapper())){
			final EventFactory events = app.events();
			final ConsumeTopic consumer = new ConsumeTopic(events.createSubscriber(topic, clientId), bootstrap.getObjectMapper());
			final Thread thread = new Thread(consumer);
			thread.start();
			thread.join();//For now block until killed	
		}
	}

	
	
	/**
	 * @author shawn
	 */
	public static final class ConsumeTopic implements Runnable{
		private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeTopic.class);
		private final Subscriber subscriber;
		private final ObjectMapper mapper;

		private final AtomicBoolean running = new AtomicBoolean();

		public ConsumeTopic(final Subscriber subscriber, final ObjectMapper mapper) {
			this.subscriber = subscriber;
			this.mapper = mapper;
		}

		@Override
		public void run() {
			if(this.running.get()){
				LOGGER.info("Consumer is already in a running state, ignoring the call to run");
				return;
			}
			subscriber.subscribe(new LogHandler(mapper));
			while(running.get()){
				try {
					Thread.sleep(2000); //Check back every two seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			subscriber.stop(); // Stop the consuming thread
		}
		
		
		/**
		 * stop the consumer
		 * @throws InterruptedException 
		 */
		public synchronized void stop() throws InterruptedException {
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
