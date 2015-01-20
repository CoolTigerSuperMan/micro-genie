package io.microgenie.commands.core;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;




/***
 * Input Command
 * 
 * @author shawn
 * @param <R>
 */
 interface InputCommand<I,O> extends GenieCommand<O>{

	public ListenableFuture<O> submitTask(final ListeningExecutorService executor, final I input);

}
