package io.microgenie.aws.sqs;

import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.Producer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;


/**
 * SQS Producer Implementation
 * @author shawn
 */
public class SqsProducer implements Producer{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SqsProducer.class);
	
	private final Map<String, String> queueUrlMap = Maps.newHashMap();
	private AmazonSQS sqs;
	
	public SqsProducer(AmazonSQS sqs){
		this.sqs = sqs;
	}

	/**
	 * Submit a message to sqs
	 * @param message
	 */
	@Override
	public void submit(final Message message) {
		
		final SendMessageRequest request = new SendMessageRequest()
		.withQueueUrl(this.getQueueUrl(message))
		.withMessageBody(message.getBody())
		.withMessageAttributes(this.toMessageAttrs(message));
		this.sqs.sendMessage(request);
		
		LOGGER.debug("Successfully submitted messageId: {} to queue: {}", message.getId(), message.getQueue());
	}
	
	
	/**
	 * Get the queue url
	 * @param message
	 * @return queueUrl
	 */
	private String getQueueUrl(final Message message){
		Preconditions.checkNotNull(message);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(message.getQueue()), "Queue Name is required in order to submit a message for sqs");
		String url = queueUrlMap.get(message.getQueue());
		if(url!=null){
			return url;
		}else{
			return this.getAndSetQueueUrl(message.getQueue());
		}
	}
	
	
	/**
	 * Get a queue url from a queue name
	 * @param queue
	 * @return queueUrl - For the specified queue name
	 */
	private synchronized String getAndSetQueueUrl(final String queueName){
		try{

			final String url = queueUrlMap.get(queueName); 
			if(url != null){
				return url;
			}else{
				final GetQueueUrlResult result = this.sqs.getQueueUrl(queueName);
				if(result != null && !Strings.isNullOrEmpty(result.getQueueUrl())){
						queueUrlMap.put(queueName, result.getQueueUrl());	
						return result.getQueueUrl();
				}				
			}
		}catch(QueueDoesNotExistException qne){
			throw new RuntimeException(qne.getMessage(),qne);
		}
		return null;
	}

	
	
	/**
	 * Submit a batch of messages to SQS
	 * @param messages
	 */
	@Override
	public void submitBatch(final List<Message> messages) {
		/** TODO Temporary, for now submit the messages one at a time internally until this is implemented**/
		for(Message message : messages){
			this.submit(message);
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

