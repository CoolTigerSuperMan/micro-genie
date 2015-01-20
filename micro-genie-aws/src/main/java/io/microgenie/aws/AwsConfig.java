package io.microgenie.aws;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


/***
 * AwsConfiguration
 * @author shawn
 *
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
}
