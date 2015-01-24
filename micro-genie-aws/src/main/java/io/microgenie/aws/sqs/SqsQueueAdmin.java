package io.microgenie.aws.sqs;

import io.microgenie.aws.SqsQueueConfig;
import io.microgenie.commands.util.CollectionUtil;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/***
 * Used to administer SQS Queues and initialize the queues if they do not exist.
 * <p> 
 * Sqs Queue Admin also performs Queue name to Queue URL translations and keeps an internal cache
 * once resolved
 * @author shawn
 */
public class SqsQueueAdmin {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SqsQueueAdmin.class);
	
	private final Map<String, String> queueUrlMap = Maps.newHashMap();
	private final AmazonSQSClient sqs;

	private volatile boolean shutdown = false;
	
	
	
	public SqsQueueAdmin(final AmazonSQSClient sqs){
		this.sqs = sqs;
	}
	
	
	/**
	 * Initialize Queues
	 * @param queues
	 */
	public void initializeQueues(final List<SqsQueueConfig> queues, final boolean blockUntilReady){
		
		if(CollectionUtil.hasElements(queues)){
			for(SqsQueueConfig queue: queues){
				queue.loadAttributes();
				final CreateQueueRequest request = new CreateQueueRequest(queue.getName())
				.withAttributes(queue.getAttributes());
				this.sqs.createQueue(request);
			}

			/** TODO - This does not currently correctly capture the start time, 
			 * still need to capture the time the create call was made 
			 *
			 * if blockUntilReady is true, Ensure all queues are created before continuing
			 * **/
			if(blockUntilReady){
				for(SqsQueueConfig config : queues){
					this.ensureQueueIsReady(config);
				}				
			}
		}
	}
	
	
	
	/**
	 * Get the queue url. First an internal cache is checked, if the name to queueUrl mapping
	 * is not found in the internal cache a call is made to the Sqs API. If a valid queue
	 * url is returned the name -> queueUrl mapping will be cached locally 
	 * @param queueName
	 * @return queueUrl
	 */
	public String getQueueUrl(final String queueName){

		Preconditions.checkArgument(!Strings.isNullOrEmpty(queueName), "Queue Name is required in order to submit a message for sqs");
		String url = queueUrlMap.get(queueName);
		if(url!=null){
			return url;
		}else{
			return this.getAndSetQueueUrl(queueName);
		}
	}
	

	
	/**
	 * Get a queue url from a queue name
	 * @param queueName
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

	
	
	/***
	 * Note, by the time this is called the queue creation process should have already been executed
	 * @param queue
	 */
	private void ensureQueueIsReady(SqsQueueConfig config) {
		
		final long timeout = (DateTime.now().getMillis() + config.getQueueCreationTimeoutMS());
		while(!shutdown && DateTime.now().getMillis() < timeout){
			try{
				final GetQueueUrlResult queueUrl = this.sqs.getQueueUrl(config.getName());
				if(queueUrl!=null && !Strings.isNullOrEmpty(queueUrl.getQueueUrl())){
					return; //Queue is ready
				}
			}catch(AmazonServiceException asException){
				/** not retryable **/
				throw new RuntimeException(asException.getMessage(), asException);
			}catch(Exception e){
				// Continue waiting
				LOGGER.info("Waiting for queue to become ready, this exception is viewed as one that allows us to continue waiting");
			}			
		}
		
		/** If the Admin Client is not being shutdown, then we timed out **/
		if(!this.shutdown){
			throw new RuntimeException(String.format("Queue %s did was not ready after creation within %d Milliseconds", config.getName(), config.getQueueCreationTimeoutMS()));	
		}		
	}

	public void shutdown() {
		/** This unblocks the ensureQueueIsReady action on shutdown **/
		this.shutdown = true;
	}
}
