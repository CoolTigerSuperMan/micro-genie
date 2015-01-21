package io.microgenie.commands.core;

import io.microgenie.commands.core.FunctionCommands.Func1;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/***
 * The GenieCommand interface is implemented by all genie commands
 * @author shawn
 *
 * @param <R>
 */
public interface GenieCommand<R> {

	
	/**
	 * The results from the current command will be used as input
	 * to the childCommmand parameter. 
	 * <p>
	 * This means that The current command
	 * will have to have results evaluated before the child commands can be 
	 * executed
	 * <p>
	 * @param inputCommand - Another command that will have this commands result provided as input before the provided command is executed
	 * @return thisCommand - as a fluent setter
	 */
	public <O> GenieCommand<R> into(final GenieInputCommand<R, O> inputCommand);
	
	public GenieCommand<R> before(final GenieCommand<?> command);
	
	

	/**
	 * 
	 * @param inputCommands - A list of input commands
	 * @return thisCommand 
	 */
	public abstract <O> GenieCommand<R> asListInputTo(final List<GenieInputCommand<R,O>> inputCommands);
	
	
	
	/**
	 *  
	 * @param function
	 * @return thisCommand - the current command
	 */
	public abstract <O> GenieCommand<R> asInputTo(final Func1<R,O> function);
	
	

	/**
	 * Run commands in parallel with this command
	 * @param command
	 * @return thisCommand
	 */
	public abstract GenieCommand<R> inParallel(final GenieCommand<?> command);
	
	
	
	/**
	 * Run commands in parallel with this command
	 * @param command - Command to be run in parallel
	 * @return thisCommand
	 */
	public abstract GenieCommand<R> inParallel(final List<GenieCommand<?>> command);



	/***
	 * The Timeout period in Milliseconds for this command
	 */
	public abstract Timeout getTimeout();

	
	/**
	 * The command key identifying this command
	 */
	public abstract String getKey();

	/**
	 * Execute the task synchronously
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	public abstract R execute() throws TimeoutException, ExecutionException;

	
	/**
	 * Submits the task asynchronously. 
	 * <p>
	 * 
	 * If an exception or timeout occurs and if a fallback value was 
	 * provided for the command then the fallback value is returned. 
	 * <p>
	 *  
	 * If an exception or timeout occurs and NO fallback value was provided
	 * for the command, then either a {@link TimeoutException} or {@link ExecutionException}
	 * exception are propagated upon calling:
	 * 
	 * <code>
	 * 	// TimeoutException or ExecutionException is propagated if no Command Fallback Value was provided
	 *  {@link CommandResult#get()}
	 * </code>
	 * @return result - {@link CommandResult}
	 */
	public abstract CommandResult<R> queue();
}
