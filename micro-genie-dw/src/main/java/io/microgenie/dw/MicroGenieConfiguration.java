package io.microgenie.dw;

import io.dropwizard.Configuration;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.AwsConfig;

import com.fasterxml.jackson.annotation.JsonProperty;


/***
 * Configuration for the Micro-Genie Dropwizard application
 * @author shawn
 *
 */
public class MicroGenieConfiguration extends Configuration {
    

	private ApplicationFactory appFactory;
	private AwsConfig aws;
    

	@JsonProperty("aws")
	public AwsConfig getAws() {
		return aws;
	}
	@JsonProperty("aws")
	public void setAws(AwsConfig aws) {
		this.aws = aws;
	}
	
	
	/**
	 * Get the application factory
	 * @return
	 */
	public ApplicationFactory getAppFactory() {
		return appFactory;
	}

	
    /***
     * Build the application factory
     * @return applicationCommandFactory
     * 
     * @throws IllegalStateException - If no valid configuration is found
     */
	public ApplicationFactory buildAppCommandFactory() {
		if(this.aws!=null){
			this.appFactory = new AwsApplicationFactory(this.aws, true); 
			return appFactory;
		}
		throw new IllegalStateException("No Valid Configuration was found, unable to build the application Factory");
	}
}