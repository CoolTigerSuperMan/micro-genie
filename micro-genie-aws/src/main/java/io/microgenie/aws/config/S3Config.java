package io.microgenie.aws.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;


/**
 * S3 Configuration
 * @author shawn
 */
public class S3Config {
	
	private String defaultDrive;
	private List<String> buckets = new ArrayList<String>();

	/**
	 * get the default bucket to use if no bucket is specified
	 * @return bucketName
	 */
	@JsonProperty("defaultBucket")
	public String getDefaultDrive() {
		return defaultDrive;
	}
	/**
	 * Set the default bucket name to use in cases where no bucket is specified
	 * @param drive
	 */
	@JsonProperty("defaultBucket")
	public void setDefaultDrive(final String drive){
		this.defaultDrive = drive;
	}
	@JsonProperty("buckets")
	public List<String> getBuckets() {
		return buckets;
	}
	@JsonProperty("buckets")
	public void setBuckets(List<String> buckets) {
		this.buckets = buckets;
	}
	
	
	public S3Config withDefaultBucket(final String bucket){
		this.defaultDrive = bucket;
		return this;
	}
	public S3Config withBuckets(final String ... buckets){
		this.buckets.addAll(Lists.newArrayList(buckets));
		return this;
	}
}
