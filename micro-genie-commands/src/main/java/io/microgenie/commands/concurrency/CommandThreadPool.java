package io.microgenie.commands.concurrency;

import java.util.concurrent.ThreadPoolExecutor;



/**
 * 
 * Based on netflix hystrix thread pool
 * 
 * @author shawn
 *
 */
public interface CommandThreadPool {

    /**
     * Implementation of {@link ThreadPoolExecutor}.
     * 
     * @return ThreadPoolExecutor
     */
    public ThreadPoolExecutor getExecutor();


    /**
     * Whether the queue will allow adding an item to it.
     * <p>
     * This allows dynamic control of the max queueSize versus whatever the actual max queueSize is so that dynamic changes can be done via property changes rather than needing an app
     * restart to adjust when commands should be rejected from queuing up.
     * 
     * @return boolean whether there is space on the queue
     */
    public boolean isQueueSpaceAvailable();


    /***
     * The unique identifier for the Thread pool
     * @return key
     */
	public String getThreadPoolKey();
}

