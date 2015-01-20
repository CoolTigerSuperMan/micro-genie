package io.microgenie.commands.core;

import io.microgenie.commands.core.Inputs.Input1;
import io.microgenie.commands.core.Inputs.Input2;
import io.microgenie.commands.core.Inputs.Input3;
import io.microgenie.commands.core.Inputs.Input4;
import io.microgenie.commands.core.Inputs.Input5;

import com.google.common.util.concurrent.ListeningExecutorService;


/***
 * Function Commands, capable of accepting variable strongly typed arguments
 * @author shawn
 *
 */
public class FunctionCommands {

	/**
	 * Base Functional Command with typed input parameters and typed
	 * output parameter
	 * 
	 * @author shawn
	 * @param <R>
	 */
	public abstract static class FunctionCommand<R> extends GenieContainer<R> {
		private final R fallback;
		
		protected FunctionCommand(final String key, ListeningExecutorService executor) {
			this(key, null, executor);
		}
		protected FunctionCommand(final String key, R defaultValue, ListeningExecutorService executor) {
			super(key, executor);
			this.fallback = defaultValue;
		}
		@Override
		protected void success(R result) {}
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected R fallback() {
			return fallback;
		}
		public Fallback<R> getFallback(){
			return new Fallback<R>(fallback);		
		}
		public abstract R run();
	}

	
	
	/**
	 * A Functional Command That has a return type but no input
	 * @author shawn
	 *
	 * @param <R>
	 */
	public static class FunctionalCommand0<R> extends FunctionCommand<R> {
		private Func0<R> function;
		public FunctionalCommand0(Func0<R> function, String key,ListeningExecutorService executor) {
			super(key, executor);
			this.function = function;
		}
		@Override
		public R run(){
			return this.function.run();
		}

	}
	
	/**
	 * A Functional Command accepting 1 typed input arguments
	 * @author shawn
	 *
	 * @param <R>
	 * @param <A>
	 */
	public static class FunctionalCommand1<A, R> extends FunctionCommand<R> {
		private Func1<A,R> function;
		private Input1<A> input;
		public FunctionalCommand1(Func1<A, R> function, Input1<A> input, final String key, ListeningExecutorService executor) {
			super(key, executor);
			this.function = function;
			this.input = input;
		}
		@Override
		public R run(){
			return this.function.run(this.input);
		}
	}
	
	
	/**
	 * A Functional Command accepting 2 typed input arguments
	 * @author shawn
	 *
	 * @param <R>
	 * @param <A>
	 * @param <B>
	 */
	public static class FunctionalCommand2<A, B, R> extends FunctionCommand<R> {
		private Func2<A,B, R> function;
		private Input2<A, B> input;
		public FunctionalCommand2(Func2<A,B, R> function, final Input2<A, B> input, final String key, ListeningExecutorService executor) {
			super(key, executor);
			this.function = function;
			this.input = input;
		}
		@Override
		public R run(){
			return this.function.run(this.input);
		}
	}
	
	
	/**
	 * A Functional Command accepting 3 typed input arguments
	 * @author shawn
	 *
	 * @param <R>
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 */
	public static class FunctionalCommand3<A, B, C, R> extends FunctionCommand<R> {
		private Func3<A,B, C, R> function;
		private Input3<A, B, C> input;
		public FunctionalCommand3(Func3<A,B, C, R> function, Input3<A,B,C> input, final String key, ListeningExecutorService executor) {
			super(key, executor);
			this.function = function;
			this.input = input;
		}
		@Override
		public R run(){
			return this.function.run(this.input);
		}
	}
	
	
	/**
	 * A Functional Command accepting 4 typed input arguments
	 * @author shawn
	 *
	 * @param <R>
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param <D>
	 */
	public static class FunctionalCommand4<A, B, C, D, R> extends FunctionCommand<R> {
		private Func4<A,B,C,D, R> function;
		private Input4<A, B, C, D> input;
		public FunctionalCommand4(Func4<A,B, C,D, R> function, final Input4<A,B,C,D> input, final String key, ListeningExecutorService executor) {
			super(key, executor);
			this.function = function;
			this.input = input;
		}
		@Override
		public R run(){
			return this.function.run(this.input);
		}
	}
	
	
	/**
	 * A Functional Command accepting 5 typed input arguments
	 * @author shawn
	 *
	 * @param <R>
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param <D>
	 * @param <E>
	 */
	public static class FunctionalCommand5< A, B, C, D, E, R> extends FunctionCommand<R> {
		private Func5<A,B,C,D,E, R> function;
		private Input5<A, B, C, D, E> input;
		public FunctionalCommand5(final Func5<A,B, C, D, E, R> function, final Input5<A,B,C,D,E> input, final String key, ListeningExecutorService executor) {
			super(key, executor);
			this.function = function;
			this.input = input;
		}
		@Override
		public R run(){
			return this.function.run(this.input);
		}
	}
	

	/**
	 * Function interfaces
	 */
	
	public interface Func<R> {}
	public interface Func0<R> extends Func<R> {
		public R run();
	}
	public  interface  Func1<A, R> extends Func<R> {
		public R run(Input1<A> input);
	}
	public interface Func2<A, B, R> extends Func<R> {
		public R run(Input2<A, B> input);
	}
	public interface Func3<A, B, C, R> extends Func<R> {
		public R run(Input3<A, B, C> input);
	}
	public interface Func4<A, B, C, D, R> extends Func<R> {
		public R run(Input4<A, B, C, D> input);
	}
	public interface Func5<A, B, C, D, E, R> extends Func<R> {
		public R run(Input5<A, B, C, D, E> input);
	}
}
