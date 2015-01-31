package io.microgenie.aws.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;


/***
 * AwsConfiguration
 * @author shawn
 */
public class AwsConfig {
	
	private String region;
	private String accessKey;
	private String secretKey;
	private DynamoDbConfig dynamo;
	private SqsConfig sqs;
	private List<KinesisConfig> kinesis;
	private S3Config s3;
	
	
	public AwsConfig(){}
	
	
	@JsonProperty("region")
	public String getRegion() {
		return region;
	}
	@JsonProperty("region")
	public void setRegion(String region) {
		this.region = region;
	}
	@JsonProperty("accessKey")
	public String getAccessKey() {
		return accessKey;
	}
	@JsonProperty("accessKey")
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	@JsonProperty("secretKey")
	public String getSecretKey() {
		return secretKey;
	}
	@JsonProperty("secretKey")
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	@JsonProperty("s3")
	public S3Config getS3() {
		return s3;
	}
	@JsonProperty("s3")
	public void setS3(S3Config s3) {
		this.s3 = s3;
	}
	@JsonProperty("dynamo")
	public DynamoDbConfig getDynamo() {
		return dynamo;
	}
	@JsonProperty("dynamo")
	public void setDynamo(DynamoDbConfig dynamo) {
		this.dynamo = dynamo;
	}
	@JsonProperty("kinesis")
	public List<KinesisConfig> getKinesis() {
		return kinesis;
	}
	@JsonProperty("kinesis")
	public void setKinesis(List<KinesisConfig> kinesis) {
		this.kinesis = kinesis;
	}
	@JsonProperty("sqs")
	public SqsConfig getSqs() {
		return sqs;
	}
	@JsonProperty("sqs")
	public void setSqs(SqsConfig sqs) {
		this.sqs = sqs;
	}
	
	
	public static class AwsConfigBuilder{
		private String region;
		private String accessKey;
		private String secretKey;
		private DynamoDbConfig dynamo;
		private SqsConfig sqs;
		private List<KinesisConfig> kinesis;
		private S3Config s3;

		public AwsConfigBuilder withRegion(final String region){
			this.region = region;
			return this;
		}
		
		public AwsConfigBuilder withAccessKey(final String accessKey){
			this.accessKey = accessKey;
			return this;
		}
		
		public AwsConfigBuilder withSecretKey(final String secretKey){
			this.secretKey = secretKey;
			return this;
		}
		
		public AwsConfigBuilder withDynamo(final DynamoDbConfig dynamo){
			this.dynamo = dynamo;
			return this;
		}
		
		
		public AwsConfigBuilder withSqs(final SqsConfig sqs){
			this.sqs = sqs;
			return this;
		}
		
		
		public AwsConfigBuilder withKinesis(final KinesisConfig ...kinesis){
			if(this.kinesis==null){
				this.kinesis = Lists.newArrayList(kinesis);
			}else{
				this.kinesis.addAll(Lists.newArrayList(kinesis));
			}
			return this;
		}
		
		public AwsConfigBuilder withS3(final S3Config s3){
			this.s3 = s3;
			return this;
		}
		
		
		public AwsConfig build(){
			final AwsConfig config = new AwsConfig();
			config.setRegion(this.region);
			config.setAccessKey(this.accessKey);
			config.setSecretKey(this.secretKey);
			config.setDynamo(this.dynamo);
			config.setKinesis(this.kinesis);
			config.setSqs(this.sqs);
			config.setS3(this.s3);
			return config;
		}
	}
}
