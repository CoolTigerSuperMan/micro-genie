package io.microgenie.commands.mocks;

import io.microgenie.commands.core.GenieInputCommand;

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
	public static class TestCommand<I,O> extends GenieInputCommand<I,O>{

		private O value;
		private O defaultValue;
		
		public  TestCommand(final O value, final String key) {
			this(value, key, null);
		}
		public  TestCommand(final O value, final String key, final O defaultValue) {
			this(value, key, defaultValue, MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
		}
		protected TestCommand(final O value, final String key, final O defaultValue, final ListeningExecutorService executor) {
			super(key, executor);
			this.value = value;
			this.defaultValue = defaultValue;
		}
		@Override
		protected void success(O result) {}
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected O fallback() {return defaultValue;}
		@Override
		public O run(I input) {
			return this.value;
		}
	}
	
	
	/**
	 * Base Test command
	 * @author shawn
	 */
	public static class TestInputCommand<I,O> extends GenieInputCommand<I,O>{
		private O value;
		private O defaultValue;
		
		public  TestInputCommand(final O value, final String key) {
			this(value, key, null);
		}
		public  TestInputCommand(final O value, final String key, final O defaultValue) {
			this(value, key, defaultValue, MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
		}
		protected TestInputCommand(final O value, final String key, final O defaultValue,final ListeningExecutorService executor) {
			super(key, executor);
			this.value = value;
			this.defaultValue = defaultValue;
		}
		@Override
		protected O run(I Input) {
			return value; //Mock Value
		}
		@Override
		protected void success(O result) {}
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected O fallback() {
			return this.defaultValue;
		}	
	}
	
	
	
	
	/**
	 * Test Command to capture callbacks for success and failures
	 * @author shawn
	 */
	public static class TestCommandWithCallBacks<I,O> extends TestCommand<I,O>{
		private final List<O> successes = new ArrayList<O>();
		private final List<Throwable> errors = new ArrayList<Throwable>();
		public TestCommandWithCallBacks(O value, String key) {
			super(value, key);
		}
		public TestCommandWithCallBacks(final O value, final String key, final O defaultValue) {
			super(value, key, defaultValue);
		}
		@Override
		protected void failure(Throwable t) {
			System.err.println(t.getMessage());
			this.errors.add(t);
		}
		@Override
		protected void success(O result) {
			this.successes.add(result);
		}
		public List<O> getSuccesses() {
			return successes;
		}
		public List<Throwable> getErrors() {
			return errors;
		}
	}

}
