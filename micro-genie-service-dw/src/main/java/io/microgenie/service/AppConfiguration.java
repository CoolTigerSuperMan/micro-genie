package io.microgenie.service;

import io.dropwizard.Configuration;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.AwsConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


/***
 * Evidint Application Configuration Factory
 * @author shawn
 */
public class AppConfiguration extends Configuration {

	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	private ApplicationFactory appFactory;
	private String host;
	private int port;
	private AwsConfig aws;
	private boolean useCommands = false;
	
	private String dateFormatPattern = ISO_8601_DATE_FORMAT;

	
	@JsonProperty("host")
	public String getHost() {
		return host;
	}
	@JsonProperty("port")
	public int getPort(){
		return this.port;
	}
	@JsonProperty("dateFormat")
	public String getDateFormatPattern() {
		return dateFormatPattern;
	}
	@JsonProperty("dateFormat")
	public void setDateFormatPattern(String dateFormatPattern) {
		this.dateFormatPattern = dateFormatPattern;
	}
	@JsonProperty("aws")
	public AwsConfig getAws() {
		return aws;
	}
	@JsonProperty("aws")
	public void setAws(AwsConfig aws) {
		this.aws = aws;
	}
	@JsonProperty("useCommands")
	public boolean isUseCommands() {
		return useCommands;
	}
	@JsonProperty("useCommands")
	public void setUseCommands(boolean useCommands) {
		this.useCommands = useCommands;
	}
	
	
	/**
	 * Get the application factory
	 * @return appFactory
	 */
	public ApplicationFactory getAppFactory() {
		return appFactory;
	}

	
	
    /**
     * Build the application factory
     * @return appFactory
     * @throws IllegalStateException - If no valid configuration was found
     */
	public ApplicationFactory buildAppFactory(final ObjectMapper mapper) throws IllegalStateException {
		if(this.aws!=null){
			this.appFactory = new AwsApplicationFactory(this.getAws(), this.useCommands); 
			return appFactory;
		}
		throw new IllegalStateException("No Valid Configuration was found, unable to build the application Factory");
	}
}
