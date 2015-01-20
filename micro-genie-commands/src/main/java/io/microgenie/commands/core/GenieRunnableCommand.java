package io.microgenie.commands.core;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;


/***
 * A genie command implementing the {@link RunnableCommand} interface
 * @author shawn
 *
 * @param <O> - The output type
 */
public abstract class GenieRunnableCommand<O> extends GenieContainer<O>  implements RunnableCommand<O>{

	protected GenieRunnableCommand(String key, ListeningExecutorService executor) {
		super(key, executor);
	}



	/***
	 * Submit the task with no input
	 */
	@Override
	public ListenableFuture<O> submitTask(final ListeningExecutorService executor) {
		final Callable<O> callable =  new Callable<O>(){
			@Override
			public O call() throws Exception {
				return run();
			}
		};
		/** Submit the task with the appropriate callable wrapper**/
		final ListenableFuture<O> resultFuture = executor.submit(callable);
		return resultFuture;
	}
	
	

	@Override
	protected void success(O result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void failure(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected O fallback() {
		// TODO Auto-generated method stub
		return null;
	}
}
