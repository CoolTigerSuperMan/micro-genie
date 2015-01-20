//package io.microgenie.commands.concurrency;
//
//import io.microgenie.commands.core.CommandConfiguration.CommandProperties;
//import io.microgenie.commands.core.CommandConfiguration.ExecutorProperties;
//import io.microgenie.commands.core.GenieCommand;
//import io.microgenie.commands.util.CloseableUtils;
//
//import java.io.Closeable;
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//
//import com.google.common.base.Preconditions;
//import com.google.common.util.concurrent.ListeningExecutorService;
//import com.google.common.util.concurrent.MoreExecutors;
//
//
///**
// * 
// * @author shawn
// *
// * @param <F>
// */
//public class ThreadGroupCommandFactory extends ThreadCommandFactory{
//
//	private final static int EXECUTOR_SHUTDOWN_TIME_MS = 2000;	
//	private final ConcurrentHashMap<String, ExecutorProperties> threadPoolsPropertyMaps;
//	private final ConcurrentHashMap<String, CommandProperties> commandPropertiesMaps;
//	private final ConcurrentHashMap<String, ListeningExecutorService> executors;
//	private final Set<Closeable> closables;
//	private final ThreadPoolFactory threadPoolFactory;
//	
//
//	/**
//	 * 
//	 */
//	public ThreadGroupCommandFactory(){
//		this.threadPoolFactory = new ThreadPoolFactory();
//		this.closables = new HashSet<Closeable>();
//		this.executors =  new ConcurrentHashMap<String, ListeningExecutorService>();
//		this.threadPoolsPropertyMaps = new ConcurrentHashMap<String, ExecutorProperties>();
//		this.commandPropertiesMaps = new ConcurrentHashMap<String, CommandProperties>();		
//	}
//	
//	
//	
//
//	
//	/**
//	 * Register a command and it's settings
//	 * @param command 
//	 * @param properties
//	 */
//	public <R> void registerCommand(GenieCommand<R> command, CommandProperties properties){
//		this.putProperties(command.getKey(), properties);
//		this.putPoolProperties(properties.getKey(), properties);
//	}
//	
//	
//	
//	
//
//	
//	/***
//	 * Get the threadPoolFactory containing all thread pools
//	 * @return threadPoolFactory
//	 */
//	public ThreadPoolFactory pools(){
//		return threadPoolFactory;
//	}
//	
//	
//	
//	/**
//	 * Get the listingExecutorService
//	 * @param key
//	 * @return executorService
//	 */
//	public ListeningExecutorService executors(final String key){
//		ListeningExecutorService executor = this.executors.get(key);
//		if(executor!=null){
//			return executor;
//		}
//		return this.createAndSetIfAbsent(key);
//	}
//	
//	
//
//	/***
//	 * Creates the executorService {@link ListeningExecutorService} if it does not already exist.
//	 * <p>
//	 * Otherwise it returns the already created executor
//	 * @param key - Thread group key
//	 * @return executor 
//	 */
//	private synchronized ListeningExecutorService createAndSetIfAbsent(final String key){
//		
//		ListeningExecutorService executor = this.executors.get(key);
//		
//		/** If it's null **/
//		if(executor == null){
//			
//			final ExecutorProperties threadPoolProperties = Preconditions.checkNotNull(this.poolProperties(key),String.format("Unable to create an executor for group %s. " + "Thread Property Configuration was null", key));
//			
//			executor = Preconditions.checkNotNull(this.threadPoolFactory.create(threadPoolProperties)); 
//
//			/** If it already existed (the map was modified outside of this sync block, use the existing executor and shutdown the one we just created **/
//			ListeningExecutorService existed = this.executors.putIfAbsent(key, executor);
//			if(existed !=null){ 
//				try{
//					if(executor!=null) {
//						MoreExecutors.shutdownAndAwaitTermination(executor, EXECUTOR_SHUTDOWN_TIME_MS, TimeUnit.MILLISECONDS);
//						executor.shutdownNow();
//					}
//				}
//				catch(Exception ex){
//					//Ignoring
//				}
//				return existed;
//			}
//		}
//		return executor;
//	}
//		
//
//	
//	/**
//	 * Get Command Properties for the given commandKey
//	 * @param key
//	 * @param properties - The command properties for this command. null if no properties exist
//	 */
//	public CommandProperties properties(String commandKey) {
//		return this.commandPropertiesMaps.get(commandKey);
//	}
//	
//	
//	
//	/***
//	 * Get the Thread Pool Configuration by pool key / command group key
//	 * <p>
//	 * 
//	 * @param poolKey - The thread pool key
//	 * @return executorProperties - Executor Properties associated with a given thread pool key
//	 */
//	public ExecutorProperties poolProperties(final String poolKey){
//		return this.threadPoolsPropertyMaps.get(poolKey);
//	}
//	
//
//
//	/**
//	 * Add Command Properties if Absent.
//	 * 
//	 * @param key - The command key
//	 * @param properties - {@link CommandProperties}
//	 * @return - If the properties already existed then existing properties are returned. 
//	 * 				Otherwise, null is returned if no value previously existed and the properties were added. 
//	 */
//	public CommandProperties putProperties(final String key, final CommandProperties properties) {
//		final CommandProperties commandProperties = this.commandPropertiesMaps.putIfAbsent(key, properties);
//		return commandProperties;
//	}
//	
//	
//	
//	/***
//	 * Add Thread pool properties
//	 * 
//	 * <p>
//	 * @param key - A thread pool key / command Group key
//	 * @param executorProperties - properties used to configure a command executor 
//	 * @return threadPoolProperties - If the properties already existed then existing properties are returned. 
//	 * 								Otherwise, null is returned if no value previously existed and the properties were added. 
//	 */
//	public ExecutorProperties putPoolProperties(final String key, final ExecutorProperties properties){
//		final ExecutorProperties  pooProperties = threadPoolsPropertyMaps.putIfAbsent(key, properties);
//		return pooProperties;
//	}
//	
//
//	/**
//	 * Register instance implementing closable that should have it's close
//	 * method executed when {@link Genie#shutdown()} is called
//	 * 
//	 * @param closable
//	 */
//	public synchronized void registerCloseable(Closeable closable){
//		this.closables.add(closable);
//	}
//	
//	
//	
//
//	
//	/***
//	 * Shutdown all registered closeable
//	 */
//	public synchronized void shutdown(){
//		
//		for(Closeable closeable : this.closables){
//			CloseableUtils.closeQuietly(closeable);
//		}
//		this.closables.clear();
//		for(Entry<String, ListeningExecutorService> executorEntry : this.executors.entrySet()){
//			try{
//				MoreExecutors.shutdownAndAwaitTermination(executorEntry.getValue(), EXECUTOR_SHUTDOWN_TIME_MS, TimeUnit.MILLISECONDS);	
//			}catch(Exception ex){
//				//Ignore
//			}	
//		}
//		this.executors.clear();
//	}
//
//	
//	@Override
//	public void close() throws IOException {
//		this.shutdown();
//	}
//}
