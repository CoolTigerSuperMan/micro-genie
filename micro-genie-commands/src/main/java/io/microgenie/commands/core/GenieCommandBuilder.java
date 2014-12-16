package io.microgenie.commands.core;
//package io.microgenie.commands.core;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeoutException;
//
//import com.google.common.util.concurrent.ListeningExecutorService;
//import io.microgenie.commands.core.FunctionCommands.Func1;
//import io.microgenie.commands.core.Inputs.Input;
//
//
///**
// * Genie command builder
// * @author shawn
// */
//public class GenieCommandBuilder {
//
//	private Input input;
//	private List<Command<?>> beforeCommands = new ArrayList<Command<?>>();
//	private List<Command<?>> inputCommands = new ArrayList<Command<?>>();
//	private List<Command<?>> parallelCommands = new ArrayList<Command<?>>();
//	
//	
//	/**
//	 * Build the command
//	 * @param function - The function to execute client logic
//	 * @param key - The command key
//	 * @param executor
//	 * @return command
//	 */
//	public <A, R> Command<R> build(final Func1<A,R> function, final String key, final ListeningExecutorService executor){
//		final Command<R> command = new FunctionCommands.FunctionalCommand1<R,A>(function, key, executor);
//		command.before(this.beforeCommands);
//		command.asInputTo(inputCommands);
//		command.withInput(input);
//		command.inParallel(this.parallelCommands);
//		return command;
//	}
//	
//	
//	
//	/**
//	 * Execute the command with the provided input
//	 * @param input
//	 * @return thisBuilder
//	 */
//	public GenieCommandBuilder withInput(Input input){
//		this.input = input;
//		return this;
//	}
//	
//	
//	
//	
//	/***
//	 * Provide the current command as input to the provided command
//	 * @param command
//	 * @return thisBuilder
//	 */
//	public GenieCommandBuilder asInputTo(Command<?> command){
//		this.inputCommands.add(command);
//		return this;
//	}
//	
//	
//	/**
//	 * Execute before the provided command
//	 * @param command
//	 * @return thisBuilder
//	 */
//	public GenieCommandBuilder before(Command<?> command){
//		this.beforeCommands.add(command);
//		return this;
//	}
//	
//	/**
//	 * Command to be executed in parallel with the current command
//	 * @param command
//	 * @return thisBuilder
//	 */
//	public GenieCommandBuilder inParallel(Command<?> command){
//		this.parallelCommands.add(command);
//		return this;
//	}
//	
//	
//	/***
//	 * Create a new Builder for this command
//	 * @return newCommandBuilder
//	 */
//	public static GenieCommandBuilder newBuilder(){
//		return new GenieCommandBuilder();
//	}
//	
//	
//	public static void main(String[] args) throws TimeoutException, ExecutionException{
//		
//		GenieCommandBuilder b = 
//				GenieCommandBuilder
//				.newBuilder()
//				.withInput(Input.with(null))
//				.inParallel(null);
//		
//		Command<String> command = b.build(null, null, null);
//		CommandResult<String> result  = command.queue();
//		
//		final String resultString = result.get();
//	}
//}
