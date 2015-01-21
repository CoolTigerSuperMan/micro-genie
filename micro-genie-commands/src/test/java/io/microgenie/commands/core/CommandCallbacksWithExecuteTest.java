package io.microgenie.commands.core;

import io.microgenie.commands.mocks.MockCommands;
import io.microgenie.commands.mocks.MockCommands.TestInputCommand;
import io.microgenie.commands.mocks.MockCommands.TestCommandWithCallBacks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;



/***
 * Tests for Command Callbacks and Fallback Values
 * 
 * @author shawn
 *
 */
public class CommandCallbacksWithExecuteTest {

	/**
	 * Command Should Return Fallback Value on Error but not throw an exception
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test
	public void shouldNotThrowExceptionWhenFallbackIsSupplied() throws TimeoutException, ExecutionException{
		
		/** Command that throws exception  **/
		final TestInputCommand<String, String> commandWithException = new TestInputCommand<String, String>(MockCommands.REAL_VALUE, MockCommands.COMMAND_KEY, MockCommands.FALL_BACK_VALUE){
			public String run(String input) {
				throw new RuntimeException(MockCommands.EXCEPTION_MESSAGE);
			}
		};
		final String value = commandWithException.execute();
		Assert.assertTrue(MockCommands.FALL_BACK_VALUE.equals(value));
	}
	
	
	
	
	/**
	 * Command callback should be triggered for exception and also return fallback value
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test
	public void shouldTriggerFailureCallbackButReturnDefaultValue() throws TimeoutException, ExecutionException{
		
		/** Command that throws exception  **/
		final TestCommandWithCallBacks<String, String> commandWithException = new TestCommandWithCallBacks<String, String>(MockCommands.REAL_VALUE, MockCommands.COMMAND_KEY, MockCommands.FALL_BACK_VALUE){
			public String run(String input) {
				throw new RuntimeException(MockCommands.EXCEPTION_MESSAGE);
			}
		};
		
		final String value = commandWithException.execute();
		Assert.assertTrue(MockCommands.FALL_BACK_VALUE.equals(value));
		Assert.assertTrue(commandWithException.getErrors().size()==1);
		Assert.assertTrue(commandWithException.getSuccesses().size()==0);
		Assert.assertTrue(commandWithException.getErrors().get(0).getMessage().equals(MockCommands.EXCEPTION_MESSAGE));
	}
	
	
	
	/**
	 * Command callback should trigger callback for success and also return expected runtime value
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test
	public void shouldTriggerSuccessCallBackAndReturnExpectedRunTimeValue() throws TimeoutException, ExecutionException{
		
		final TestCommandWithCallBacks<String, String> commandWithCallbacks = new TestCommandWithCallBacks<String, String>(MockCommands.REAL_VALUE, MockCommands.COMMAND_KEY,  MockCommands.FALL_BACK_VALUE);
		final String value = commandWithCallbacks.execute();
		Assert.assertTrue(MockCommands.REAL_VALUE.equals(value));
		Assert.assertTrue(commandWithCallbacks.getErrors().size()==0);
		Assert.assertTrue(commandWithCallbacks.getSuccesses().size()==1);
		Assert.assertTrue(commandWithCallbacks.getSuccesses().get(0).equals(MockCommands.REAL_VALUE));
	}
	
	
	
	

	/**
	 * Command Should throw Exception when now Fallback Value is specified
	 * @throws Exception 
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test(expected=ExecutionException.class)
	public void shouldInvokeExceptionWhenNoFallBackValueIsSupplied() throws Exception {
		
		/** Command that throws exception  **/
		final TestCommandWithCallBacks<String, String> commandWithException = new TestCommandWithCallBacks<String, String>("realValue", "commandKey"){
			public String run(final String input) {
				throw new RuntimeException(MockCommands.EXCEPTION_MESSAGE);
			}
		};
		
		try{
			commandWithException.execute();	
		}catch(ExecutionException ex){
			Assert.assertTrue("Expected Success Was: 0",commandWithException.getSuccesses().size()==0);
			Assert.assertTrue("Expected Errors Were: 1", commandWithException.getErrors().size()==1);
			throw ex;
		}
	}
}
