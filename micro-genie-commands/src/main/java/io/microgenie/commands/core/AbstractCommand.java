package io.microgenie.commands.core;

import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.Inputs.Input;
import io.microgenie.commands.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.assertj.core.util.Lists;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.Uninterruptibles;



/**
 * An asynchronous command capable of executing work synchronously or
 * asynchronously - based on Guava ListenableFutures and modeled / inspired by netflix hystrix and Spotify Trickle 
 * @author shawn
 *
 * @param <R>
 */
public abstract class AbstractCommand<R> implements Command<R>  {

	private final List<Command<?>> parallelCommands = Lists.newArrayList();
	private final List<Command<?>> beforeCommands = Lists.newArrayList();
	private final List<Command<?>> inputCommands = Lists.newArrayList();
	private final List<Func1<R,?>> inputFunctions = Lists.newArrayList();
	
    private static final Timeout DEFAULT_COMMAND_TIMEOUT_MS = Timeout.inMilliseconds(60 * 1000); // default command timeout in MS
    private Timeout timeout = DEFAULT_COMMAND_TIMEOUT_MS;

	private String key;
	
	private ListeningExecutorService executor;
	
	protected abstract R run() ;
	protected abstract <I> R run(I input);
	
	protected abstract void success(R result);
	protected abstract void failure(Throwable t);
	protected abstract R fallback();
	
	

	
	/**
	 * If the configuration for this command has not already been
	 * initialized it will be, lazily
	 */
	protected AbstractCommand(final String key, ListeningExecutorService executor){
		this.key = key;
		this.executor = executor;
	}
	
	

	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#before(io.microgenie.commands.core.AbstractCommand)
	 */
	@Override
	public Command<R> before(Command<?> command){
		this.beforeCommands.add(command);
		return this;
	}
	
	
	
	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#asInputTo(io.microgenie.commands.core.AbstractCommand)
	 */
	@Override
	public Command<R> asInputTo(Command<?> command){
		this.inputCommands.add(command);
		return this;
	}
	
	
	@Override
	public <O> Command<R> asInputTo(Func1<R,O> function){
		this.inputFunctions.add(function);
		return this;
	}
	

	
	
	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#getTimeout()
	 */
	@Override
	public Timeout getTimeout() {
		return this.timeout;
	}
	
	
	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#getKey()
	 */
	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public Command<R> before(List<Command<?>> commands) {
		this.beforeCommands.addAll(commands);
		return this;
	}
	@Override
	public Command<R> asInputTo(List<Command<?>> commands) {
		this.inputCommands.addAll(commands);
		return this;
	}
	@Override
	public Command<R> inParallel(Command<?> command) {
		this.parallelCommands.add(command);
		return this;
	}
	@Override
	public Command<R> inParallel(List<Command<?>> commands) {
		this.parallelCommands.addAll(commands);
		return this;
	}
	
	
	
	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#execute()
	 */
	@Override
	public R execute() throws TimeoutException, ExecutionException  {
		try {
			return this.queue().get();
		} catch (RuntimeException ex){
			throw ex;
		}
	}
	

	
	/* (non-Javadoc)
	 * @see io.microgenie.commands.core.Command#queue()
	 */
	@Override
	public CommandResult<R> queue() {
		return this.queueWithInput(null);
	}
	
	
	/**
	 * If input is NOT null this method queues the current command with 
	 * the specified input parameter submitted to it's run method, {@link #run(Object)}
	 * <p>
	 * If input IS null, this method queues the current command with null
	 * from input which invokes the no argument run method {@link #run()}
	 */
	public <I> CommandResult<R> queueWithInput(I input){
		
		CommandResult<R> finalResult;
		
		final ListenableFuture<R> future = this.submitTask(input);
		final ListenableFuture<R> resultFuture = this.addFallback(future);
		final List<CommandResult<?>> parallelResults = new ArrayList<CommandResult<?>>();
		
		
		/** Queue All Parallel commands **/
		if(CollectionUtil.hasElements(this.parallelCommands)){
			for(Command<?> command : this.parallelCommands){
				CommandResult<?> result = command.queue();
				parallelResults.add(result);
			}
		}

		/** check for dependent commands**/
		if(	CollectionUtil.hasElements(this.beforeCommands) || 
			CollectionUtil.hasElements(this.inputCommands) || 
			CollectionUtil.hasElements(this.inputFunctions)){
			finalResult = this.getResultAndProcessDependents(resultFuture);
		}else{
			finalResult = CommandResult.create(resultFuture, this.getTimeout());	
		}
		
		/** Combine All results **/
		finalResult.getChildren().addAll(parallelResults);
		return finalResult;
	}
	
	

	/**
	 * Execute the {@link #run()}  or {@link #run(Object)} depending on wether or not the task should
	 * be submitted with input.
	 * 
	 * @return listenableFuture
	 */
	private <I>  ListenableFuture<R> submitTask(final I input) {
		
		Callable<R> callable = null;
		
		/** The the appropriate callable wrapper, depending whether the run method should receive input or not **/
		if(input==null){
			callable = new Callable<R>(){
				@Override
				public R call() throws Exception {
					return run();
				}
			};
		}else{
			callable = new Callable<R>(){
				@Override
				public R call() throws Exception {
					return run(input);
				}
			};
		}
		
		/** Submit the task with the appropriate callable wrapper**/
		final ListenableFuture<R> resultFuture = this.executor.submit(callable);
		this.addCallback(resultFuture);
		return resultFuture;
	}
	
	
	/**
	 * Because there are commands that are dependent upon this command, we have to 
	 * evaluate the result of this command first, then process all dependent child commands 
	 *  
	 * @param inputFuture
	 * @return CommandResult for this command
	 */
	private CommandResult<R> getResultAndProcessDependents(final ListenableFuture<R> inputFuture) {
		
		List<CommandResult<?>> commandResults = Lists.newArrayList();
		ListenableFuture<R> resultFuture = inputFuture;

		try {				
			
			final R result = Uninterruptibles.getUninterruptibly(resultFuture, this.timeout.getTimeout(), this.timeout.getUnit());
			resultFuture = Futures.immediateFuture(result);
			commandResults = this.processDependents(result);
			 
		} catch (ExecutionException e) {
			resultFuture = Futures.immediateFailedFuture(e);
		} catch (TimeoutException e) {
			resultFuture = Futures.immediateFailedCheckedFuture(e);
		}
		return CommandResult.create(resultFuture, this.getTimeout(), commandResults);
	}
	
	
	
	/**
	 * Process Dependent commands, commands dependent on the current command completing first
	 * @param input - Type R, the output from the current command
	 * @return dependents - command results
	 */
	private List<CommandResult<?>> processDependents(R input) {
		
		final List<CommandResult<?>> results = new ArrayList<CommandResult<?>>();
		/**  process child commands that should run after this one**/
		if(CollectionUtil.hasElements(this.beforeCommands)){
			for(Command<?> child : this.beforeCommands){
				final CommandResult<?> childResult = child.queue();
				results.add(childResult);
			}			
		}
		/** Process dependent child commands that should receive output from the current command as input **/
		if(CollectionUtil.hasElements(this.inputCommands)){
			for(Command<?> child : this.inputCommands){
				final CommandResult<?> childResult = child.queueWithInput(input);
				results.add(childResult);
			}			
		}
		
		/** Process Input Functions **/
		if(CollectionUtil.hasElements(this.inputFunctions)){
			for(Func1<R,?> child : this.inputFunctions){
				final Object result = child.run(Input.with(input));
				results.add(CommandResult.create(Futures.immediateFuture(result)));
			}			
		}
		return results;
	}
	
	
	
	/**
	 * Add a fallback if a fallback value was specified 
	 * @param future
	 */
	private ListenableFuture<R> addFallback(ListenableFuture<R> future) {
		final R fallback = this.fallback();
		if(fallback!=null){
			return Futures.withFallback(future, Fallback.create(fallback));	
		}
		return future;
	}
	
	
	/**
	 * Add the callback
	 * @param future
	 */
	private void addCallback(ListenableFuture<R> future) {
		Futures.addCallback(future, new FutureCallback<R>() {
			@Override
			public void onSuccess(R result) {	
				success(result);
			}
			@Override
			public void onFailure(Throwable t) {
				failure(t);
			}
		});
	}
	
	
	
	/**
	 * Create a default future fallback
	 * @return
	 */
	protected FutureFallback<R> getFallback(){
		return new Fallback<R>(this.fallback());
	}
	
	
	
	/**
	 * Close the internal executor if we created it
	 */
	public final void close(){
		/** Lose the reference  **/
		this.executor = null;
	}
}
