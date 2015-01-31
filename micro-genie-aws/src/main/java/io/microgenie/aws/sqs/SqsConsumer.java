package io.microgenie.aws.sqs;

import io.microgenie.application.queue.Consumer;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.aws.admin.SqsQueueAdmin;
import io.microgenie.aws.config.SqsConsumerConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;



/***
 * An SQS implementation of the {@link Consumer} interface.
 * Used to consume messages from AWS SQS queue.
 * 
 * @author shawn
 */
public class SqsConsumer implements Consumer{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SqsConsumer.class);
	
	private final AmazonSQSClient client;
	private final SqsQueueAdmin queueAdmin;
	private final SqsConsumerConfig config;
	
	private final List<Poller> pollers;
	private final ExecutorService executor;

	private final AtomicBoolean running = new AtomicBoolean();
	
	
	/***
	 * Constructor for SQSConsumer
	 * 
	 * @param client
	 * @param admin
	 * @param config
	 */
	public SqsConsumer(final AmazonSQSClient client, final SqsQueueAdmin admin, final SqsConsumerConfig config)  {
		this.client = Preconditions.checkNotNull(client, "AmazonSQSClient cannot be null");
		this.queueAdmin = Preconditions.checkNotNull(admin, "SqsQueueAdmin cannot be null");
		this.config = Preconditions.checkNotNull(config, "SqsConsumerConfig cannot be null");;
		this.pollers = this.createPollers(config);
		this.executor = Executors.newFixedThreadPool(this.pollers.size());
	}
	
	
	
	/***
	 * Create runnable {@link Poller} list to consume from sqs
	 * where the count of poller are equal to config thread count
	 * 
	 * @param config
	 * @return pollerList
	 */
	private List<Poller> createPollers(final SqsConsumerConfig config){
		final List<Poller>  pollers = Lists.newArrayList();
		final String url = this.queueAdmin.getQueueUrl(config.getQueue());
		MessageHandler handler = null;
		try{
			  handler = config.createHandler();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}	
		for(int i =0; i < config.getThreads();  i++){
			pollers.add(new Poller(config.getQueue(), url, handler, this.client, this.running));
		}
		return pollers;
	}
	
	
	
	/***
	 * Start the sqs consumer
	 */
	@Override
	public void start() {
		if(!this.running.getAndSet(true)){
			for(Poller poller : this.pollers){
				this.executor.execute(poller);
			}			
			LOGGER.info("{} consuming threads have been started for queue: {}", this.config.getThreads(), this.config.getQueue());
		}else{
			LOGGER.info("consumer for queue: {} is already running - thread count: {}. This call has no effect", this.config.getQueue(), this.config.getThreads());	
		}
	}

	
	/***
	 * Stop the consumer, which includes all running consuming threads
	 */
	@Override
	public void stop() {
		if(this.running.getAndSet(false)){
			try {
				LOGGER.info("waiting {} milliseconds for SQS consumer threads to shutdown", config.getShutdownTimeMS());
				if(!this.executor.awaitTermination(config.getShutdownTimeMS(),TimeUnit.MILLISECONDS)){
					this.executor.shutdownNow();
				}
			} catch (Exception e) {		
				this.executor.shutdownNow();
			}
			LOGGER.info("{} consuming threads have been stopped for queue: {}", this.config.getThreads(), this.config.getQueue());
		}
	}


	/**
	 * If true, the consumer is running, polling messages
	 */
	@Override
	public boolean isRunning() {
		return this.running.get();
	}
}


