package io.microgenie.service;

import io.dropwizard.Configuration;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.StateChangeConfiguration;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.config.AwsConfig;
import io.microgenie.service.commands.CommandConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


/***
 * Application Configuration Factory
 * <p>
 * This class contains common configuration elements to configure micro-genie service
 * functionality
 * @author shawn
 */
public class AppConfiguration extends Configuration {

	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private String dateFormat = ISO_8601_DATE_FORMAT; /** default to ISO 8601 UTC date format **/
	private ApiConfiguration api;
	private StateChangeConfiguration stateChanges;
	private CommandConfiguration commands;
	private AwsConfig aws;
	
	private ApplicationFactory appFactory;

	
	/***
	 * AWS related configuration
	 * @return awsConfig
	 */
	@JsonProperty("aws")
	public AwsConfig getAws() {
		return aws;
	}
	/***
	 * AWS related configuration
	 * @param aws
	 */
	@JsonProperty("aws")
	public void setAws(AwsConfig aws) {
		this.aws = aws;
	}
	/***
	 * State Change Configuration
	 * @return stateChangeConfiguration
	 */
	@JsonProperty("stateChanges")
	public StateChangeConfiguration getStateChanges() {
		return stateChanges;
	}
	/**
	 * State Change Configuration
	 * @param stateChanges
	 */
	@JsonProperty("stateChanges")
	public void setStateChanges(StateChangeConfiguration stateChanges) {
		this.stateChanges = stateChanges;
	}
	/***
	 * API Documentation configuration
	 * @return apiDocumentation configuration
	 */
	@JsonProperty("api")
	public ApiConfiguration getApi() {
		return this.api;
	}
	/**
	 * API Documentation configuration
	 * @param api
	 */
	@JsonProperty("api")
	public void setApi(ApiConfiguration api) {
		this.api = api;
	}
	/***
	 * The date format to use with JSON date fields, unless specifically overridden by other configurations
	 * @return dateFormat
	 */
	@JsonProperty("dateFormat")
	public String getDateFormat() {
		return dateFormat;
	}
	@JsonProperty("dateFormat")
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	/**
	 * Command Configuration
	 * @return commandConfiguration
	 */
	@JsonProperty("commands")
	public CommandConfiguration getCommands() {
		return commands;
	}
	/***
	 * Command Configuration
	 * @param commands - Set command configuration
	 */
	@JsonProperty("commands")
	public void setCommands(CommandConfiguration commands) {
		this.commands = commands;
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
			this.appFactory = new AwsApplicationFactory(this.getAws(), mapper); 
			return appFactory;
		}
		throw new IllegalStateException("No Valid Configuration was found, unable to build the application Factory");
	}





	/**
	 * API documentation configuration
	 * <p>
	 * Configuration to configure API documentation. The host property defaults
	 * to localhost and the port default of 8080
	 *</p>
	 * @author shawn
	 */
	public static class ApiConfiguration{
		private String host = "localhost";
		private int port = 8080;
		private SchemaContracts schemaContracts;
		private String dateFormat = ISO_8601_DATE_FORMAT;
		@JsonProperty("host")
		public String getHost() {
			return this.host;
		}
		@JsonProperty("host")
		public void setHost(final String host) {
			this.host = host;
		}
		@JsonProperty("port")
		public int getPort(){
			return this.port;
		}	
		@JsonProperty("port")
		public void setPort(final int port) {
			this.port = port;
		}
		@JsonProperty("dateFormat")
		public String getDateFormat() {
			return dateFormat;
		}
		@JsonProperty("dateFormat")
		public void setDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
		}
		@JsonProperty("schemaContracts")
		public SchemaContracts getSchemaContracts() {
			return schemaContracts;
		}
		@JsonProperty("schemaContracts")
		public void setSchemaContracts(final SchemaContracts schemaContracts) {
			this.schemaContracts = schemaContracts;
		}
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
