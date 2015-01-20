package io.microgenie.aws;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Kinesis configuration
 * @author shawn
 *
 */
public class KinesisConfig {
	private String topic;
	private int shards;
	
	public KinesisConfig(){}
	
	
	@JsonProperty("topic")
	public String getTopic() {
		return topic;
	}
	@JsonProperty("topic")
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	
	
	@JsonProperty("shards")
	public int getShards() {
		return shards;
	}
	@JsonProperty("shards")
	public void setShards(int shards) {
		this.shards = shards;
	}
}
