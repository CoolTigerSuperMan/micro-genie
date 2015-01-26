package io.microgenie.aws.dynamodb;

import io.microgenie.application.database.DatabaseFactory;

import java.io.IOException;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/***
 * An implementation of {@link DatabaseFactory} based on the AWS Java SDK dynamodb mapper, 
 * for object persistence
 * 
 * Create a {@link DatabaseFactory} that implements behavior of the {@link DynamoDBMapper} but also
 * includes initialization of any scanned models that are annotated with {@link DynamoDBTable}.
 * 
 * Any models that are found from the scan will be further scanned for fields and indexes to create
 * 
 * 
 * @author shawn
 */
public class DynamoDbMapperFactory extends DatabaseFactory {

	
	private final DynamoMapperRepository mapperRepo;
//	private final DynamoAdmin admin;
//	private final String packageScanPrefix;
	
	
	/***
	 * Create a {@link DatabaseFactory} that implements behavior of the {@link DynamoDBMapper} but also
	 * includes initialization of any scanned models that are annotated with {@link DynamoDBTable}.
	 * 
	 * Any models that are found from the scan will be further scanned for fields and indexes to create

	 * @param client - The AmazonDynamoDBClient {@link AmazonDynamoDBClient}
	 */
	public DynamoDbMapperFactory(final AmazonDynamoDBClient client) {
		this.mapperRepo = DynamoMapperRepository.create(client);
//		this.packageScanPrefix = packageScanPrefix;
//		this.admin = new DynamoAdmin(client);
	}

	
	
	/***
	 * The causes the models to be scanned for annotated classes that model dynamodb tables for object persistence
	 */
//	@Override
//	public void initialize() {
//		//this.admin.scan(this.packageScanPrefix);
//	}
	
	
	
	public DynamoMapperRepository getMapperRepository(){
		return this.mapperRepo;
	}


	@Override
	public void close() throws IOException {

	}
}
