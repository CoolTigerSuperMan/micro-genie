package io.microgenie.examples;

import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.aws.admin.SqsQueueAdmin;
import io.microgenie.aws.config.SqsConfig;
import io.microgenie.aws.config.SqsConsumerConfig;
import io.microgenie.aws.config.SqsQueueConfig;
import io.microgenie.aws.sqs.SqsFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.common.collect.Maps;


/***
 * SQS QueueFactory Examples which show how to perform the following actions:
 * 
 *  <ol>
 *  	<li>Configure SQS Queues</li>
 *  	<li>Initialize SQS Queues</li>
 *   	<li>Configure an SQS Producer</li>
 *  	<li>Configure multithreaded SQS Consumers</li>
 *      <li>Publish Messages to SQS</li>
 *  	<li>Start Consuming SQS Messages</li>
 *  	<li>Shutdown all consuming threads - {@link Closeable} is implemented by {@link SqsFactory}</li>
 *  </ol>
 *  
 * @author shawn
 */

public class QueueExamples {
	
	
	private static final AmazonSQSClient sqsClient = new AmazonSQSClient();

	private static final String QUEUE_1 = "Queue1";
	private static final String QUEUE_2 = "Queue2";
	private static final String QUEUE_3 = "Queue3";
	
	private static final int MESSAGE_COUNT = 10;
	
	
	
	/***
	 * Execute SQS Examples 
	 * 
	 * <p>
	 * This example uses {@link SqsQueueAdmin} to ensure three sqs queues are created and ready before any producers or consumers start.
	 * </p>
	 * 
	 * 
	 * <p>
	 * It then produces to all three queues and consumes from all three queues. 
	 * Consumers can be configured with multiple threads.
	 * </p>
	 * 
	 * <p>
	 * The example shows how to start pre-configured consumers, 
	 * as well as how to start consuming from a thread, ad hoc
	 * </p>
	 * 
	 * <ol>Use SQSAdmin to configure three sqs queues which are garunteed to be initialized before and producers or consumers are started</ol>
	 * <ol>Start pre-configured consumers with multiple threads for queues 1 and 2</ol>
	 * <ol>Start a new consumer ad hoc, against to consume from queue 3 which has not been pre-configured</ol>
	 * 
	 * 
	 * @param args
	 * @throws TimeoutException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {

		final SqsQueueAdmin admin = new SqsQueueAdmin(sqsClient);
		final SqsConfig config = QueueExamples.createQueueConfig();
		
		admin.initialize(config);

		/** Create the SQS Queue factory implementation **/ 
		try (QueueFactory queues = new SqsFactory(sqsClient, config)){
			
			/** Start the pre-configured queue consumers **/ 
			queues.getConsumer(QUEUE_1).start();
			queues.getConsumer(QUEUE_2).start();
			
			/** start a consumer (ad hoc) against queue three, that was NOT pre configured **/
			queues.consume(QUEUE_3, 1, new LoggingMessageHandler("q3-consumer"));
			
			
			/** Produce Messages to queues, 1, 2, and 3 **/
			for(int i=0;i<MESSAGE_COUNT; i++){
				
				/** Produce message to queue 1 **/
				final Message queueOneMessage = QueueExamples.createMessage(QUEUE_1,  String.valueOf(i), String.format("item: %d - queue %s - message item %d", i, QUEUE_1, i));
				queues.produce(queueOneMessage);
				
				/** Produce message to queue 2 **/
				final Message queueTwoMessage = QueueExamples.createMessage(QUEUE_2, String.valueOf(i), String.format("item: %d - queue %s - message item %d", i, QUEUE_2, i));
				queues.produce(queueTwoMessage);
				
				/** Produce message to queue 3 **/
				final Message queueThreeMessage = QueueExamples.createMessage(QUEUE_3,  String.valueOf(i), String.format("item: %d - queue %s - message item %d", i, QUEUE_3, i));
				queues.produce(queueThreeMessage);
			}
			
			/** wait for all messages to be consumed **/
			Thread.currentThread().join(5000);
		}finally{
			sqsClient.shutdown();
		}
	}
	
	
	
	/***
	 * Create a message
	 * 
	 * @param queue
	 * @param id
	 * @param body
	 * @return message
	 */
	private static Message createMessage(final String queue, final String id, final String body) {
		return new Message(){
			@Override
			public Map<String, String> getHeaders() {
				return Maps.newHashMap();
			}
			@Override
			public String getId() {
				return id;
			}
			@Override
			public String getQueue() {
				return queue;
			}
			@Override
			public String getBody() {
				return body;
			}};
	}



	/***
	 * Create Queue Configuration.
	 * <p>
	 * Configure 3 Queues and configure consumers for queue1 and queue2
	 * <p>
	 * Also set a flag to ensure queues are created before continuing by {@link SqsQueueAdmin}
	 * <p>
	 * We also set a flag which tells the {@link SqsFactory} that we will be producing messages
	 * 
	 * @return sqsConfig
	 */
	private static SqsConfig createQueueConfig(){
		
		/** Configure Queues to be initialized **/
		final List<SqsQueueConfig> queueConfigs = new ArrayList<SqsQueueConfig>();
		
		/** Queue 1 **/
		final SqsQueueConfig queue1 = new SqsQueueConfig();
		queue1.setName(QUEUE_1);
		queueConfigs.add(queue1);
		
		/** Queue 2 **/
		final SqsQueueConfig queue2 = new SqsQueueConfig();
		queue2.setName(QUEUE_2);
		queueConfigs.add(queue2);
		
		/** Queue 3 **/
		final SqsQueueConfig queue3 = new SqsQueueConfig();
		queue3.setName(QUEUE_3);
		queueConfigs.add(queue3);

		/** Configure Consumers to be initialized  **/
		final List<SqsConsumerConfig> consumerConfigs = new ArrayList<SqsConsumerConfig>();

		/** Configure Two consumers for Queue 1 **/
		final SqsConsumerConfig queueOneConsumerConfig = new SqsConsumerConfig();
		queueOneConsumerConfig.setHandlerInstance(new LoggingMessageHandler("q1-consumer"));
		queueOneConsumerConfig.setQueue(queue1.getName());
		queueOneConsumerConfig.setThreads(2);
		consumerConfigs.add(queueOneConsumerConfig); 	//Add consumer config for queue 1


		/** Configure One consumer for Queue 2 **/
		final SqsConsumerConfig queueTwoConsumerConfig = new SqsConsumerConfig();
		queueTwoConsumerConfig.setHandlerInstance(new LoggingMessageHandler("q2-consumer"));
		queueTwoConsumerConfig.setQueue(queue2.getName());
		queueTwoConsumerConfig.setThreads(1); 			//Being explicit, one thread is the default
		consumerConfigs.add(queueTwoConsumerConfig); 	//Add consumer config for queue 2
		
		
		/** Create the complete SqsQueueFactory Configuration **/
		final SqsConfig config = new SqsConfig();
		config.setQueues(queueConfigs); 		//Add queue configurations
		config.setConsumers(consumerConfigs); 	// Add consumer configurations
		config.setBlockUntilReady(true); 		// Tell the admin to block until queues are created
		config.setProduces(true); 				// Tells the factory we will be producing to these queues
		return config;
	}
	
	
	
	
	/***
	 * A Message Handler that logs messages it consumes 
	 * @author shawn
	 */
	public static class LoggingMessageHandler implements MessageHandler{
		private final String consumerId;
		public LoggingMessageHandler(final String consumerId){
			this.consumerId = consumerId;
		}
		private static final Logger LOGGER = LoggerFactory.getLogger(LoggingMessageHandler.class);
		@Override
		public void handle(Message message) {
			LOGGER.info("ConsumerId: {} - ThreadId: {} - Queue: {} - MessageId: {} - MessageBody: {}", consumerId, Thread.currentThread().getId(), message.getQueue(), message.getId(), message.getBody());
		}
		@Override
		public void handleBatch(List<Message> messages) {
			for(Message message : messages){
				this.handle(message);
			}
		}
	}
}
