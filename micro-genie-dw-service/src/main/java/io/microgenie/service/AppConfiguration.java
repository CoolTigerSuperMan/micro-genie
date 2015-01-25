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

	private String host;
	private int port;
	private SchemaContracts schemaContracts;
	
	private String dateFormatPattern = ISO_8601_DATE_FORMAT;
	private ApplicationFactory appFactory;

	private AwsConfig aws;
	private boolean useCommands = false;
	
	
	@JsonProperty("host")
	public String getHost() {
		return this.host;
	}
	@JsonProperty("host")
	public void setHost(String host) {
		this.host = host;
	}
	@JsonProperty("port")
	public int getPort(){
		return this.port;
	}	
	@JsonProperty("port")
	public void setPort(int port) {
		this.port = port;
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

	@JsonProperty("schemaContracts")
	public SchemaContracts getSchemaContracts() {
		return schemaContracts;
	}
	@JsonProperty("schemaContracts")
	public void setSchemaContracts(SchemaContracts schemaContracts) {
		this.schemaContracts = schemaContracts;
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



	/***
	 * Used to publish schema contracts from this service for other services and / or code generation
	 * @author shawn
	 */
	public static class SchemaContracts{
		
		private String publishDrive;
		private String publishPath;
		private String scanPackage;
		
		protected SchemaContracts(){}
		
		public SchemaContracts(final String drive, final String path, final String scanPackage){
			this.publishDrive = drive;
			this.publishPath = path;
			this.scanPackage = scanPackage;
		}
		
		@JsonProperty("drive")
		public String getDrive() {
			return publishDrive;
		}
		@JsonProperty("drive")
		public void setDrive(String publishDrive) {
			this.publishDrive = publishDrive;
		}
		@JsonProperty("path")
		public String getPath() {
			return publishPath;
		}
		@JsonProperty("path")
		public void setPath(String publishPath) {
			this.publishPath = publishPath;
		}
		@JsonProperty("scanPackage")
		public String getScanPackage() {
			return scanPackage;
		}
		@JsonProperty("scanPackage")
		public void setScanPackage(String scanPackage) {
			this.scanPackage = scanPackage;
		}
	}
}
