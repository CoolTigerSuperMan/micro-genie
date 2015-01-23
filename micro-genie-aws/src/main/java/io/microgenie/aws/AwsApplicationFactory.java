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
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;



/***
 * AWS Application factory
 * @author shawn
 */
public class AwsApplicationFactory extends ApplicationFactory{

	private volatile boolean isInitialized;
	
	private final AwsConfig config;
	
	private FileStoreFactory files;
	private EventFactory events;
	private QueueFactory queues;
	private DatabaseFactory databases;
	private HttpFactory<String> http;
	private ApplicationCommandFactory commands;
	private boolean withCommands;
	
	private AmazonDynamoDBClient dynamoClient;
	private AmazonCloudWatchClient cloudwatchClient;
	private AmazonKinesisClient kinesisClient;

	private AmazonSQSClient sqsClient;
	private AmazonS3Client s3Client;
	
	
	
	
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
			
			if(config.getKinesis()!=null || config.getDynamo()!=null){
				this.dynamoClient = new AmazonDynamoDBClient();
				if(this.config.getKinesis()!=null){
					this.cloudwatchClient = new AmazonCloudWatchClient();	
				}
			}
			if(config.getKinesis()!=null){
				this.kinesisClient = new AmazonKinesisClient();	
				events = new KinesisEventFactory(kinesisClient, config.getKinesis(), this.dynamoClient, this.cloudwatchClient);
			}
			if(config.getS3()!=null){
				this.s3Client = new AmazonS3Client();
				files = new S3BlobFactory(this.s3Client, config.getS3());		
			}
			if(config.getDynamo()!=null){
				databases = new DynamoDbMapperFactory(config.getDynamo().getPackagePrefix(), this.dynamoClient);
			}
			if(config.getSqs()!=null){
				this.sqsClient = new AmazonSQSClient();
				queues = new SqsFactory(this.sqsClient, config.getSqs());
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
	public synchronized void initialize() {
		if(config!=null){
			if(!this.isInitialized){
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
	}
	
	@Override
	public void registerFiles(final FileStoreFactory files) {
		this.files = files;
	}
	@Override
	public void registerQueues(final QueueFactory queues) {
		this.queues = queues;
	}
	@Override
	public void registerEvents(final EventFactory events) {
		this.events = events;
	}
	@Override
	public void registerDatabase(final DatabaseFactory database) {
		this.databases = database;
	}
	@Override
	public void registerHttp(final HttpFactory<String> http) {
		this.http = http;
	}
	@Override
	public void registerCommands(final ApplicationCommandFactory commands) {
		this.commands = commands;
	}
	
	
	@Override
	public void close(){
		

		CloseableUtil.closeQuietly(databases);
		CloseableUtil.closeQuietly(files);
		CloseableUtil.closeQuietly(queues);
		CloseableUtil.closeQuietly(http);
		CloseableUtil.closeQuietly(events);
		CloseableUtil.closeQuietly(commands);
		if (this.kinesisClient!=null){
			this.kinesisClient.shutdown();
		}
		if(this.cloudwatchClient!=null){
			this.cloudwatchClient.shutdown();	
		}	
		if(this.sqsClient!=null){
			this.sqsClient.shutdown();	
		}
		if(this.dynamoClient!=null){
			this.dynamoClient.shutdown();	
		}
		if(this.kinesisClient!=null){
			this.kinesisClient.shutdown();	
		}
		if(this.s3Client !=null){
			this.s3Client.shutdown();	
		}
	}


	public boolean isInitialized() {
		return isInitialized;
	}
}
