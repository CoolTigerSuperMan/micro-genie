package io.microgenie.commands.core;

import io.microgenie.commands.core.Functions.MapResults;
import io.microgenie.commands.core.Functions.ReduceFunction;
import io.microgenie.commands.core.Functions.TransformFunction;
import io.microgenie.commands.core.Inputs.Input;
import io.microgenie.commands.core.Inputs.Input1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.assertj.core.util.Lists;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;


/**
 * A command result, which is used to return 
 * results from asynchronous command execution
 * 
 * @author shawn
 *
 * @param <R>
 */
public class CommandResult<R> {

	private final Timeout timeout;
	private final ListenableFuture<R> future;
	private List<CommandResult<?>> children = new ArrayList<CommandResult<?>>();

	protected CommandResult(ListenableFuture<R> future){
		this(future, null, Lists.<CommandResult<?>>newArrayList());
	}
	protected CommandResult(ListenableFuture<R> future, Timeout timeout) {
		this(future, timeout, Lists.<CommandResult<?>>newArrayList());
	}
	protected CommandResult(ListenableFuture<R> future, Timeout timeout, List<CommandResult<?>> children) {
		this.future = future;
		this.timeout = timeout;
		this.children = children;
	}
	
	
	/**
	 * Get the result
	 * 
	 * @return returnValue
	 * @throws ExecutionException 
	 */
	public R get() throws TimeoutException, ExecutionException {
		try {
			if(this.future==null){
				return null;
			}
			if(timeout==null){
				return Uninterruptibles.getUninterruptibly(this.future);
			}else{
				return Uninterruptibles.getUninterruptibly(future, timeout.getTimeout(), timeout.getUnit());
			}
		}catch (ExecutionException e){
			throw e;
		} catch (TimeoutException t){
			throw t;
		}
	}
	
	
	public List<CommandResult<?>> getChildren() {
		return children;
	}
	
	
	/**
	 * Create a command result which gets returned to the caller
	 * 
	 * @param future - {@link Future} - to retrieve results
	 * @return result - A newly created {@link CommandResult} instance
	 */
	public static <R> CommandResult<R> create(final ListenableFuture<R> future){
		return new CommandResult<R>(future);
	}
	
	
	/**
	 * Create a command result which gets returned to the caller
	 * 
	 * @param future - {@link Future} - to retrieve results
	 * @param timeout - {@link Timeout} - the command timeout value
	 * @return result - A newly created {@link CommandResult} instance
	 */
	public static <R> CommandResult<R> create(final ListenableFuture<R> future, final Timeout timeout){
		return create(future, timeout, Lists.<CommandResult<?>>newArrayList());
	}
	
	
	/**
	 * Create a command result which gets returned to the caller
	 * 
	 * @param future - {@link Future} - to retrieve results
	 * @param timeout - {@link Timeout} - the command timeout value
	 * @param children - {@link CommandResult<?>} childResults Dependent on completion of this result
	 * @return result - A newly created {@link CommandResult} instance
	 */
	public static <R> CommandResult<R> create(final ListenableFuture<R> future, final Timeout timeout, final List<CommandResult<?>> childResults){
		Preconditions.checkNotNull(future, "Future is required");
		Preconditions.checkNotNull(timeout, "Timeout is required");
		return new CommandResult<R>(future, Timeout.create(timeout.getTimeout(), timeout.getUnit()), childResults);
	}

	/***
	 * Internal Transform method 
	 * @param input - a listenable future of type I for the input
	 * @param function - async function meant to perform the actual transform
	 * @return output - The transformed output value
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	public R transform(TransformFunction<R,R> function) throws TimeoutException, ExecutionException{
		return this.transform(this, function);
	}
	
	
	/***
	 * Internal Transform method 
	 * @param input - a listenable future of type I for the input
	 * @param function - async function meant to perform the actual transform
	 * @return output - The transformed output value
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private  <I,O> O transform(CommandResult<I> input, AsyncFunction<CommandResult<I>, O> function) throws ExecutionException{
		try {
			ListenableFuture<O> output = Futures.transform(Futures.immediateFuture(input), function);
			return output.get();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	public <O> List<O> allResults(){
		final MapResults<R,O> map = new MapResults<R,O>();
		List<O> fromList = map.run(Input1.with(this));
		return fromList;
	}
	
	/***
	 * Reduce this command result to a single output
	 * @param function - The function to reduce List<Object> to single output of R`
	 * @return reducedResults - Applied by function
	 */
	public <I, O> O reduce(ReduceFunction<I,O> function){
		final MapResults<I, I> map = new MapResults<I, I>();
		List<I> fromList = map.run(Input.with(this));
		return function.reduce(fromList);
	}
}
