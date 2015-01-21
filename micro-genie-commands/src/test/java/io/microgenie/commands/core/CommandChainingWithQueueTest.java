package io.microgenie.commands.core;

import static org.junit.Assert.assertTrue;
import io.microgenie.commands.mocks.MockCommands;
import io.microgenie.commands.mocks.MockCommands.TestInputCommand;
import io.microgenie.commands.mocks.MockCommands.TestRunnableCommand;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;



/***
 * Tests the Command Chaining Features, such as 
 * command.before(new OtherCommand()).execute();
 * 
 * Where command should complete before other command is executed
 * @author shawn
 *
 */
public class CommandChainingWithQueueTest {
	

	
	/**
	 * The first command is expected to pass input into the second command, which means
	 * that the first command must complete and submit it's input into the second command 
	 * even though the queue() method is called which executes asynchronously 
	 * 
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test
	public void secondCommandShouldWaitAndReceiveOutputFromFirstCommandAsInput() throws TimeoutException, ExecutionException{
		
		
		/** First Command **/
		final TestRunnableCommand<Long> firstCommand = new TestRunnableCommand<Long>(MockCommands.MICHALE_JORDAN,"firstCommand");
			
		
		/** Second Command **/
		final TestInputCommand<Long, Long> secondCommand = new TestInputCommand<Long, Long> (0L, "secondCommand"){
			@Override
			public Long run(Long input){
				return  input;
			}
		};
		
		/** Execute the command chain **/
		CommandResult<Long> commandResults = firstCommand
				.into(secondCommand)
				.queue();
		
		/** Get the results **/
		final List<Long> results = commandResults.allResults();
		long firstCommandValue = results.get(0);
		long secondCommandValue = results.get(1);
		assertTrue(firstCommandValue == secondCommandValue);
		assertTrue(MockCommands.MICHALE_JORDAN == secondCommandValue);
	}
	
	
	

	
	/**
	 * An asynchronous command should complete before the next command is executed when asInputTo
	 * is called, and the first command should submit it's result to the second command. 
	 * 
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test
	public void parallelCommandsShouldExecuteOnSeparateThreads() throws TimeoutException, ExecutionException{
	
		/** First Command **/
		final TestRunnableCommand<Long> firstCommand = new TestRunnableCommand<Long>(0L,"firstCommand"){
			@Override
			public Long run(){
				return Thread.currentThread().getId();
			}
		};
		
		
		/** Second Command **/
		final TestRunnableCommand<Long> secondCommand = new TestRunnableCommand<Long>(0L, "secondCommand"){
			@Override
			public Long run(){
				return Thread.currentThread().getId();	
			}
		};

		
		/** Queuing chained commands in parallel should ensure they execute on separate threads **/
		final CommandResult<Long> commandResults = firstCommand
				.inParallel(secondCommand)
				.queue();
		

		final List<Long> results = commandResults.allResults();
		long firstCommandValue = results.get(0);
		long secondCommandValue = results.get(1);
		assertTrue(firstCommandValue != secondCommandValue);
		assertTrue(firstCommandValue > 0);
		assertTrue(secondCommandValue > 0);
	}
}
