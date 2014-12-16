package io.microgenie.commands.concurrency;


import io.microgenie.commands.core.CommandConfiguration.ExecutorProperties;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/***
 * Thread Pool Factory
 * @author shawn
 */
public class ThreadPoolFactory {

	/***
	 * Create the appropriate blocking queue based on max queue size
	 * 
	 * @param maxQueueSize
	 *            - max queue size of the blocking queue - The point where commands will be rejected
	 *            
	 * @return blockingQueue
	 */
	private BlockingQueue<Runnable> createBlockingQueue(int maxQueueSize) {
		if (maxQueueSize <= 0) {
			return new SynchronousQueue<Runnable>();
		} else {
			return new LinkedBlockingQueue<Runnable>(maxQueueSize);
		}
	}
	
	

	/***
	 * Create a Thread Pool Executor with a custom thread factory
	 * 
	 * @param threadPoolKey
	 * @param properties
	 * @return workQueue
	 */
	private ThreadPoolExecutor createExecutor(
			final ExecutorProperties properties,
			final BlockingQueue<Runnable> workQueue) {

		final String prefix = properties.getPropertyPrefix();
		final String key = properties.getKey();
		
		return new ThreadPoolExecutor(
				properties.getCoreSize(), 
				properties.getMaxQueueSize(),
				properties.getKeepAliveTimeMinutes(), 
				TimeUnit.MINUTES, 
				workQueue, new ThreadFactory() {
					protected final AtomicInteger threadNumber = new AtomicInteger(0);
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format("%s-%s-%d",prefix, key,threadNumber.incrementAndGet()));
					}
				});
	}


	 /***
	  * Create a new ListeningExecutorService
	  * 
	  * @param properties Thread Pool properties
	  * @return executor
	  */
	 public synchronized ListeningExecutorService create(final ExecutorProperties properties){
		 final BlockingQueue<Runnable> queue = this.createBlockingQueue(properties.getMaxQueueSize());
		 final ThreadPoolExecutor executor = this.createExecutor(properties, queue);
		return MoreExecutors.listeningDecorator(executor);
	 }
}
