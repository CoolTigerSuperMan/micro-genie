package io.microgenie.aws.dynamodb;

import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.database.KeyValueRepository;
import io.microgenie.aws.DynamoDbConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


/***
 * 
 * @author shawn
 **/
public class DynamoDbFactory extends DatabaseFactory{
	
	private final Map<String, KeyValueRepository> repositories = new ConcurrentHashMap<String, KeyValueRepository>();
	
	//private final AmazonDynamoDBClient client;
	private final DynamoMapperRepository mapperRepository;
	

	
	/***
	 * @param dynamoConfig
	 */
	public DynamoDbFactory(final DynamoDbConfig dynamoConfig) {
		this(new AmazonDynamoDBClient(),  dynamoConfig);
	}
	
	
	/****
	 * DynamoDb Factory
	 * 
	 * @param dynamoClient
	 * @param dynamoConfig
	 */
	public DynamoDbFactory(final AmazonDynamoDBClient dynamoClient, final DynamoDbConfig dynamoConfig) {
		this.mapperRepository = DynamoMapperRepository.create(dynamoClient);
	}
	

	@Override
	public void close(){}
	
	
	public DynamoMapperRepository getMapperRepository() {
		return mapperRepository;
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
