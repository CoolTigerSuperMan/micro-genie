package io.microgenie.aws.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

/***
 * SQS Queue Configuration
 * @author shawn
 */
public class SqsConfig {

	private boolean produces = true;
	private boolean blockUntilReady = true;
	private List<SqsQueueConfig> queues = new ArrayList<SqsQueueConfig>();
	private List<SqsConsumerConfig> consumers = new ArrayList<SqsConsumerConfig>();
	
	@JsonProperty(value="queues")
	public List<SqsQueueConfig> getQueues() {
		return queues;
	}
	@JsonProperty(value="queues")
	public void setQueues(List<SqsQueueConfig> queues) {
		this.queues = queues;
	}
	@JsonProperty(value="consumers")
	public List<SqsConsumerConfig> getConsumers() {
		return consumers;
	}
	@JsonProperty(value="consumers")
	public void setConsumers(List<SqsConsumerConfig> consumers) {
		this.consumers = consumers;
	}
	@JsonProperty(value="produces")
	public boolean isProduces() {
		return produces;
	}
	@JsonProperty(value="produces")
	public void setProduces(boolean produces) {
		this.produces = produces;
	}
	@JsonProperty(value="blockUntilReady")
	public boolean isBlockUntilReady() {
		return blockUntilReady;
	}
	@JsonProperty(value="blockUntilReady")
	public void setBlockUntilReady(boolean blockUntilReady) {
		this.blockUntilReady = blockUntilReady;
	}
	
	
	
	public SqsConfig withProduces(boolean producers){
		this.produces = producers;
		return this;
	}
	public SqsConfig withBlockUntilReader(boolean block){
		this.blockUntilReady = block;
		return this;
	}
	public SqsConfig withQueues(SqsQueueConfig ...queues){
		this.queues.addAll(Lists.newArrayList(queues));
		return this;
	}
	public SqsConfig withConsumers(SqsConsumerConfig ...consumers){
		this.consumers.addAll(Lists.newArrayList(consumers));
		return this;
	}
}
