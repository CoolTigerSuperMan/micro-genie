package io.microgenie.aws;

import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.aws.dynamodb.DynamoDbMapperFactory;
import io.microgenie.aws.kinesis.KinesisEventFactory;
import io.microgenie.aws.s3.S3BlobFactory;
import io.microgenie.aws.sqs.SqsFactory;
import io.microgenie.commands.util.CloseableUtil;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;

/***
 * AWS Application factory
 * 
 * @author shawn
 */
public class AwsApplicationFactory extends ApplicationFactory {


	private final AwsConfig config;

	private S3BlobFactory files;
	private KinesisEventFactory events;
	private SqsFactory queues;
	private DynamoDbMapperFactory databases;

	/** Amazon Clients **/
	private AmazonDynamoDBClient dynamoClient;
	private AmazonCloudWatchClient cloudwatchClient;
	private AmazonKinesisClient kinesisClient;
	private AmazonSQSClient sqsClient;
	private AmazonS3Client s3Client;

	/**
	 * Default Constructor to close thread group factory
	 * 
	 * @param config
	 *            - Aws configuration
	 */
	public AwsApplicationFactory(final AwsConfig config) {
		this.config = config;
		this.createConfiguredFactories(config);
	}

	/***
	 * Implementations that subclass {@link AwsApplicationFactory} should
	 * override this method to create implementation specific factories for
	 * events, http, queues, etc.. where an aws service is not being used.
	 * 
	 * @param config
	 */
	protected void createConfiguredFactories(final AwsConfig config) {

		
		/*** Create any clients that have been configured **/
		if (config != null) {
			
			if (config.getKinesis() != null || config.getDynamo() != null) {

				/** both kinesis and dynamodb rely on the AmazonDynamoDbClient **/
				this.dynamoClient = new AmazonDynamoDBClient();
				
				/** Kinesis KCL uses the cloudwatchClient **/
				if (this.config.getKinesis() != null) {
					this.kinesisClient = new AmazonKinesisClient();
					this.cloudwatchClient = new AmazonCloudWatchClient();
					events = new KinesisEventFactory(kinesisClient, this.dynamoClient, this.cloudwatchClient);
				}
			}
			
			if (config.getS3() != null) {
				this.s3Client = new AmazonS3Client();
				files = new S3BlobFactory(this.s3Client, config.getS3().getDefaultDrive());
			}
			if (config.getDynamo() != null) {
				databases = new DynamoDbMapperFactory(this.dynamoClient);
			}
			if (config.getSqs() != null) {
				this.sqsClient = new AmazonSQSClient();
				queues = new SqsFactory(this.sqsClient, config.getSqs());
			}
		}
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
		return files;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends DatabaseFactory> T database() {
		return (T)databases;
	}

//	@Override
//	public ApplicationCommandFactory commands() {
//		return commands;
//	}


	@Override
	public void close() {

		CloseableUtil.closeQuietly(databases);
		CloseableUtil.closeQuietly(files);
		CloseableUtil.closeQuietly(queues);
		CloseableUtil.closeQuietly(events);
		
//		CloseableUtil.closeQuietly(http);
//		CloseableUtil.closeQuietly(commands);
		
		if (this.kinesisClient != null) {
			this.kinesisClient.shutdown();
		}
		if (this.cloudwatchClient != null) {
			this.cloudwatchClient.shutdown();
		}
		if (this.sqsClient != null) {
			this.sqsClient.shutdown();
		}
		if (this.dynamoClient != null) {
			this.dynamoClient.shutdown();
		}
		if (this.kinesisClient != null) {
			this.kinesisClient.shutdown();
		}
		if (this.s3Client != null) {
			this.s3Client.shutdown();
		}
	}
}
