package io.microgenie.application.queue;

import io.microgenie.application.commands.QueueCommandFactory.ToMessageFunction;
import io.microgenie.commands.core.GenieInputCommand;
import io.microgenie.commands.core.Inputs.Input;

import java.util.UUID;

import com.google.common.util.concurrent.ListeningExecutorService;


/**
 * Producers submitted input from a chained command to a message queue 
 * @author shawn
 */
public class ProduceInputCommand<I> extends GenieInputCommand<I, String> {
	
	private final Producer producer;
	private final String queue;
	private final ToMessageFunction<I> toMessage;
	private String defaultValue;
	
	public ProduceInputCommand(final Producer producer, final String queue, final ToMessageFunction<I> toMessage, final ListeningExecutorService executor) {
		this(producer, queue, toMessage, executor, null);
	}
	/**
	 * ProducerCommand
	 * 
	 * @param producer
	 * @param message
	 * @param executor
	 * @param defaultValue
	 */
	protected ProduceInputCommand(final Producer producer, final String queue, final ToMessageFunction<I> toMessage, final ListeningExecutorService executor, final String defaultValue) {
		super(ProduceCommand.class.getName(), executor);
		this.producer = producer;
		this.queue = queue;
		this.toMessage = toMessage;
		this.defaultValue = defaultValue;	
	}


	/**
	 * 
	 */
	@Override
	public String run(I input) {
		final String messageId = UUID.randomUUID().toString();
		final Message message = toMessage.run(Input.with(this.queue, messageId, input));
		producer.submit(message);
		return message.getId();
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