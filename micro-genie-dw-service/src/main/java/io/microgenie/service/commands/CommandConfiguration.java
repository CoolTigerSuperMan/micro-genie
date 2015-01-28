package io.microgenie.service.commands;

import io.dropwizard.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommandConfiguration extends Configuration{

	@JsonProperty(value="kinesisConsumer")
	private KinesisConsumerConfig kinesisConsumer;
	
	public static class KinesisConsumerConfig{
		private String topic;
		private String clientId;
		@JsonProperty(value="topic")
		public String getTopic() {
			return topic;
		}
		@JsonProperty(value="topic")
		public void setTopic(String topic) {
			this.topic = topic;
		}
		@JsonProperty(value="clientId")
		public String getClientId() {
			return clientId;
		}
		@JsonProperty(value="clientId")
		public void setClientId(String clientId) {
			this.clientId = clientId;
		}
	}

	@JsonProperty(value="kinesisConsumer")
	public KinesisConsumerConfig getKinesisConsumer() {
		return kinesisConsumer;
	}
	@JsonProperty(value="kinesisConsumer")
	public void setKinesisConsumer(KinesisConsumerConfig kinesisConsumer) {
		this.kinesisConsumer = kinesisConsumer;
	}
}
