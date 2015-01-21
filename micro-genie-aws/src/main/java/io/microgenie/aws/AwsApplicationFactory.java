package io.microgenie.aws;

import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.commands.ApplicationCommandFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.http.ApacheHttpFactory;
import io.microgenie.application.http.HttpFactory;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.aws.dynamodb.DynamoDbMapperFactory;
import io.microgenie.aws.kinesis.KinesisEventFactory;
import io.microgenie.aws.s3.S3BlobFactory;
import io.microgenie.aws.sqs.SqsFactory;
import io.microgenie.commands.core.CommandFactory;
import io.microgenie.commands.util.CloseableUtil;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.sqs.AmazonSQSClient;



/***
 * AWS Application factory
 * @author shawn
 */
public class AwsApplicationFactory extends ApplicationFactory{

	private final AwsConfig config;
	
	private static FileStoreFactory files;
	private static EventFactory events;
	private static QueueFactory queues;
	private static DatabaseFactory databases;
	private static HttpFactory<String> http;
	private static ApplicationCommandFactory commands;
	private boolean withCommands;
	
	
	
	/**
	 * Default Constructor to close thread group factory
	 * @param config - Aws configuration
	 * @param withCommands - whether or not the {@link CommandFactory} should also be initialized
	 */
	public AwsApplicationFactory(final AwsConfig config, final boolean withCommands){
		this.config = config;
		this.withCommands = withCommands;
		this.createApplicationFactories(config);
	}
	

	private void createApplicationFactories(final AwsConfig config) {
		
		/** Always Create the Http Client Factory **/
		http = new ApacheHttpFactory();
		
		/*** Configure Resources **/
		if(config!=null){
			
			AmazonDynamoDBClient dynamoDbClient = null;
			AmazonCloudWatchClient cloudWatchClient = null;
			
			if(config.getKinesis()!=null || config.getDynamo()!=null){
				dynamoDbClient = new AmazonDynamoDBClient();
				cloudWatchClient = new AmazonCloudWatchClient();
			}
			if(config.getKinesis()!=null){
				final AmazonKinesisClient kinesisClient = new AmazonKinesisClient();	
				events = new KinesisEventFactory(kinesisClient, config.getKinesis(), dynamoDbClient, cloudWatchClient);
			}
			if(config.getS3()!=null){
				files = new S3BlobFactory(config.getS3());		
			}
			if(config.getDynamo()!=null){
				databases = new DynamoDbMapperFactory(config.getDynamo().getPackagePrefix(), dynamoDbClient);
			}
			if(config.getSqs()!=null){
				queues = new SqsFactory(new AmazonSQSClient(),config.getSqs());
			}
			if(this.withCommands){
				commands = new ApplicationCommandFactory(this);
			}
		}
		
	}


	@Override
	public HttpFactory<String> http() {
		return http;
	}
	@Override
	public EventFactory events() {
		return events;
	}
	@Override
	public QueueFactory queues() {
		return queues;
	}
	@Override
	public FileStoreFactory blobs() {
		return  files;
	}
	@Override
	public DatabaseFactory database() {
		return databases;
	}
	@Override
	public ApplicationCommandFactory commands() {
		return commands;
	}
	

	@Override
	public void initialize() {
		if(config!=null){
			if(http!=null){
				http.initialize();
			}
			if(events!=null){
				events.initialize();	
			}
			if(databases!=null){
				databases.initialize();	
			}
			if(files!=null){
				files.initialize();	
			}
			if(queues !=null){
				queues.initialize();
			}
			if(commands!=null){
				commands.initialize();
			}
		}
	}
	
	@Override
	public void registerFiles(final FileStoreFactory files) {
		AwsApplicationFactory.files = files;
	}
	@Override
	public void registerQueues(final QueueFactory queues) {
		AwsApplicationFactory.queues = queues;
	}
	@Override
	public void registerEvents(final EventFactory events) {
		AwsApplicationFactory.events = events;
	}
	@Override
	public void registerDatabase(final DatabaseFactory database) {
		AwsApplicationFactory.databases = database;
	}
	@Override
	public void registerHttp(final HttpFactory<String> http) {
		AwsApplicationFactory.http = http;
	}
	@Override
	public void registerCommands(final ApplicationCommandFactory commands) {
		AwsApplicationFactory.commands = commands;
	}
	
	
	@Override
	public void close(){
		CloseableUtil.closeQuietly(events);
		CloseableUtil.closeQuietly(databases);
		CloseableUtil.closeQuietly(files);
		CloseableUtil.closeQuietly(http);
		CloseableUtil.closeQuietly(queues);
		CloseableUtil.closeQuietly(commands);
	}
}
