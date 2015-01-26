package io.microgenie.application.commands;

import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventFactory;
import io.microgenie.commands.concurrency.ExecutorRegistry;
import io.microgenie.commands.concurrency.ThreadCommandFactory;
import io.microgenie.commands.core.CommandFactory;
import io.microgenie.commands.core.FunctionCommands.Func2;
import io.microgenie.commands.core.GenieInputCommand;
import io.microgenie.commands.core.GenieRunnableCommand;
import io.microgenie.commands.core.Inputs.Input;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListeningExecutorService;


/***
 * Event Commands
 * @author shawn
 *
 */
public class EventCommandFactory extends CommandFactory{

	private final EventFactory events;
	
	
	public EventCommandFactory(final EventFactory events) {
		this.events = events;
	}
	public PublishCommand submit(final Event event){
		return new PublishCommand(events, event, event.getTopic(), ExecutorRegistry.INSTANCE.get(event.getTopic()));
	}
	public <I> PublishInputCommand<I> submit(final String topic, final ToEventFunction<I> toEventFunction){
		return new PublishInputCommand<I>(this.events, topic, toEventFunction, topic, ExecutorRegistry.INSTANCE.get(topic));
	}

	
	/***
	 * If the {@link ThreadCommandFactory} was created by this instance it will be closed
	 */
	@Override
	public void close() throws IOException {}

	
	public interface ToEventFunction<I> extends Func2<String, I, Event>{}
	
	
	
	public static class PublishCommand extends GenieRunnableCommand<Event>{
		private final EventFactory events;
		private final Event event;
		public PublishCommand(final EventFactory events, final Event event, String key, ListeningExecutorService executor) {
			super(key, executor);
			this.events = events;;
			this.event = event;
		}
		@Override
		public Event run() throws ExecutionException {
			this.events.publish(event);
			return event;
		}		
	}
	
	
	/**
	 * Publish the input to the desired topic
	 * @author shawn
	 *
	 * @param <I>
	 */
	public static class PublishInputCommand<I> extends GenieInputCommand<I, Event>{
		private final EventFactory events;
		private final ToEventFunction<I> toEvent;
		private final String topic;
		public PublishInputCommand(final EventFactory events, final String topic, ToEventFunction<I> toEvent, String key, ListeningExecutorService executor) {
			super(key, executor);
			this.events = events;
			this.topic = topic;
			this.toEvent = toEvent;
		}		
		@Override
		protected Event run(I input) throws ExecutionException {
			final Event event = this.toEvent.run(Input.with(this.topic, input));
			this.events.publish(event);
			return event;
		}
	}
}
