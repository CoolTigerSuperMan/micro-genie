package io.microgenie.application.queue;

import io.microgenie.commands.core.GenieRunnableCommand;

import com.google.common.util.concurrent.ListeningExecutorService;


/**
 * 
 * @author shawn
 *
 */
public class ProduceCommand extends GenieRunnableCommand<String> {
	
	private Producer producer;
	private Message message;
	private String defaultValue;
	protected ProduceCommand(Producer producer, Message message,ListeningExecutorService executor) {
		this(producer, message, executor, null);
	}
	
	/**
	 * ProducerCommand
	 * @param producer
	 * @param message
	 * @param executor
	 * @param defaultValue
	 */
	protected ProduceCommand(Producer producer, Message message,ListeningExecutorService executor, String defaultValue) {
		super(ProduceCommand.class.getName(), executor);
		this.producer = producer;
		this.message = message;
		this.defaultValue = defaultValue;
	}
	@Override
	public String run() {
		producer.submit(message);
		throw new RuntimeException();
	}
	@Override
	protected void success(String result) {}
	@Override
	protected void failure(Throwable t) {}
	@Override
	protected String fallback() {
		return defaultValue;
	}
	public void put(Message message){
		this.producer.submit(message);
	}
}
