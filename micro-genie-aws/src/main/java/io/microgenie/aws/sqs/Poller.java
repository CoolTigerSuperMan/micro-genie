package io.microgenie.aws.sqs;

import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.application.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



/****
 * SQS Polling {@link Runnable}
 * @author shawn
 */
class Poller implements Runnable {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(Poller.class);
	
	private final String queue;
	private final String queueUrl;
	private final MessageHandler handler;
	private final AmazonSQSClient client;
	private final AtomicBoolean runFlag;
		
	public Poller(final String queue, final String queueUrl, final MessageHandler handler, final AmazonSQSClient client, final AtomicBoolean runFlag){
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(queueUrl), "Queue URL must be evaludated in order to consume messages from queue ");
		this.queue = queue;
		this.queueUrl = queueUrl; 					
		this.client = client;
		this.handler = handler;
		this.runFlag = runFlag;
	}
	
	
	@Override
	public void run() {
		while(runFlag.get()){
			try{		
				final ReceiveMessageRequest request = new ReceiveMessageRequest(this.queueUrl);
				final ReceiveMessageResult result = this.client.receiveMessage(request);
				if(result !=null && CollectionUtil.hasElements(result.getMessages())){
					final List<com.amazonaws.services.sqs.model.Message> sqsMessages = result.getMessages();		
					this.handleMessages(this.queue, sqsMessages);	
				}
			}catch(AbortedException ex){
				LOGGER.debug("Aborted threadId: {} - message: {} Thread isRunning: {}", Thread.currentThread().getId(),ex.getMessage(), this.runFlag.get());
				if(!runFlag.get()){
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
}

	