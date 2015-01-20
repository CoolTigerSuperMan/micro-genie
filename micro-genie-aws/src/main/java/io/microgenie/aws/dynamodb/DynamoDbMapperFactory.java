package io.microgenie.aws.dynamodb;

import io.microgenie.application.database.DatabaseFactory;

import java.io.IOException;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

/***
 * A database repository factory based on the aws java sdk dynamodb mapper, for object persistence
 * @author shawn
 */
public class DynamoDbMapperFactory extends DatabaseFactory {

	private final DynamoMapperRepository mapperRepo;
	
	private final DynamoAdmin admin;
	private final String packageScanPrefix;
	private final AmazonDynamoDBClient client;
	
	private boolean shouldCloseClient;
	
	
	public DynamoDbMapperFactory(final String packageScanPrefix){
		this(packageScanPrefix, new AmazonDynamoDBClient());
		this.shouldCloseClient = true;
	}
	
	
	public DynamoDbMapperFactory(final String packageScanPrefix, final AmazonDynamoDBClient client) {
		this.packageScanPrefix = packageScanPrefix;
		this.client = client;
		final DynamoDBMapperConfig config = new DynamoDBMapperConfig(SaveBehavior.UPDATE);
		final DynamoDBMapper mapper = new DynamoDBMapper(client, config);
		this.mapperRepo = new DynamoMapperRepository(mapper);
		this.admin = new DynamoAdmin(client);
	}

	
	@Override
	public void initialize() {
		this.admin.scan(this.packageScanPrefix);
	}
	
	protected DynamoMapperRepository getMapperRepo(){
		return this.mapperRepo;
	}


	@Override
	public void close() throws IOException {
		if(this.shouldCloseClient){
			this.client.shutdown();
		}
	}
}
