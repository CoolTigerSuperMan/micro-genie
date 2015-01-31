package io.microgenie.aws.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * SQS Queue Configuration
 * @author shawn
 */
public class SqsQueueConfig {
	
	/** The queue name **/
	private String name;
	
	
	/** If blockUntilQueue is ready in queue admin, the create queue process will wait this amount of time for the queue to become ready **/
	private int queueCreationTimeoutMS = 60000;
	
	
	/***
	 * The time in seconds that the delivery
     * of all messages in the queue will be delayed. An integer from 0 to 900
     * (15 minutes). The default for this attribute is 0 (zero).
	 */
	private int delaySeconds;
	public final static String DELAY_SECONDS = "DelaySeconds"; 
	
	
	
	/***
	 *  The limit of how many bytes a
     * message can contain before Amazon SQS rejects it. An integer from 1024
     * bytes (1 KiB) up to 262144 bytes (256 KiB). The default for this
     * attribute is 262144 (256 KiB)
	 */
	private int maximumMessageSize; 
	public final static String MAXIMUM_MESSAGE_SIZE = "MaximumMessageSize"; 
	
	
	
	/***
	 * - The number of seconds Amazon SQS retains a message. 
	 *   Integer representing seconds, from 60 (1minute) to 1209600 (14 days). 
	 *   The default for this attribute is 345600 (4 days).
	 */
	private int messageRetentionPeriod; 
	public final static String MESSAGE_RETENTION_PERIOD = "MessageRetentionPeriod"; 
	
	
	/***
	 * The default visibility timeout for
     * the queue. An integer from 0 to 43200 (12 hours). The default for this
     * attribute is 30.
	 */
	private int visibilityTimeout;
	public final static String VISIBILITY_TIMEOUT = "VisibilityTimeout"; 
	
	

	@JsonProperty("name")
	public String getName() {
		return name;
	}
	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}
	public SqsQueueConfig withName(final String name){
		this.name = name;
		return this;
	}
	
	/**
	 * When publishing messages to a queue where delay seconds is set, the queue will act as a delayed queue
	 * @return delaySeconds
	 */
	@JsonProperty("delaySeconds")
	public int getDelaySeconds() {
		return delaySeconds;
	}
	@JsonProperty("delaySeconds")
	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
	}
	public SqsQueueConfig withDelay(final int seconds){
		this.delaySeconds = seconds;
		return this;
	}
	
	
	/***
	 * Maximum Message size
	 * @return maximumMessageSize - The maximum message size in bytes
	 */
	@JsonProperty("maximumMessageSize")
	public int getMaximumMessageSize() {
		return maximumMessageSize;
	}
	@JsonProperty("maximumMessageSize")
	public void setMaximumMessageSize(int maximumMessageSize) {
		this.maximumMessageSize = maximumMessageSize;
	}
	public SqsQueueConfig withMaxMessageSize(final int maxMessageSize){
		this.maximumMessageSize = maxMessageSize;
		return this;
	}
	
	
	/**
	 * Retention Period
	 * @return messageRetentionPeriod
	 */
	@JsonProperty("messageRetentionPeriod")
	public int getMessageRetentionPeriod() {
		return messageRetentionPeriod;
	}
	@JsonProperty("messageRetentionPeriod")
	public void setMessageRetentionPeriod(int messageRetentionPeriod) {
		this.messageRetentionPeriod = messageRetentionPeriod;
	}
	public SqsQueueConfig withMessageRetentionPeriod(final int retention){
		this.messageRetentionPeriod = retention;
		return this;
	}
	
	
	/**
	 * Default Visibility timeout. Messages will remain hidden from all consumers for a period of time after being consumed. 
	 * If not deleted within the visibilityTimeout window the message will appear as available again, so long as it's still under the retention period
	 * @return - visibilityTimeout
	 */
	@JsonProperty("visibilityTimeout")
	public int getVisibilityTimeout() {
		return visibilityTimeout;
	}
	@JsonProperty("visibilityTimeout")
	public void setVisibilityTimeout(int visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;
	}
	public SqsQueueConfig withVisibilityTimeout(final int timeout){
		this.visibilityTimeout = timeout;
		return this;
	}
	
	@JsonProperty("queueCreationTimeoutMS")
	public int getQueueCreationTimeoutMS() {
		return queueCreationTimeoutMS;
	}
	
	@JsonProperty("queueCreationTimeoutMS")
	public void setQueueCreationTimeoutMS(int queueCreationTimeoutMS) {
		this.queueCreationTimeoutMS = queueCreationTimeoutMS;
	}
	public SqsQueueConfig withQueueCreationTimeout(final int queueCreationTimeoutMS){
		this.queueCreationTimeoutMS = queueCreationTimeoutMS;
		return this;
	}
	
	
	
	protected Map<String, String> getAttributes() {
		return this.createAttributes();
	}
	
	/***
	 * Create SQS Attribute Map from configuration settings
	 */
	public Map<String, String> createAttributes() {
		final Map<String, String> attributes = new HashMap<String, String>();
		if(this.maximumMessageSize>0){
			attributes.put(MAXIMUM_MESSAGE_SIZE, String.valueOf(this.maximumMessageSize));	
		}
		if(this.messageRetentionPeriod>0){
			attributes.put(MESSAGE_RETENTION_PERIOD, String.valueOf(this.messageRetentionPeriod));	
		}
		if(this.visibilityTimeout>0){
			attributes.put(VISIBILITY_TIMEOUT, String.valueOf(this.visibilityTimeout));	
		}
		if(this.delaySeconds>0){
			attributes.put(DELAY_SECONDS, String.valueOf(this.delaySeconds));	
		}
		return attributes;
	}
}
