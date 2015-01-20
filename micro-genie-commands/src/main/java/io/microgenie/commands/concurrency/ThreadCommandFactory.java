package io.microgenie.commands.concurrency;

import io.microgenie.commands.core.CommandConfiguration.ExecutorProperties;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;


/***
 * The default is to use one executor with a cached thread pool
 * @author shawn
 *
 */
public class ThreadCommandFactory implements Closeable{

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadCommandFactory.class);
	
	private static final Object CREATE_EXECUTOR_LOCK = new Object();
	private static final long DEFAULT_SHUTDOWN_SECONDS = 1;
	private final ListeningExecutorService defaultExecutor;
	private final ThreadPoolFactory threadPoolFactory;
	
	private final Map<String, ListeningExecutorService> groupExecutors = new HashMap<String, ListeningExecutorService>();
	
	
	public ThreadCommandFactory(){
		this(new ThreadPoolFactory());
	}
	
	
	public ThreadCommandFactory(ThreadPoolFactory threadPoolFactory){
		this.threadPoolFactory = threadPoolFactory;
		this.defaultExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	}
	
	
	
	public ListeningExecutorService registerExecutor(final ExecutorProperties properties){
		final String key = properties.getKey();
		synchronized (CREATE_EXECUTOR_LOCK) {
			if(!groupExecutors.containsKey(key)){
				final ListeningExecutorService  executorService = this.threadPoolFactory.create(properties);
				groupExecutors.put(key, executorService);
			}
		}
		return groupExecutors.get(key);
	}
	
	

	
	/***
	 * This method should be overridden where different thread pool strategies apply
	 * @param commandKey
	 * @return executor
	 */
	public  ListeningExecutorService getExecutor(final String key){		
		
		ListeningExecutorService es = null; 
		if(key!=null){
			es = groupExecutors.get(key);	
		}

		if(es==null){
			LOGGER.debug("Using the default executor - No Executor found for key: {}", key);
			return this.defaultExecutor;	
		}else{
			LOGGER.debug("Using Executor for key: {}", key);
			return es;
		}
	}

	
	/**
	 * Shutdown the default executor
	 */
	@Override
	public void close() throws IOException {
		for(Entry<String, ListeningExecutorService> exectorEntries : this.groupExecutors.entrySet()){
			MoreExecutors.shutdownAndAwaitTermination(exectorEntries.getValue(), DEFAULT_SHUTDOWN_SECONDS, TimeUnit.SECONDS);
		}
		if(this.defaultExecutor!=null){
			MoreExecutors.shutdownAndAwaitTermination(this.defaultExecutor, DEFAULT_SHUTDOWN_SECONDS, TimeUnit.SECONDS);	
		}
	}	
}
