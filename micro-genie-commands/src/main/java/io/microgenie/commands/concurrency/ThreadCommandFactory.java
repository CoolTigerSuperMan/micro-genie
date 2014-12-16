package io.microgenie.commands.concurrency;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;


/***
 * The default is to use one executor with a cached thread pool
 * @author shawn
 *
 */
public class ThreadCommandFactory implements Closeable{

	private static final Object CREATE_EXECUTOR_LOCK = new Object();
	private static final long DEFAULT_SHUTDOWN_SECONDS = 2;
	private ListeningExecutorService defaultExecutor;
	
	public ThreadCommandFactory(){}
	
	
	/***
	 * This method should be overridden where different thread pool strategies apply
	 * @param commandKey
	 * @return executor
	 */
	public  ListeningExecutorService getExecutor(final String commandKey){
		if(defaultExecutor==null){
			synchronized (CREATE_EXECUTOR_LOCK) {
				if(this.defaultExecutor==null){
					this.defaultExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());	
				}
			}
		}
		return this.defaultExecutor;
	}

	
	/**
	 * Shutdown the default executor
	 */
	@Override
	public void close() throws IOException {
		if(this.defaultExecutor!=null){
			MoreExecutors.shutdownAndAwaitTermination(this.defaultExecutor, DEFAULT_SHUTDOWN_SECONDS, TimeUnit.SECONDS);	
		}
	}	
}
