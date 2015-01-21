package io.microgenie.aws.dynamodb;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
public class DynamoMapperRepository {


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
	 * @param id
	 * @return item - Of type T
	 */
	public <T> T get(final Class<T> clazz, final Object id) {
		return this.mapper.load(clazz, id);
	}
	
	
	/**
	 * Get the Item with the given hash key and range key
	 * @param clazz
	 * @param id
	 * @param rangeKey
	 * @return item - of Type T
	 */
	public <T> T get(final Class<T> clazz, Object id, final Object rangeKey) {
		return this.mapper.load(clazz, id, rangeKey);
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
	
	
	
	public <T> List<T> query(final Class<T> clazz, final T itemKey, final String indexName, final String rangeKeyField, Condition condition, final int limit){
		final Map<String, Condition> rangeConditions = Maps.newHashMap();
		rangeConditions.put(rangeKeyField, condition);
		return this.query(clazz, itemKey, null, null, null, limit, false);
	}
	public <T> List<T> query(final Class<T> clazz, final T itemKey, final int limit){
		return this.query(clazz, itemKey, null, null, null, limit, false);
	}
	public <T> List<T> query(final Class<T> clazz, final T itemKey, final int limit, final boolean consistentRead){
		return this.query(clazz, itemKey, null, null, null, limit, consistentRead);
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
		if(operator!=null){
			expression.withConditionalOperator(operator);
		}
		if(!Strings.isNullOrEmpty(indexName)){
			expression.withIndexName(indexName);
		}
		if(rangeKeyConditions != null && rangeKeyConditions.size()>0){
			expression.withRangeKeyConditions(rangeKeyConditions);
		}
		if(limit>0){
			expression.withLimit(limit);	
		}
		return this.query(clazz, expression);
	}
	
	
	/***
	 * Query DynamoDb using the given query expression
	 * @param clazz
	 * @param expression
	 * @return
	 */
	public <T> PaginatedQueryList<T> query(final Class<T> clazz, DynamoDBQueryExpression<T> expression){
		final PaginatedQueryList<T> itemList = mapper.query(clazz, expression);
		return itemList;
	}
	
	
	public <T> PaginatedScanList<T> query(final Class<T> clazz, DynamoDBScanExpression expression){
		final PaginatedScanList<T> itemList = mapper.scan(clazz, expression);
		return itemList;
	}
	
	
	/***
	 * Conditionally Save the item
	 * 
	 * @param item
	 * @param conditional
	 * @param expectedAttributes
	 */
	public <T> void saveIf(final T item, ConditionalOperator conditional, final String attributeName, final ExpectedAttributeValue expectedAttribute){
		DynamoDBSaveExpression expression = new DynamoDBSaveExpression()
		.withConditionalOperator(conditional)
		.withExpectedEntry(attributeName, expectedAttribute);
		this.mapper.save(item, expression);
	}
	
	
	
	/***
	 * Conditionally Save the item
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
	 * @param value
	 * @param exists
	 * @return expectedAttributeValue
	 */
	public static ExpectedAttributeValue expected(final ComparisonOperator operator, final List<String> value, final boolean exists){
		return new ExpectedAttributeValue()
		.withComparisonOperator(operator)
		.withExists(exists)
		.withValue(new AttributeValue(value));
	}


	/***
	 * Create a DynamoMapper repository
	 * 
	 * @param amazonDynamoDBClient
	 * @return mapperRepository - {@link DynamoMapperRepository}
	 */
	public static DynamoMapperRepository create(final AmazonDynamoDBClient amazonDynamoDBClient) {
		final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(amazonDynamoDBClient);
		final DynamoMapperRepository mapperRepository = new DynamoMapperRepository(dynamoDBMapper);
		return mapperRepository;
	}
}
