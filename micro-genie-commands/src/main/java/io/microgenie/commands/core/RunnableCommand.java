package io.microgenie.commands.core;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;


/***
 * A runnable genie command accepts no input parameters
 * @author shawn
 *
 * @param <R>
 */
public interface RunnableCommand<R> extends GenieCommand<R>{

	public ListenableFuture<R> submitTask(final ListeningExecutorService executor);
	public R run() throws ExecutionException;
}