package io.microgenie.commands.core;

import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.FunctionCommands.FunctionCommand;
import io.microgenie.commands.core.Inputs.Input;
import io.microgenie.commands.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
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
public abstract class GenieContainer<R> implements GenieCommand<R>  {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenieContainer.class);
	
	private final List<GenieCommand<?>> parallelCommands = Lists.newArrayList();
	private final List<GenieCommand<?>> after = Lists.newArrayList();
	
	
	private final List<GenieInputCommand<R,?>> inputCommands = Lists.newArrayList();
	private final List<Func1<R,?>> inputFunctions = Lists.newArrayList();
	
	
    private static final Timeout DEFAULT_COMMAND_TIMEOUT_MS = Timeout.inMilliseconds(60 * 1000); // default command timeout in MS
    private Timeout timeout = DEFAULT_COMMAND_TIMEOUT_MS;
	
    private String key;
	private ListeningExecutorService executor;
	
	protected abstract void success(R result);
	protected abstract void failure(Throwable t);
	protected abstract R fallback();
	
	

	/**
	 * If the configuration for this command has not already been
	 * initialized it will be, lazily
	 */
	protected GenieContainer(final String key, ListeningExecutorService executor){
		this.key = key;
		this.executor = executor;
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
	public GenieCommand<R> inParallel(GenieCommand<?> command) {
		this.parallelCommands.add(command);
		return this;
	}
	
	@Override
	public GenieCommand<R> inParallel(List<GenieCommand<?>> commands) {
		this.parallelCommands.addAll(commands);
		return this;
	}
	@Override
	public <O> GenieCommand<R> asListInputTo(List<GenieInputCommand<R,O>> inputCommands) {
		this.inputCommands.addAll(inputCommands);
		return this;
	}
	
	public <O> GenieCommand<R> into(GenieInputCommand<R, O> inputCommand) {
		this.addInput(inputCommand);
		return this;
	}
	

	@Override
	public GenieCommand<R> before(GenieCommand<?> command) {
		this.after.add(command);
		return this;
	}
	
	

	/***
	 * Add input commands
	 * @param input
	 */
	protected void addInput(GenieInputCommand<R,?> input){
		this.inputCommands.add(input);
	}
	
	
	
	@Override
	public <O> GenieCommand<R> asInputTo(Func1<R,O> function){
		LOGGER.trace("adding function: {} as input command to container command with key: {}", function.getClass().getName(), this.getKey());
		this.inputFunctions.add(function);
		return this;
	}
	
	
	/**
	 * Executes asynchronously and blocks until execution has completed 
	 */
	@Override
	public R execute() throws TimeoutException, ExecutionException{
		final CommandResult<R> result = this.queue();
		return result.get();
	}
	
	
	/***
	 * Queue the command for asynchronous execution
	 */
	@Override
	public CommandResult<R> queue(){
		return this.queue(null);
	}
	

	
	
	/**
	 * Submit the appropriate task type
	 * @param input
	 * @return future
	 */
	@SuppressWarnings("unchecked")
	private <I> ListenableFuture<R>  submit(final I input){
		

		LOGGER.trace("submitting input:{} for commmandKey: {}", input, this.getKey());
		
		ListenableFuture<R> future;
		if(GenieInputCommand.class.isInstance(this)){
			future = ((GenieInputCommand<I,R>)this).submitTask(this.executor, input);
		}else if(FunctionCommand.class.isInstance(this)){
			final R output =  ((FunctionCommand<R>)this).run();
			future = Futures.immediateFuture(output);
		}else {
			future = ((RunnableCommand<R>)this).submitTask(this.executor);
		}
		return future;
	}
	
	/**
	 * If input is NOT null this method queues the current command with 
	 * the specified input parameter submitted to it's run method, {@link #run(Object)}
	 * <p>
	 * If input IS null, this method queues the current command with null
	 * from input which invokes the no argument run method {@link #run()}
	 */
	public <I> CommandResult<R> queue(I input){
		
		LOGGER.trace("queing command iwth input for commandKey: {}", this.getKey());
		
		CommandResult<R> finalResult;
		
		final ListenableFuture<R> future = this.submit(input);
		this.addCallback(future);
		final ListenableFuture<R> resultFuture = this.addFallback(future);
		
		
		/** Queue All Parallel commands and store results **/
		final List<CommandResult<?>> parallelResults = new ArrayList<CommandResult<?>>();
		if(CollectionUtil.hasElements(this.parallelCommands)){
			for(GenieCommand<?> command : this.parallelCommands){
				CommandResult<?> result = command.queue();
				parallelResults.add(result);
			}
		}

		
		/** check for dependent commands**/
		if( CollectionUtil.hasElements(this.inputCommands) || 
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
	private List<CommandResult<?>> processDependents(final R input) {
		
		
		/** output result list **/
		final List<CommandResult<?>> results = new ArrayList<CommandResult<?>>();
		
		
		/** Process Commands that should execute after this command **/
		if(CollectionUtil.hasElements(this.after)){
			for(GenieCommand<?> child : this.after){
				final CommandResult<?> childResult = child.queue();
				results.add(childResult);
			}			
		}
		
		
		/** Process dependent child commands that should receive output from the current command as input **/
		if(CollectionUtil.hasElements(this.inputCommands)){
			for(GenieInputCommand<R, ?> child : this.inputCommands){
				final CommandResult<?> childResult = child.queue(input);
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
