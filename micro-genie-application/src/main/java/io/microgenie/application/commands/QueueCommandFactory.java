package io.microgenie.application.commands;

import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.ProduceCommand;
import io.microgenie.application.queue.ProduceInputCommand;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.application.queue.QueueSpecs.DefaultQueueInputSpec;
import io.microgenie.commands.concurrency.ExecutorRegistry;
import io.microgenie.commands.core.CommandFactory;
import io.microgenie.commands.core.FunctionCommands.Func3;

import java.io.IOException;

/***
 * QueueFactory
 * @author shawn
 */
public class QueueCommandFactory extends CommandFactory {

	private final QueueFactory queueFactory;

	/**
	 * Create QueueCommandFactory - With a queueFactory and threadCommandFactory
	 * @param queues - {@link QueueFactory} - The underlying queueFactory implementation to use when the command is executed
	 */
	public QueueCommandFactory(final QueueFactory queues){
		this.queueFactory = queues;
	}
	
	

	/**
	 * 
	 * @param queueInputSpec - A specification used to produce messages from input to a particular queue
	 * @return produceInputCommand
	 */
	public <I> ProduceInputCommand<I> produce(DefaultQueueInputSpec<I> queueInputSpec) {
		return new ProduceInputCommand<I>(this.queueFactory.getProducer(), queueInputSpec.getQueue(), queueInputSpec.getToMessageFunction(), 
				ExecutorRegistry.INSTANCE.get(ProduceCommand.class.getName()));
	}
	
	
	/**
	 * 
	 * @param queue
	 * @param toMessage
	 * @return  producerCommand
	 */
	public <I> ProduceInputCommand<I> produce(final String queue, final ToMessageFunction<I> toMessage) {
		return new ProduceInputCommand<I>(this.queueFactory.getProducer(), queue, toMessage, ExecutorRegistry.INSTANCE.get(ProduceCommand.class.getName()));
	}

	
	
	/**
	 * Close resources
	 */
	@Override
	public void close() throws IOException {}


	public interface ToMessageFunction<I> extends Func3<String,String,I, Message>{}
}
