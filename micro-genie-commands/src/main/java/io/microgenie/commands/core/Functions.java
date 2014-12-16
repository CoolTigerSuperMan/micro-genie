package io.microgenie.commands.core;

import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.Inputs.Input;
import io.microgenie.commands.core.Inputs.Input1;
import io.microgenie.commands.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;



/**
 * Handy Functions for transformations
 * @author shawn
 * @param <A>
 */
public class Functions{

	
	
	public static class MapResults<I,O> implements Func1<CommandResult<I>,List<O>>{

		private List<O> flattenResults(CommandResult<I> result) throws TimeoutException, ExecutionException{
			return this.flattenResults(result, new ArrayList<O>());
		}
		
		private List<O> flattenResults(final CommandResult<?> result, List<O> collected) throws TimeoutException, ExecutionException{
			@SuppressWarnings("unchecked")
			final O actualResult = (O)result.get();
			if(actualResult!=null){
				collected.add(actualResult);	
			}
			if(CollectionUtil.hasElements(result.getChildren())){
				for(CommandResult<?> child : result.getChildren()){
					this.flattenResults(child, collected);
				}
			}
			return collected;
		}

		/**
		 * Execute the flattenResults function
		 */
		@Override
		public List<O> run(Input1<CommandResult<I>> input) {
			try {
				return this.flattenResults(input.a);
			} catch (TimeoutException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Legacy method
		 */
		@Override
		@SuppressWarnings("unchecked")
		public List<O> run(Input input) {
			return this.run((Input1<CommandResult<I>>)input);
		}
	}
	
	


	/***
	 * A base transform function used to transform results
	 * @author shawn
	 *
	 * @param <I>
	 * @param <O>
	 */
	public static abstract class TransformFunction<I,O> implements  AsyncFunction<CommandResult<I>, O>{
		
		protected abstract  void collect(Object result);
		protected abstract O transform();

		/**
		 * Function apply method that executes the collect and transform methods
		 * <p>
		 * All results are traversed and for each result, {@link #collect(Object)} is called, 
		 * allowing the implementation to filter out and accumulate results.
		 * <p> 
		 * After all commands have been traversed, {@link #transform(CommandResult)} is called.
		 */
		@Override
		public ListenableFuture<O> apply(CommandResult<I> input)throws Exception {
			this.collectResults(input);
			O output = this.transform();
			return Futures.immediateFuture(output);
		}
		
		/**
		 * @param result
		 * @throws ExecutionException 
		 * @throws TimeoutException 
		 */
		private void collectResults(CommandResult<?> result) throws TimeoutException, ExecutionException{			
			if(result!=null){
				this.collect(result.get());
				if(CollectionUtil.hasElements(result.getChildren())){
					for(CommandResult<?> childResult : result.getChildren()){
						this.collectResults(childResult);
					}	
				}				
			}
		}
	}


	
	/**
	 * Reduce many results into one
	 * @author shawn
	 *
	 * @param <F>
	 * @param <T>
	 */
	public static abstract class ReduceFunction<I, O> implements Func1<List<I>, O> {
		protected abstract O reduce(List<I> from);
		@SuppressWarnings("unchecked")
		public O run(Input input){
			return this.run((Input1<List<I>>)input);
		}
		public O run(Input1<List<I>> input){
			return reduce(input.a);
		}
	}
}
