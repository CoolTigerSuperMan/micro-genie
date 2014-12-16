package io.microgenie.commands.core;

import io.microgenie.commands.mocks.MockCommands;
import io.microgenie.commands.mocks.MockCommands.TestCommand;
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
		final TestCommand<String> commandWithException = new TestCommand<String>(MockCommands.REAL_VALUE, MockCommands.COMMAND_KEY, MockCommands.FALL_BACK_VALUE){
			protected String run() {
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
		final TestCommandWithCallBacks<String> commandWithException = new TestCommandWithCallBacks<String>(MockCommands.REAL_VALUE, MockCommands.COMMAND_KEY, MockCommands.FALL_BACK_VALUE){
			protected String run() {
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
		
		final TestCommandWithCallBacks<String> commandWithCallbacks = new TestCommandWithCallBacks<String>(MockCommands.REAL_VALUE, MockCommands.COMMAND_KEY,  MockCommands.FALL_BACK_VALUE);
		final String value = commandWithCallbacks.execute();
		Assert.assertTrue(MockCommands.REAL_VALUE.equals(value));
		Assert.assertTrue(commandWithCallbacks.getErrors().size()==0);
		Assert.assertTrue(commandWithCallbacks.getSuccesses().size()==1);
		Assert.assertTrue(commandWithCallbacks.getSuccesses().get(0).equals(MockCommands.REAL_VALUE));
	}
	
	
	
	

	/**
	 * Command Should throw Exception when now Fallback Value is specified
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	@Test(expected=ExecutionException.class)
	public void shouldInvokeExceptionWhenNoFallBackValueIsSupplied() throws TimeoutException, ExecutionException{
		
		/** Command that throws exception  **/
		final TestCommandWithCallBacks<String> commandWithException = new TestCommandWithCallBacks<String>("realValue", "commandKey"){
			protected String run() {
				throw new RuntimeException("Test Error");
			}
		};
		
		try{
			commandWithException.execute();	
		}catch(Exception ex){
			Assert.assertTrue("Expected Success Was: 0",commandWithException.getSuccesses().size()==0);
			Assert.assertTrue("Expected Errors Were: 1", commandWithException.getErrors().size()==1);
			throw ex;
		}
	}
}
