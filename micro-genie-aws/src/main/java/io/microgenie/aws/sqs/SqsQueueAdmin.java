package io.microgenie.aws.sqs;

import io.microgenie.aws.SqsQueueConfig;
import io.microgenie.commands.util.CollectionUtil;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.google.common.base.Strings;

/***
 * Used to administer SQS Queues
 * @author shawn
 */
public class SqsQueueAdmin {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SqsQueueAdmin.class);
	
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
				/** not retriable **/
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
