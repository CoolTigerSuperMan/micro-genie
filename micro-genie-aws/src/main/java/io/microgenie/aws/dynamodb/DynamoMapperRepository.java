package io.microgenie.aws.dynamodb;

import io.microgenie.application.database.EntityDatabusRepository.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;


/***
 * Entity Repository for DynamoDb. 
 * 
 * The purpose is to encapsulate common calls against
 * the dynamoDb mapper that are needed to perform common tasks for
 * all repositories
 * 
 * @author shawn
 *
 */
public class DynamoMapperRepository  {


	private final DynamoDBMapper mapper;

	
	/***
	 * A configured {@link DynamoDBMapper} is expected
	 * @param mapper
	 */
	public DynamoMapperRepository(final DynamoDBMapper mapper) {
		this.mapper = mapper;
	}
	
	
	/***
	 * Get the item with the given hash key
	 * @param clazz
	 * @param key
	 * @return item - Of type T
	 */
	public <T> T get(final Class<T> clazz, final Key key) {
		if(!Strings.isNullOrEmpty(key.getHash()) && !Strings.isNullOrEmpty(key.getRange())){
			return this.mapper.load(clazz, key.getHash(), key.getRange());	
		}else if(!Strings.isNullOrEmpty(key.getHash())){
			return this.mapper.load(clazz, key.getHash());
		}else {
			throw new IllegalArgumentException();
		}
	}
	
	
	
	/***
	 * Get multiple items in batch
	 * @param itemKeys - instances with the keys set
	 * @return itemList
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(List<T> itemKeys) {
		final List<T> results = new ArrayList<T>();
		final List<Object> itemsToGet = new ArrayList<Object>();
		for(T item : itemKeys){
			itemsToGet.add(item);	
		}
		final Map<String, List<Object>> tables = this.mapper.batchLoad(itemsToGet);
		if(tables!=null){
			for(Entry<String, List<Object>> table : tables.entrySet()){
				for(Object item : table.getValue()){
					results.add(((T)item));
				}
			}			
		}
		return results;
	}

	
	/**
	 * Save the given item To DynamoDb
	 * @param item
	 */
	public <T> void  save(final T item){
		this.mapper.save(item);
	}

	
	/**
	 * Save the given items To DynamoDb
	 * @param items
	 */
	public <T> void  save(final List<T> items){
		this.mapper.batchSave(items);
	}
	
	
	/**
	 * Delete the item from Dynamodb
	 * @param item
	 */
	public <T> void delete(final T item){
		this.mapper.delete(item);
	}
	
	
	
	
	/***
	 * Query and Index by hash key only, expecting multiple results.
	 * <br>
	 * The query expects multiple results 
	 * 
	 * @param clazz - The model to query
	 * @param itemKey - An instance of the model class with values in the predicate fields
	 * @param indexName - The index to query
	 * @param limit - The number of results to return
	 * @return results
	 */
	public <T> List<T> queryIndexHashKey(final Class<T> clazz, final T itemKey, final String indexName, final int limit){
		return this.query(clazz, itemKey, indexName, null, null, limit, false);
	}
	

	
	/****
	 * 
	 * Queries a dynamodb table Index by the hash key only, expecting multiple results. 
	 * This query allows a read consistency option to be specified
	 * 
	 * @param clazz - The model to query
	 * @param itemKey - An instance of the model class with values in the predicate fields
	 * @param indexName - The index to use
	 * @param limit - The records to return
	 * @param consistentRead - Whether or not to perform a consistent read
	 * @return results - List<T>
	 */
	public <T> List<T> queryIndexHashKey(final Class<T> clazz, final T itemKey, final String indexName, final int limit, final boolean consistentRead){
		return this.query(clazz, itemKey, indexName, null, null, limit, consistentRead);
	}
	
	
	/***
	 * Query a dynamodb Index by a hash key and RangeKey.  
	 * 
	 * This does not execute a consistent read
	 * 
	 * @param clazz - The model to query
	 * @param itemKey - An instance of the model class with values in the predicate fields
	 * @param indexName - The index to query
	 * @param rangeKeyName - The field name of the Index rangeKey
	 * @param condition - The condition to apply to the rangeKey
	 * @param limit - The number of results to return 
	 * @return results
	 */
	public <T> List<T> queryIndexHashAndRangeKey(final Class<T> clazz, final T itemKey, final String indexName, final String rangeKeyName, Condition condition, final int limit){
		
		Map<String, Condition> rangeConditions = null;
		ConditionalOperator conditionOperator = null;
		if(condition!=null){
			rangeConditions = Maps.newHashMap();
			rangeConditions.put(rangeKeyName, condition);
			//conditionOperator = ConditionalOperator.AND; // Leave this out for now
		}
		return this.query(clazz, itemKey, indexName, conditionOperator, rangeConditions, limit, false);
	}
	
	


	/**
	 * Run Queries against dynamodb
	 * @param clazz
	 * @param itemKey
	 * @param indexName
	 * @param operator
	 * @param rangeKeyConditions
	 * @param limit
	 * @param consistentRead
	 * @return items
	 */
	public <T> List<T> query(final Class<T> clazz, final T itemKey, final String indexName, final ConditionalOperator operator,  final Map<String, Condition> rangeKeyConditions, final int limit, final boolean consistentRead){
		
		final DynamoDBQueryExpression<T> expression = new DynamoDBQueryExpression<T>();
		expression.withHashKeyValues(itemKey);
		expression.withConsistentRead(consistentRead);
		
		/**
		 * Optional Query parameters
		 */
		if(!Strings.isNullOrEmpty(indexName)){
			expression.withIndexName(indexName);
		}
		if(operator!=null){
			expression.withConditionalOperator(operator);
		}
		if(rangeKeyConditions != null && rangeKeyConditions.size()>0){
			expression.withRangeKeyConditions(rangeKeyConditions);
		}
		if(limit>0){
			expression.withLimit(limit);	
		}
		List<T> items = this.query(clazz, expression);
		if(items==null){
			return new ArrayList<T>();
		}else{
			return items;
		}
	}
	
	
	
	/***
	 * Get range keys for a given hashKey
	 * @param clazz
	 * @param itemHash
	 * @return rangeKeys
	 */
	public <T> List<T> getRanges(final Class<T> clazz, final T itemHash){
		return this.getRanges(clazz, itemHash, 0, null);		
	}
	
	
	
	/***
	 * Get range keys for a given hashKey
	 * @param clazz
	 * @param itemHash
	 * @return rangeKeys
	 */
	public <T> List<T> getRanges(final Class<T> clazz, final T itemHash, int limit, Map<String, AttributeValue> startKey){
		
		final Map<String, AttributeValue> starKey = Maps.newHashMap();
		final DynamoDBQueryExpression<T> query = new DynamoDBQueryExpression<T>();
		query.withHashKeyValues(itemHash);
		if(limit>0 && starKey!=null){
			query.withLimit(limit);
			query.withExclusiveStartKey(starKey);
		}
		return this.mapper.query(clazz, query);		
	}
	

	
	
	
	/***
	 * Query DynamoDb using the given query expression
	 * @param clazz - The Java class specifying the return type
	 * @param queryExpression - The dynamoDb query expression
	 * @return paginatedList
	 */
	public <T> PaginatedQueryList<T> query(final Class<T> clazz, DynamoDBQueryExpression<T> queryExpression){
		final PaginatedQueryList<T> itemList = mapper.query(clazz, queryExpression);
		return itemList;
	}
	
	
	
	
	/***
	 * 
	 * Query DynamoDb using the given scan expression
	 * @param clazz - The Java class specifying the return type
	 * @param scanExpression - The dynamoDb query expression
	 * @return paginatedScanList
	 */
	public <T> PaginatedScanList<T> query(final Class<T> clazz, DynamoDBScanExpression scanExpression){
		final PaginatedScanList<T> itemList = mapper.scan(clazz, scanExpression);
		return itemList;
	}
	
	
	
	
	/***
	 * Conditionally Save the item if the expected attribute is found to be true
	 * @param item
	 * @param conditional
	 * @param attributeName
	 * @param expectedAttribute
	 */
	public <T> void saveIf(final T item, ConditionalOperator conditional, final String attributeName, final ExpectedAttributeValue expectedAttribute){
		
		final DynamoDBSaveExpression expression = new DynamoDBSaveExpression()
		.withExpectedEntry(attributeName, expectedAttribute);
		if(conditional !=null){
			expression.withConditionalOperator(conditional);
		}
		this.mapper.save(item, expression);
	}
	
	
	
	/***
	 * Conditionally Save the items  if the expected attributes are found to be true
	 * 
	 * @param item
	 * @param conditional
	 * @param expectedAttributes
	 */
	public <T> void saveIf(final T item, ConditionalOperator conditional, final Map<String, ExpectedAttributeValue> expectedAttributes){
		final DynamoDBSaveExpression expression = new DynamoDBSaveExpression()
		.withConditionalOperator(conditional)
		.withExpected(expectedAttributes);
		this.mapper.save(item, expression);
	}
	
	
	
	/***
	 * Construct an expected attribute value
	 * 
	 * @param operator
	 * @param value
	 * @param exists
	 * 
	 * @return expectedAttributeValue
	 */
	public static ExpectedAttributeValue expected(final ComparisonOperator operator, final String value, final boolean exists){
		return new ExpectedAttributeValue()
		.withComparisonOperator(operator)
		.withExists(exists)
		.withValue(new AttributeValue(value));
	}
	
	
	
	/***
	 * Construct an expected attribute value
	 * 
	 * @param operator
	 * @param values
	 * @param exists
	 * @return expectedAttributeValue
	 */
	public static ExpectedAttributeValue expected(final ComparisonOperator operator, final List<String> values, final boolean exists){
		return new ExpectedAttributeValue()
		.withComparisonOperator(operator)
		.withExists(exists)
		.withValue(new AttributeValue(values));
	}


	
	
	
	/***
	 * Create a DynamoMapper repository. This defaults the SaveBehavior to {@link SaveBehavior#UPDATE}
	 * 
	 * @param dynamoClient
	 * @return mapperRepository - {@link DynamoMapperRepository}
	 */
	public static DynamoMapperRepository create(final AmazonDynamoDBClient dynamoClient) {
		return DynamoMapperRepository.create(dynamoClient, new DynamoDBMapperConfig(SaveBehavior.UPDATE));
	}
	
	
	
	/***
	 * Create a DynamoMapper repository
	 * 
	 * @param dynamoClient - {@link AmazonDynamoDBClient}
	 * @param config - {@link DynamoDBMapperConfig} 
	 * @return mapperRepository - {@link DynamoMapperRepository}
	 */
	public static DynamoMapperRepository create(final AmazonDynamoDBClient dynamoClient, final DynamoDBMapperConfig config) {
		final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoClient);
		final DynamoMapperRepository mapperRepository = new DynamoMapperRepository(dynamoDBMapper);
		return mapperRepository;
	}
}
