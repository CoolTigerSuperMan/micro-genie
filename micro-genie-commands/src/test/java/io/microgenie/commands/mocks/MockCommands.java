package io.microgenie.commands.mocks;

import io.microgenie.commands.core.AbstractCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;


public class MockCommands {
	
	

	/** The successful value **/
	public static final String REAL_VALUE = "realValue";
	public static final String COMMAND_KEY = "commandKey";
	public static final String FALL_BACK_VALUE = "fallbackValue";
	public static final String EXCEPTION_MESSAGE = "Test Error";
	
	
	
	/**
	 * 
	 * Mock Command Objects
	 * 
	 */

	
	
	/**
	 * Base Test command
	 * @author shawn
	 */
	public static class TestCommand<T> extends AbstractCommand<T>{
		private T value;
		private T defaultValue;
		
		public  TestCommand(final T value, final String key) {
			this(value, key, null);
		}
		public  TestCommand(final T value, final String key, final T defaultValue) {
			this(value, key, defaultValue, MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
		}
		protected TestCommand(final T value, final String key, final T defaultValue,final ListeningExecutorService executor) {
			super(key, executor);
			this.value = value;
			this.defaultValue = defaultValue;
		}
		@Override
		protected T run() {return value;}
		@Override
		protected void success(T result) {}
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected T fallback() {return defaultValue;}
		@SuppressWarnings("unchecked")
		@Override
		protected <I> T run(I input){return (T)input;}
	}
	
	
	
	
	/**
	 * Test Command to capture callbacks for success and failures
	 * @author shawn
	 */
	public static class TestCommandWithCallBacks<T> extends TestCommand<T>{
		private final List<T> successes = new ArrayList<T>();
		private final List<Throwable> errors = new ArrayList<Throwable>();
		public TestCommandWithCallBacks(T value, String key) {
			super(value, key);
		}
		public TestCommandWithCallBacks(final T value, final String key, final T defaultValue) {
			super(value, key, defaultValue);
		}
		@Override
		protected void failure(Throwable t) {
			System.err.println(t.getMessage());
			this.errors.add(t);
		}
		@Override
		protected void success(T result) {
			this.successes.add(result);
		}
		public List<T> getSuccesses() {
			return successes;
		}
		public List<Throwable> getErrors() {
			return errors;
		}
	}

}
