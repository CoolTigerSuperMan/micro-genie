package io.microgenie.commands.core;

import io.microgenie.commands.concurrency.ExecutorRegistry;
import io.microgenie.commands.core.FunctionCommands.Func0;
import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.FunctionCommands.Func2;
import io.microgenie.commands.core.FunctionCommands.Func3;
import io.microgenie.commands.core.FunctionCommands.Func4;
import io.microgenie.commands.core.FunctionCommands.Func5;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand0;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand1;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand2;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand3;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand4;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand5;
import io.microgenie.commands.core.Inputs.Input1;
import io.microgenie.commands.core.Inputs.Input2;
import io.microgenie.commands.core.Inputs.Input3;
import io.microgenie.commands.core.Inputs.Input4;
import io.microgenie.commands.core.Inputs.Input5;

import java.io.Closeable;


/**
 * Abstract Command Factory
 * @author shawn
 */
public abstract class CommandFactory implements Closeable {
	
	private static final String DEFAULT_FUNCTION_COMMAND = "default-function-command";
	
	
	public CommandFactory(){}

	



	/***
	 * Input parameters that will be available to the run command
	 * @param function
	 * @return functionCommand
	 */
	public <R> GenieContainer<R> withFunction(Func0<R> function){
		return new FunctionalCommand0<R>(function, DEFAULT_FUNCTION_COMMAND, ExecutorRegistry.INSTANCE.get(DEFAULT_FUNCTION_COMMAND));
	}
	public <A,R> GenieContainer<R> withFunction(Func1<A, R> function, Input1<A> input){
		return new FunctionalCommand1<A, R>(function,input, DEFAULT_FUNCTION_COMMAND, ExecutorRegistry.INSTANCE.get(DEFAULT_FUNCTION_COMMAND));
	}
	public <A, B, R> GenieContainer<R> withFunction(Func2<A, B, R> function, Input2<A,B> input){
		return new FunctionalCommand2<A, B, R>(function, input, DEFAULT_FUNCTION_COMMAND, ExecutorRegistry.INSTANCE.get(DEFAULT_FUNCTION_COMMAND));
	}
	public <A, B, C, R> GenieContainer<R> withFunction(Func3<A, B, C, R> function, Input3<A,B,C> input){
		return new FunctionalCommand3<A, B, C, R>(function, input, DEFAULT_FUNCTION_COMMAND, ExecutorRegistry.INSTANCE.get(DEFAULT_FUNCTION_COMMAND));
	}
	public <A, B, C, D, R> GenieContainer<R> withFunction(Func4<A, B, C, D, R> function, Input4<A, B, C, D> input){
		return new FunctionalCommand4<A, B, C, D, R>(function, input, DEFAULT_FUNCTION_COMMAND, ExecutorRegistry.INSTANCE.get(DEFAULT_FUNCTION_COMMAND));
	}
	public <A, B, C, D, E, R> GenieContainer<R> withFunction(Func5<A, B, C, D, E, R> function, Input5<A, B, C, D, E> input){
		return new FunctionalCommand5<A, B, C, D, E, R>(function, input, DEFAULT_FUNCTION_COMMAND, ExecutorRegistry.INSTANCE.get(DEFAULT_FUNCTION_COMMAND));
	}
	
	
	/**
	 * Used to invoke initialization for all command factory implementations
	 */
	public abstract void initialize();

}
