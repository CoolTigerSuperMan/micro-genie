package io.microgenie.aws.config;

import io.microgenie.application.queue.MessageHandler;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sqs Consumer Configuration
 * @author shawn
 */
public class SqsConsumerConfig{
	private int threads = 1; 	//Default to a thread count of one for consumers
	private String queue;
	private String handlerKey;
	private int shutdownTimeMS = 2000;
	private MessageHandler handlerInstance;
	
	@JsonProperty("queue")
	public String getQueue() {
		return queue;
	}
	@JsonProperty("queue")
	public void setQueue(String queue) {
		this.queue = queue;
	}
	public SqsConsumerConfig withQueue(final String queue) {
		this.queue = queue;
		return this;
	}
	
	@JsonProperty("threads")
	public int getThreads() {
		return threads;
	}
	@JsonProperty("threads")
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public SqsConsumerConfig withThreads(final int threads) {
		this.threads = threads;
		return this;
	}
	
	
	@JsonProperty("handler")
	public String getHandlerKey() {
		return handlerKey;
	}
	@JsonProperty("handler")
	public void setHandlerKey(String handler) {
		this.handlerKey = handler;
	}

	
	
	@JsonProperty("shutdownWaitMs")
	public int getShutdownTimeMS() {
		return shutdownTimeMS;
	}
	@JsonProperty("shutdownWaitMs")
	public void setShutdownTimeMS(int shutdownTimeMS) {
		this.shutdownTimeMS = shutdownTimeMS;
	}
	public SqsConsumerConfig withHandler(final int shutdownTimeMS) {
		this.shutdownTimeMS = shutdownTimeMS;
		return this;
	}
	
	public MessageHandler getHandlerInstance() {
		return handlerInstance;
	}
	public void setHandlerInstance(MessageHandler handlerInstance) {
		this.handlerInstance = handlerInstance;
	}
	public SqsConsumerConfig withHandler(final MessageHandler handler) {
		this.handlerInstance = handler;
		return this;
	}
	
	/***
	 * 
	 * @throws ClassNotFoundException
	 */
	public MessageHandler createHandler() throws ClassNotFoundException{
		if(this.handlerInstance==null){
			@SuppressWarnings("unchecked")
			Class<MessageHandler> handler = (Class<MessageHandler>)Class.forName(handlerKey);
			try{
				this.handlerInstance = handler.newInstance();	
			}catch(Exception ex){
				throw new RuntimeException(ex.getMessage(), ex);
			}	
		}
		return this.handlerInstance;
	}
	
	
	
	/***
	 * Makes a new copy of the configuration
	 * @return copiedConfig
	 */
	public SqsConsumerConfig copy() {
		final SqsConsumerConfig config = new SqsConsumerConfig();
		config.setHandlerInstance(this.handlerInstance);
		config.setHandlerKey(this.handlerKey);
		config.setQueue(this.queue);
		config.setThreads(this.threads);
		config.setShutdownTimeMS(this.shutdownTimeMS);
		return config;
	}
}