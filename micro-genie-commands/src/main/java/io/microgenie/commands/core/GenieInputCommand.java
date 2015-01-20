package io.microgenie.commands.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;


/**
 * 
 * @author shawn
 *
 * @param <O>
 */
public abstract class GenieInputCommand<I, O> extends GenieContainer<O>  implements InputCommand<I, O>{

	private static final Logger LOGGER = LoggerFactory.getLogger(GenieInputCommand.class);
	protected GenieInputCommand(String key, ListeningExecutorService executor) {
		super(key, executor);
	}
	
	
	protected abstract O run(I Input) throws ExecutionException;
	
	
	/***
	 * Submit the task with input
	 */
	@Override
	public ListenableFuture<O> submitTask(final ListeningExecutorService executor, final I input) {
		final Callable<O> callable =  new Callable<O>(){
			@Override
			public O call() throws Exception {
				return run(input);
			}
		};
		/** Submit the task with the appropriate callable wrapper**/
		final ListenableFuture<O> resultFuture = executor.submit(callable);
		return resultFuture;
	}


	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#execute()
	 */
	@Override
	public O execute() throws TimeoutException, ExecutionException  {
		try {
			LOGGER.trace("executing command with key: {}", this.getKey());
			return this.queue().get();
		} catch (RuntimeException ex){
			throw ex;
		}
	}
	
	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#asInputTo(io.microgenie.commands.core.GenieContainer)
	 */
	
	public <R> GenieInputCommand<I,O> into(GenieInputCommand<O,R> command){
		LOGGER.trace("adding input command {} to container command with key: {}", command.getKey(), this.getKey());
		super.addInput(command);
		return this;
	}
	
	@Override
	public <R> GenieCommand<O> asListInputTo(List<GenieInputCommand<O,R>> inputCommands) {
		return super.asListInputTo(inputCommands);
	}
	
	
	@Override
	protected void success(O result) {
	}
	@Override
	protected void failure(Throwable t) {
	}
	@Override
	protected O fallback() {
		return null;
	}
}
