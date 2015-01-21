package io.microgenie.application;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.microgenie.application.queue.Consumer;
import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.application.queue.Producer;
import io.microgenie.application.queue.QueueFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;


@RunWith(MockitoJUnitRunner.class)
public class QueueFactoryTest {
	
	private final Producer producer = mock(Producer.class);
	
	private QueueFactory queues;
	
	@Before
	public  void setup(){
		this.queues  = new QueueFactoryImpl(producer);
	}
	
	
	@Test
	public void producerShouldSubmitMessage(){
		
		final Map<String, String> headers = Maps.newHashMap();
		headers.put("correlationId", "123");
		final Message message = new MessageImpl("MockQueue", "1", "This is the body", headers);

		queues.produce(message);
		verify(producer, times(1)).submit(eq(message));		
	}
	
	
	
	static class QueueFactoryImpl extends QueueFactory{

		private final Producer producer;
		public QueueFactoryImpl(final Producer producer) {
			this.producer = producer;
		}
		@Override
		public void close() throws IOException {}

		@Override
		public Producer getProducer() {
			return producer;
		}
		@Override
		public Consumer getConsumer(String queue) {
			return null;
		}
		@Override
		public void submit(Message message) {
			producer.submit(message);
		}
		@Override
		public void submitBatch(List<Message> messages) {
			producer.submitBatch(messages);
		}
		@Override
		public void consume(String queue, MessageHandler handler) {
			
		}

		@Override
		public void initialize() {
			
		}
		
	}
	
	/**
	 * Message Implementation for testing
	 * @author shawn
	 */
	static class MessageImpl implements Message{
		private final Map<String, String> headers;
		private final String id;
		private final String queue;
		private final String body;
		public MessageImpl(final String queue, final String id, final String body, final Map<String, String> headers){
			this.queue = queue;
			this.id = id;
			this.body = body;
			this.headers = headers;
		}
		@Override
		public Map<String, String> getHeaders() {
			return this.headers;
		}
		@Override
		public String getId() {
			return this.id;
		}
		@Override
		public String getQueue() {
			return this.queue;
		}
		@Override
		public String getBody() {
			return this.body;
		}
	}
}
