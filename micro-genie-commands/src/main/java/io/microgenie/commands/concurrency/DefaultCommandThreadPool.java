package io.microgenie.commands.concurrency;

import io.microgenie.commands.core.CommandConfiguration.ExecutorProperties;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ExcludeFromJavadoc
 * @ThreadSafe
 */
class DefaultCommandThreadPool implements CommandThreadPool {
    
    private final ThreadPoolExecutor threadPoolExecutor;
	private ExecutorProperties properties;

	
    /**
     * The default thread pool implementation for commands 
     * @param threadPoolKey - unique thread pool identifier
     * @param properties - {@link ExecutorProperties}
     */
    public DefaultCommandThreadPool(final ExecutorProperties properties, final ThreadPoolExecutor executor) {
        
		this.properties = properties;
        this.threadPoolExecutor = executor;
    }

    
    @Override
    public ThreadPoolExecutor getExecutor() {
        return threadPoolExecutor;
    }
    

    @Override
	public String getThreadPoolKey() {
		return this.properties.getKey();
	}



    /**
     * Whether the thread pool queue has space available according to the <code>queueSizeRejectionThreshold</code> settings.
     * <p>
     * If a SynchronousQueue implementation is used (<code>maxQueueSize</code> == -1), it always returns 0 as the size so this would always return true.
     */
    @Override
    public boolean isQueueSpaceAvailable() {
        if (this.properties.getMaxQueueSize() < 0) {
            return true;
        } else {
            return this.threadPoolExecutor.getQueue().size() < properties.getQueueSizeRejectionThreshold();
        }
    }
}