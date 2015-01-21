package io.microgenie.commands.concurrency;

import io.microgenie.commands.core.CommandConfiguration.ExecutorProperties;

import com.google.common.util.concurrent.ListeningExecutorService;


/***
 * A singleton of ThreadCommandFactory
 * @author shawn
 *
 */
public enum ExecutorRegistry {
	INSTANCE;
	
	private ThreadCommandFactory threadFactory = new ThreadCommandFactory();
	
	ExecutorRegistry(){}
	
	/***
	 * Get the ThreadCommandFactory
	 */
	public ThreadCommandFactory get(){
		return threadFactory;
	}
	/***
	 * Get the listening executor for the Command Group Key
	 * @param key - Command Group Key
	 * @return executorService
	 */
	public ListeningExecutorService get(final String key){
		return this.threadFactory.getExecutor(key);
	}
	public ListeningExecutorService register(final ExecutorProperties properties){
		return this.threadFactory.registerExecutor(properties);
	}
}
