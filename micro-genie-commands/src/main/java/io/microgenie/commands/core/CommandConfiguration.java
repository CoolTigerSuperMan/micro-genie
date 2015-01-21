package io.microgenie.commands.core;

import io.microgenie.commands.core.Timeout.TimeoutMillis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



/***
 * Contains Configuration classes for {@link ExecutorProperties} and {@link CommandProperties}
 * which are used to configure {@link ExecutorService} and {@link GenieContainer} implementations, respectively
 * 
 * @author shawn
 */
public class CommandConfiguration {


	/**
	 * Required ExecutorProperties Properties.
	 *  <p>
	 * Properties are cached when the ExecutorService instance is created 
	 * 
	 * @author shawn
	 */
	public static class ExecutorProperties {

	    private final String key;
	    private final Integer corePoolSize;
	    private final Integer keepAliveTime;
	    private final Integer maxQueueSize;
	    private final Integer queueSizeRejectionThreshold;
	    private final String propertyPrefix;
	    
	    /***
	     * ExecutorProperties Constructor, set by builder
	     */
	    protected ExecutorProperties() {
	        this(new Builder(), "command-executor");
	    }

	    /***
	     * ExecutorProperties Constructor, set by builder
	     * @param builder
	     */
	    protected ExecutorProperties(Builder builder) {
	        this(builder, "command-executor");
	    }

	    /***
	     * ExecutorProperties Constructor, set by builder
	     * @param builder
	     * @param propertyPrefix
	     */
	    protected ExecutorProperties(Builder builder, String propertyPrefix) {
	        this.key = builder.getKey();
	    	this.corePoolSize = builder.getCoreSize();
	        this.keepAliveTime = builder.getKeepAliveTimeMinutes();
	        this.maxQueueSize =  builder.getMaxQueueSize(); 
	        this.queueSizeRejectionThreshold = builder.getQueueSizeRejectionThreshold();  	  
	        this.propertyPrefix = propertyPrefix;
	    }


	    /**
	     * Core thread-pool size that gets passed to {@link ThreadPoolExecutor#setCorePoolSize(int)}
	     * 
	     * @return coreSize - {@code Integer}
	     */
	    public Integer getCoreSize() {
	        return corePoolSize;
	    }

	    /**
	     * Keep-alive time in minutes that gets passed to {@link ThreadPoolExecutor#setKeepAliveTime(long, TimeUnit)}
	     * @return keepAliveTime - {@code Integer}
	     */
	    public Integer getKeepAliveTimeMinutes() {
	        return keepAliveTime;
	    }

	    /**
	     * Max queue size that gets passed to {@link BlockingQueue} 
	     * @return maxQueueSize - {@code Integer}
	     */
	    public Integer getMaxQueueSize() {
	        return maxQueueSize;
	    }

	    /**
	     * Queue size rejection threshold is an artificial "max" size at which rejections will occur even if {@link #maxQueueSize} has not been reached. This is done because the {@link #maxQueueSize} of a
	     * {@link BlockingQueue} can not be dynamically changed and we want to support dynamically changing the queue size that affects rejections.
	     * <p>
	     * This is used by {@link GenieContainer} when queuing a thread for execution.
	     * 
	     * @return queueSizeRejectionThreshold - {@code Integer}
	     */
	    public Integer getQueueSizeRejectionThreshold() {
	        return queueSizeRejectionThreshold;
	    }
	    /**
	     * Factory method to retrieve the Builder.
	     * @return builder - new Builder instance
	     */
	    public static Builder Builder() {
	        return new Builder();
	    }
	    /**
	     * A unique key identifying an {@link ExecutorService}
	     * @return key
	     */
	    public String getKey() {
			return key;
		}
	    /**
	     * The property prefix to apply to naming conventions
	     * and eventually dynamic properties
	     * @return prefix
	     */
		public String getPropertyPrefix() {
			return propertyPrefix;
		}

		
		/***
		 * Default Properties for ExecutorProperties
		 * @param key - The ExecutorProperties key
		 * @return executorProperties
		 */
		public static ExecutorProperties defaults(final String key) {
			return new ExecutorProperties(new Builder(key));
		}
		
		
		/**
	     * Fluent interface that allows chained setting of properties that can 
	     * be passed into {@link ExecutorProperties} via a {@link GenieCommand} constructor 
	     * allowing injection of instance specific property overrides.
	     * <p>
	     * 
	     * <p>
	     * Example:
	     * <p>
	     * <pre> {@code
	     * CommandProperties.Buider()
	     * 			 .withKey("userService")
	     *           .withCoreSize(10)
	     *           .withQueueSizeRejectionThreshold(10);
	     * } </pre>
	     * 
	     * @NotThreadSafe
	     */
	    public static class Builder {
	    	
	    	/*** Default Property values*/
	    	private static final String DEFAULT_KEY = "default-thread-pool"; 	// default thread pool key 
	    	private static final int DEFAULT_CORE_SIZE = 10; 					// size of thread pool
		    private static final int DEFAULT_KEEP_ALIVE_MINUTES = 1;			// minutes to keep a thread alive (though in practice this doesn't get used as by default we set a fixed size)
		    private static final int DEFAULT_MAX_QUEUE_SIZE = 10; 				// size of queue (this can't be dynamically changed so we use 'queueSizeRejectionThreshold' to artificially limit and reject)
		    																	// -1 turns if off and makes us use SynchronousQueue
		    
		    private static final int DEFAULT_QUEUE_SIZE_REJECTION_THRESHOLD = 5; // number of items in queue
		    
		    private String key = DEFAULT_KEY;
	    	private Integer coreSize = DEFAULT_CORE_SIZE;
	        private Integer keepAliveTimeMinutes = DEFAULT_KEEP_ALIVE_MINUTES;
	        private Integer maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
	        private Integer queueSizeRejectionThreshold = DEFAULT_QUEUE_SIZE_REJECTION_THRESHOLD;
	        
	        private Builder() {}
	        private Builder(final String key) {
	        	this.key = key;
	        }
	        public String getKey() {
	            return this.key;
	        }
	        public Integer getCoreSize() {
	            return this.coreSize;
	        }
	        public Integer getKeepAliveTimeMinutes() {
	            return this.keepAliveTimeMinutes;
	        }
	        public Integer getMaxQueueSize() {
	            return this.maxQueueSize;
	        }
	        public Integer getQueueSizeRejectionThreshold() {
	            return this.queueSizeRejectionThreshold;
	        }
			public Builder WithKey(final String key) {
				this.key = key;
				return this;
			}
	        public Builder withCoreSize(final int size) {
	            this.coreSize = size;
	            return this;
	        }
	        public Builder withKeepAliveTimeMinutes(final int minutes) {
	            this.keepAliveTimeMinutes = minutes;
	            return this;
	        }
	        public Builder withMaxQueueSize(final int size) {
	            this.maxQueueSize = size;
	            return this;
	        }
	        public Builder withQueueSizeRejectionThreshold(final int threshold) {
	            this.queueSizeRejectionThreshold = threshold;
	            return this;
	        }
	    }
	}

	
	
	/**
	 * Command Properties
	 */
	
	
	/***
	 * Command Properties which include {@link ExecutorProperties}
	 * @author shawn
	 */
	public static class CommandProperties extends ExecutorProperties {

		private final static TimeoutMillis DEFAULT_COMMAND_TIMEOUT = Timeout.inMilliseconds(60 * 1000);
		private TimeoutMillis timeout = DEFAULT_COMMAND_TIMEOUT;		
		private final String commandKey;
		
	
		/**
		 * CommandProperties constructor
		 * 
		 * @param key - unique key identifying this command
		 * @param executorProperties - a configuration builder for ExecutorProperties
		 */
		protected CommandProperties(String key, ExecutorProperties.Builder executorProperties) {
			super(executorProperties, "command-");
			this.commandKey = key;
		}
		
		/**
		 * CommandProperties constructor
		 * 
		 * @param key - unique key identifying this command
		 * @param timeout - {@link TimeoutMillis} Command Timeout in milliseconds
		 * @param executorProperties - a configuration builder for ExecutorProperties
		 */
		protected CommandProperties(final String key, TimeoutMillis timeout, ExecutorProperties.Builder executorProperties) {
			super(executorProperties, "command-");
			this.commandKey = key;
			this.timeout = timeout ;
		}
		
		/**
		 * CommandProperties constructor
		 * 
		 * @param key - unique key identifying this command
		 * @param timeout - {@link TimeoutMillis} Command Timeout in milliseconds
		 * @param executorProperties - a configuration builder for ExecutorProperties
		 * @param propertyPrefix - a Property Prefix, generally specific to this command
		 */
		protected CommandProperties(String key, TimeoutMillis timeout, ExecutorProperties.Builder executorProperties, String propertyPrefix) {
			super(executorProperties, propertyPrefix);
			this.commandKey = key;
			this.timeout = timeout;
		}
		
		
		/***
		 * Get the specified command timeout in MS
		 * @return timeoutMS
		 */
		public Timeout getTimeout() {
			return this.timeout;
		}
		
		
		/***
		 * Get the command Key, uniquely identifying this command
		 * @return commandKey
		 */
		public String getCommandKey(){
			return this.commandKey;
		}
	
		
		/***
		 * Create an instance of CommandProperties for a specified command with defaults
		 * 
		 * @param key - A unique key identifying this command
		 * @param timeout - {@link TimeoutMillis} - A command timeout value, in milliseconds
		 * @return commandProperties - Default command properties
		 */
		public static CommandProperties createwithDefaults(final String key, final TimeoutMillis timeout){
			return new CommandProperties(key, timeout, ExecutorProperties.Builder());
		}
		
		/**
		 * Create an instance of CommandProperties for a specified command with defaults
		 * @param key - A unique key identifying this command
		 * @return commandProperties - Default command properties
		 */
		public static CommandProperties createwithDefaults(final String key){
			return new CommandProperties(key, ExecutorProperties.Builder());
		}
	}
}
