package io.microgenie.commands.core;

import io.microgenie.commands.core.FunctionCommands.Func1;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface Command<R> {

	/**
	 * If a command is provided, this command will have it's
	 * results evaluated before invoking the child commands
	 * 
	 * @param command - A child command to be executed after this commands results have been evaluated
	 * @return thisCommand
	 */
	public abstract Command<R> before(Command<?> command);
	
	
	/***
	 * 
	 * @param commands
	 * @return
	 */
	public abstract Command<R> before(List<Command<?>>  commands);

	
	
	/**
	 * The results from the current command will be used as input
	 * to the childCommmand parameter. 
	 * <p>
	 * This means that The current command
	 * will have to have results evaluated before the child commands can be 
	 * executed
	 * <p>
	 * @param command - Another command that will have this commands result provided as input before the provided command is executed
	 * @return thisCommand - as a fluent setter
	 */
	public abstract Command<R> asInputTo(Command<?> command);
	
	
	
	/**
	 *  
	 * @param function
	 * @return the current command
	 */
	public abstract <O> Command<R> asInputTo(Func1<R,O> function);
	
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	public abstract Command<R> asInputTo(List<Command<?>> command);
	

	/**
	 * Run commands in parallel with this command
	 * @param command
	 * @return thisCommand
	 */
	public abstract Command<R> inParallel(Command<?> command);
	
	
	
	/**
	 * Run commands in parallel with this command
	 * @param command - Command to be run in parallel
	 * @return thisCommand
	 */
	public abstract Command<R> inParallel(List<Command<?>> command);



	
	
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
	
	
	/***
	 * Used to chain commands together, where a command should be executed with the output result from a previous command execution as it's input
	 * @param input
	 * @return result - {@link Command}
	 */
	public abstract <I> CommandResult<R> queueWithInput(I input);


	

	

}