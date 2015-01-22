package io.microgenie.aws.sqs;

import io.microgenie.application.queue.Consumer;
import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.aws.SqsConsumerConfig;
import io.microgenie.commands.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



/***
 * 
 * @author shawn
 */
public class SqsConsumer implements Consumer, Runnable{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SqsConsumer.class);
	
	private final long shutdownTimeMS = 2000L;
	
	private final AmazonSQSClient client;
	private final SqsConsumerConfig config;
	
	private MessageHandler handler;
	private volatile boolean running;
	private String queueUrl;
	private final Thread[] threads; 
	
	
	
	
	/***
	 * Constructor for SQS Consumer
	 * @param client
	 * @param config
	 */
	public SqsConsumer(final AmazonSQSClient client, final SqsConsumerConfig config) {
		
		this.client = Preconditions.checkNotNull(client, "AmazonSQSClient cannot be null");
		
		Preconditions.checkNotNull(config, "SqsConsumerConfig cannot be null");
		this.config = Preconditions.checkNotNull(config.copy(), "The SqsConsumerConfig copy cannot be null");
		this.threads  = new Thread[this.config.getThreads()];
	}
	
	
	/***
	 * Start the Sqs Consumer
	 * @param handler
	 */
	@Override
	public void start(final MessageHandler handler) {
		this.queueUrl = this.getQueueUrl(this.config.getQueue());
		this.handler = handler;
		for(int i = 0;i < threads.length; i++){
			threads[i] = new Thread(this);
			threads[i].start();
		}
	}
	
	
	
	/***
	 * Get the queue Url
	 * @param queue
	 * @return queueUrl
	 */
	private String getQueueUrl(final String queue) {
		final GetQueueUrlResult result = this.client.getQueueUrl(queue);
		if(result != null && !Strings.isNullOrEmpty(result.getQueueUrl())){
			return result.getQueueUrl();
		}
		return null;
	}


	
	/**
	 * Start polling the queue and pump messages to the handler
	 * @param handler
	 */
	private void poll(final  MessageHandler handler) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(this.queueUrl), "Queue URL must be evaludated in order to consume messages from queue %s", this.config.getQueue());
		this.running = true;
		while(running){
			try{		
				final ReceiveMessageRequest request = new ReceiveMessageRequest(this.queueUrl);
				final ReceiveMessageResult result = this.client.receiveMessage(request);
				if(result !=null && CollectionUtil.hasElements(result.getMessages())){
					final List<com.amazonaws.services.sqs.model.Message> sqsMessages = result.getMessages();		
					this.handleMessages(this.config.getQueue(), sqsMessages);	
				}
			}catch(AbortedException ex){
				LOGGER.debug("Aborted threadId: {} - message: {} Thread isRunning: {}", Thread.currentThread().getId(),ex.getMessage(), this.running);
				if(!running){
					return;
				}
			}catch(Exception ex){
				LOGGER.error(ex.getMessage(), ex);
			}
		}
	}
	
	
	/***
	 * Handle Messages
	 * @param queue
	 * @param sqsMessages
	 */
	private void handleMessages(final String queue, final List<com.amazonaws.services.sqs.model.Message> sqsMessages) {
		
		final List<Message> messages =  fromSqsMessages(queue, sqsMessages);
		if(CollectionUtil.hasElements(messages)){
			
			this.handler.handleBatch(messages);
			final List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<DeleteMessageBatchRequestEntry>();

			/*** TODO Allow the caller to specify messages to delete **/
			for(com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages){
				final DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry(sqsMessage.getMessageId(), sqsMessage.getReceiptHandle());
				deleteEntries.add(entry);
			}
			/** Delete the message batch - TODO - This should actually Respect the feedback given from the handler on which messages to delete**/ 
			this.client.deleteMessageBatch(
					new DeleteMessageBatchRequest(this.queueUrl)
					.withEntries(deleteEntries));
		}
	}

	
	/***
	 * Transform to out message format
	 * @param sqsMessages
	 * @return messages
	 */
	private List<Message> fromSqsMessages(final String queue, final List<com.amazonaws.services.sqs.model.Message> sqsMessages){
		
		final List<Message> messages = Lists.newArrayList();
		
		for(com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages){
			final String messageId = sqsMessage.getMessageId();
			final Map<String, String> headers = Maps.newHashMap(sqsMessage.getAttributes());
			final String body = sqsMessage.getBody();
			
			/** Construct the message for the application **/
			final Message message = new Message() {
				@Override
				public String getQueue() {
					return queue;
				}
				@Override
				public String getId() {
					return messageId;
				}
				@Override
				public Map<String, String> getHeaders() {
					return headers;
				}
				@Override
				public String getBody() {
					return body;
				}
			};
			messages.add(message);
		}
		return messages;	
	}

	
	@Override
	public void stop() {
		this.running = false;
		for(Thread t : this.threads){
			LOGGER.info("stopping threadId: {}", t.getId());
			if(!t.isInterrupted()){
				t.interrupt();
			}
		}
		
		try {
			LOGGER.debug("waiting {} milliseconds for SQS consumer threads to shutdown", shutdownTimeMS);
			Thread.sleep(shutdownTimeMS);
		} catch (InterruptedException e) {}
	}


	/**
	 * If true, the consumer is running, polling messages
	 */
	@Override
	public boolean isRunning() {
		return this.running;
	}


	@Override
	public void run() {
		try{
			this.poll(this.handler);	
		}catch(Exception ex){
			LOGGER.debug("Consumer has been aborted");
		}
	}
}


