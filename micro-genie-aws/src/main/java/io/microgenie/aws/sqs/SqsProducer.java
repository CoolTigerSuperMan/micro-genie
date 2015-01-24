package io.microgenie.aws.sqs;

import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.Producer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.common.collect.Maps;


/**
 * SQS Producer Implementation 
 * @author shawn
 */
public class SqsProducer implements Producer{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SqsProducer.class);
	
	private final SqsQueueAdmin queueAdmin;
	private final AmazonSQS sqs;
	
	
	public SqsProducer(AmazonSQS sqs, SqsQueueAdmin queueAdmin){
		this.sqs = sqs;
		this.queueAdmin = queueAdmin;
	}

	
	
	/**
	 * Submit a message to sqs
	 * @param message
	 */
	@Override
	public void submit(final Message message) {
		
		final SendMessageRequest request = new SendMessageRequest()
		.withQueueUrl(this.queueAdmin.getQueueUrl(message.getQueue()))
		.withMessageBody(message.getBody())
		.withMessageAttributes(this.toMessageAttrs(message));
		this.sqs.sendMessage(request);
		
		LOGGER.debug("Successfully submitted messageId: {} to queue: {}", message.getId(), message.getQueue());
	}
	
	
	
	
	/**
	 * Submit a batch of messages to SQS. The messages can be destined for different queues. This
	 * method will categories the messages in batches according to what queue they around bound for. After
	 * categorization of the messages into batches, each batch will be sent serially.
	 * 
	 * failures will be written to the error log.
	 * 
	 * @param messages - The batches of messages which can be destined for one or more queues
	 */
	@Override
	public void submitBatch(final List<Message> messages) {
		final Map<String, List<SendMessageBatchRequestEntry>> messageBatches = this.createBatchesForQueues(messages);
		this.submitBatches(messageBatches);
	}


	
	/***
	 * Submit the batches of messages
	 * @param messageBatches
	 */
	private void submitBatches(
			final Map<String, List<SendMessageBatchRequestEntry>> messageBatches) {
		for(Entry<String, List<SendMessageBatchRequestEntry>> queueBatchEntry : messageBatches.entrySet()){
			final String queueUrl = this.queueAdmin.getQueueUrl(queueBatchEntry.getKey());
			final SendMessageBatchRequest batch = new SendMessageBatchRequest()
			.withQueueUrl(queueUrl)
			.withEntries(queueBatchEntry.getValue());
			final SendMessageBatchResult batchResult = this.sqs.sendMessageBatch(batch);
			this.logFailures(batchResult.getFailed());
		}
	}



	
	/***
	 * Categorize the messages into batches per queue
	 * @param messages
	 * @return messageBatches - belonging to one or more queues
	 */
	private Map<String, List<SendMessageBatchRequestEntry>> createBatchesForQueues(final List<Message> messages) {
		
		final Map<String, List<SendMessageBatchRequestEntry>> messageBatches = new HashMap<String, List<SendMessageBatchRequestEntry>>();

		for(Message message : messages){
			final Map<String, MessageAttributeValue> attributes = this.toMessageAttrs(message);
			
			final SendMessageBatchRequestEntry entry = new SendMessageBatchRequestEntry() 
			.withId(message.getId())
			.withMessageAttributes(attributes)
			.withMessageBody(message.getBody());

			if(!messageBatches.containsKey(message.getQueue())){
				messageBatches.put(message.getQueue(), new ArrayList<SendMessageBatchRequestEntry>());
			}
			messageBatches.get(message.getQueue()).add(entry);
		}
		return messageBatches;
	}
	
	
	

	/***
	 * Logging Failures
	 * @param failed 
	 */
	private void logFailures(List<BatchResultErrorEntry> failed) {
		for(BatchResultErrorEntry batchError :  failed){
			LOGGER.error("Failed to submit sqs batch message entry - Id: {} - Code: {} - Message: {}, isSenders fault: {}",  batchError.getId(), batchError.getCode(), batchError.getMessage(), batchError.getSenderFault());
		}
	}



	/**
	 * Convert Message headers from {@link Message} to SQS {@link MessageAttributeValue} Map entries
	 * @param message
	 * @return messageAttributes
	 */
	private Map<String, MessageAttributeValue> toMessageAttrs(Message message){
		if(message!=null && message.getHeaders() != null && message.getHeaders().size()>0){
			final Map<String, MessageAttributeValue> messageAttrs = Maps.newHashMap();
			for(Entry<String, String> attr : message.getHeaders().entrySet()){
				messageAttrs.put(attr.getKey(), new MessageAttributeValue().withStringValue(attr.getValue()));
			}
			return messageAttrs;
		}
		return null;
	}
}

