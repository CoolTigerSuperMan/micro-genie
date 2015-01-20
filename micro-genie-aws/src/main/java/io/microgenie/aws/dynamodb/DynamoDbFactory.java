package io.microgenie.aws.dynamodb;

import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.database.KeyValueRepository;
import io.microgenie.aws.DynamoDbConfig;
import io.microgenie.aws.DynamoDbConfig.Key;
import io.microgenie.aws.DynamoDbConfig.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;


/***
 * 
 * @author shawn
 **/
public class DynamoDbFactory extends DatabaseFactory{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbFactory.class);
	
	private final Map<String, KeyValueRepository> repositories = new ConcurrentHashMap<String, KeyValueRepository>();
	
	private AmazonDynamoDBClient client; 
	private DynamoDbConfig config;

	
	/***
	 * @param dynamoDbConfig
	 * @param tgFactory
	 */
	public DynamoDbFactory(final DynamoDbConfig dynamoDbConfig) {
		this.config = dynamoDbConfig;
		this.client = new AmazonDynamoDBClient();
	}
	
	
	/****
	 * DynamoDb Factory
	 * 
	 * @param dynamoClient
	 * @param dynamoDbConfig
	 * @param tgFactory
	 */
	public DynamoDbFactory(final AmazonDynamoDBClient dynamoClient, final DynamoDbConfig dynamoDbConfig) {
		this.config = dynamoDbConfig;
		this.client = dynamoClient;
	}
	
	
	

	/**
	 * Initialize Tables. creates tables as long as they do not exist
	 */
	@Override
	public void initialize(){
		if(config !=null && config.getTables()!=null){
			for(Table table : config.getTables()){
				try{
					final DescribeTableResult result = client.describeTable(new DescribeTableRequest(table.getName()));
					if(result!=null){
						LOGGER.info("Table: {} was found with status: {}", result.getTable().getTableName(), result.getTable().getTableStatus());
					}
				}catch(ResourceNotFoundException rnf){
					LOGGER.info("Table: {} was not found. Attempting to create", table);
					CreateTableRequest tableRequest = this.createTableRequest(table);
					client.createTable(tableRequest);					
				}
			}
		}
	}





	/***
	 * Create the Table Request
	 * @param table
	 * @return
	 */
	private CreateTableRequest createTableRequest(final Table table) {
		
		final ProvisionedThroughput throughput = this.createProvisionedThroughput(table.getReadCapacityUnits(), table.getWriteCapacityUnits());
		final List<KeySchemaElement> keys = this.createKeySchemaElements(table.getKeys());


		final CreateTableRequest tableRequest = new CreateTableRequest(table.getName(), keys)
			.withProvisionedThroughput(throughput);

		/***
		 * Set Indexes
		 */
		final List<LocalSecondaryIndex> localSecondaryIndexes = this.createLocalSecondaryIndexes(table.getLocalSecondaryIndexes());
		final List<GlobalSecondaryIndex> globalSecondaryIndexes = this.createGlobalSecondaryIndexes(table.getGlobalSecondaryIndexes());
		
		
		/** Local Secondary Indexes **/
		if(localSecondaryIndexes!=null){
			tableRequest.withLocalSecondaryIndexes(localSecondaryIndexes);
		}
		
		/** Global Secondary Indexes **/
		if(globalSecondaryIndexes!=null){
			tableRequest.withGlobalSecondaryIndexes(globalSecondaryIndexes);
		}
		
		/** Set Attribute Definitions **/
		final List<AttributeDefinition> attributeDefinitions = this.createAttributeDefinitions(table.getAttributeDefinitions());
		tableRequest.withAttributeDefinitions(attributeDefinitions);
		return tableRequest;
	}




	private List<AttributeDefinition> createAttributeDefinitions(final List<io.microgenie.aws.DynamoDbConfig.AttributeDefinition> attributeDefinitions) {
		
		List<AttributeDefinition> attributeDefs = null;
		
		if(attributeDefinitions!=null){
			attributeDefs = new ArrayList<AttributeDefinition>();
			for(DynamoDbConfig.AttributeDefinition configAttr : attributeDefinitions){
				final AttributeDefinition attrDef = new AttributeDefinition()
					.withAttributeName(configAttr.getName())
					.withAttributeType(configAttr.getType());
				attributeDefs.add(attrDef);
			}
		}
		return attributeDefs;
	}





	/***
	 * Create The list of Global Secondary Indexes if they exist
	 * @param globalSecondaryIndexes
	 * @return globalSecondaryIndexList
	 */
	private List<GlobalSecondaryIndex> createGlobalSecondaryIndexes(final List<io.microgenie.aws.DynamoDbConfig.GlobalSecondaryIndex> globalSecondaryIndexes) {
		List<GlobalSecondaryIndex> indexes = null;
		if(globalSecondaryIndexes!=null){
			indexes = new ArrayList<GlobalSecondaryIndex>();
			for(DynamoDbConfig.GlobalSecondaryIndex configGlobalIndex : globalSecondaryIndexes){
				final GlobalSecondaryIndex gi = new GlobalSecondaryIndex();
				gi.withIndexName(configGlobalIndex.getName());
				gi.withProjection(this.createProjection(configGlobalIndex.getProjection()));
				gi.withProvisionedThroughput(this.createProvisionedThroughput(configGlobalIndex.getReadCapacityUnits(), configGlobalIndex.getWriteCapacityUnits()));
				gi.withKeySchema(this.createKeySchemaElements(configGlobalIndex.getKeys()));
				indexes.add(gi);
			}
		}
		return indexes;
	}





	
	/***
	 * Create the dynamoDb Projection
	 * @param projection
	 * @return projection
	 */
	private Projection createProjection(final io.microgenie.aws.DynamoDbConfig.Projection projection) {

		final Projection p = new Projection();
		p.withNonKeyAttributes(projection.getNonKeyAttributes());
		p.withProjectionType(projection.getType());
		return p;
	}




	/***
	 * Create Local Secondary Indexes
	 * @param localSecondaryIndexes
	 * @return localIndexList
	 */
	private List<LocalSecondaryIndex> createLocalSecondaryIndexes(List<io.microgenie.aws.DynamoDbConfig.LocalSecondaryIndex> localSecondaryIndexes) {
		List<LocalSecondaryIndex>  indexes = null;
		if(localSecondaryIndexes!=null){
			indexes = new ArrayList<LocalSecondaryIndex>();	
			for(DynamoDbConfig.LocalSecondaryIndex configIndex : localSecondaryIndexes){
				LocalSecondaryIndex index = new LocalSecondaryIndex()
				.withIndexName(configIndex.getName())
				.withProjection(this.createProjection(configIndex.getProjection()))
				.withKeySchema(this.createKeySchemaElements(configIndex.getKeys()));
				indexes.add(index);
			}
		}
		return indexes;
	}





	/**
	 * Create the provisioned throughput for Tables and Global Indexes
	 * @param readCapacityUnits
	 * @param writeCapacityUnits
	 * @return throughput
	 */
	private ProvisionedThroughput createProvisionedThroughput(final Long readCapacityUnits, final Long writeCapacityUnits) {
		final ProvisionedThroughput throughput = new ProvisionedThroughput();
		throughput.withReadCapacityUnits(readCapacityUnits);
		throughput.withWriteCapacityUnits(writeCapacityUnits);
		return throughput;
	}

	
	/***
	 * Create the list of key Schema Elements for the Key Set
	 * @param keys
	 * @return keySchemaElementList
	 */
	private List<KeySchemaElement> createKeySchemaElements(final List<Key> keys){
		final List<KeySchemaElement>  keySchemaElements = new ArrayList<KeySchemaElement>();
		for(Key key : keys){
			keySchemaElements.add(this.createKeySchemaElement(key));
		}
		return keySchemaElements;
	}
	
	
	/**
	 * Create a single KeySchemaElement
	 * @param key
	 * @return keySchemaElement;
	 */
	private KeySchemaElement createKeySchemaElement(Key key) {
		final KeySchemaElement keyElement = new KeySchemaElement();
		keyElement.setAttributeName(key.getAttributeName());
		keyElement.setKeyType(key.getKeyType());
		return keyElement;
	}	
	
	@Override
	public void close(){
		this.client.shutdown();
	}
	
	
	/***
	 * Get a registered repository
	 * @param key
	 * @return repository
	 */
	public synchronized KeyValueRepository getRepo(final String key) {
		return repositories.get(key);
	}
	
	/**
	 * Register Repositories
	 * @param key
	 * @param repo
	 */
	public synchronized void registerRepositories(final String key, KeyValueRepository repo) {
		if(!repositories.containsKey(key)){
			this.repositories.put(key, repo);
		}
	}
}
