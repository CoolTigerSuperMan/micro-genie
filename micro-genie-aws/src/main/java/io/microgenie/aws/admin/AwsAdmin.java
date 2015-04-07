package io.microgenie.aws.admin;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;


/***
 * AWS Admin Utilities used to create resources
 * @author shawn
 *
 */
public class AwsAdmin {

	public DynamoAdmin createDynamoAdmin(final AmazonDynamoDBClient dynamoClient){
		return new DynamoAdmin(dynamoClient);
	}
	public KinesisAdmin createKinesisAdmin(final AmazonKinesisClient kinesisClient){
		return new KinesisAdmin(kinesisClient);
	}
	public S3Admin createS3Admin(final AmazonS3Client s3Client){
		return new S3Admin(s3Client);
	}
	public SqsQueueAdmin createSqsAdmin(final AmazonSQSClient sqsClient){
		return new SqsQueueAdmin(sqsClient);
	}
	public static AwsAdmin create(){
		return new AwsAdmin();
	}
}
