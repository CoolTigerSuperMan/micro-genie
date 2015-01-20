package io.microgenie.aws.dynamodb;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;





import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;


/***
 * 
 * @author shawn
 *
 */
class ItemConverter {

	/**
	 * Builds a Get Item Request instance
	 * @param key
	 * @param id
	 * @return  getItemRequest
	 */
	public GetItemRequest getItemRequest(final String tableName, final String key, final Object id){
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(tableName), "tableName cannot be null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key cannot be null or empty");
		Preconditions.checkNotNull(id, "id cannot be null or empty");
		
		final Map<String, Object> map = Maps.newHashMap();
		map.put(key, id);
				
		final GetItemRequest request = new GetItemRequest()
		.withTableName(tableName)
		.withKey(this.createKeyMap(map));
		return request;
	}
	
	
	public GetItemRequest getItemRequest(final String tableName, final String key, final Object id, final String rangeKey, final Object rangeId){
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(tableName), "tableName cannot be null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key cannot be null or empty");
		Preconditions.checkNotNull(id, "id cannot be null or empty");
		
		Preconditions.checkNotNull(rangeId, "rangeId cannot be null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(rangeKey), "rangeKey cannot be null or empty");
		
		final Map<String, Object> map = Maps.newHashMap();
		map.put(key, id);
		map.put(rangeKey, rangeId);
				
		final GetItemRequest request = new GetItemRequest()
		.withTableName(tableName)
		.withKey(this.createKeyMap(map));
		return request;
	}
	
	
	
	/***
	 * Create a key / attribute value map
	 * @param keys
	 * @return keyValue Map
	 */
	public Map<String, AttributeValue> createKeyMap(final Map<String, Object> keys){
		final Map<String, AttributeValue> keyAttributes = new HashMap<String, AttributeValue>();
		for(Entry<String, Object> keyEntry : keys.entrySet()){
			AttributeValue val = this.createAttributeValue(keyEntry.getValue());
			keyAttributes.put(keyEntry.getKey(), val);
		}
		return keyAttributes;
		
	}


	/**
	 * Create an attribute for dynamodb queries
	 * @param value
	 * @return attributeValue
	 */
	private AttributeValue createAttributeValue(final Object value) {
				Preconditions.checkNotNull(value, "value cannot be null");
		
		AttributeValue val = new AttributeValue();
		if(String.class.equals(value.getClass())){
			return val.withS(value.toString());	
		}else if(Date.class.equals(value.getClass())){
			return val.withS(value.toString());
		}else if(Number.class.equals(value.getClass())){
			return val.withN(value.toString());	
		}else if(Long.class.equals(value.getClass())){
			return val.withS(value.toString());	
		}
	
		throw new IllegalArgumentException("Unsupported data type");
	}



	/**
	 * Get bytes
	 * @param result
	 * @return
	 */
	public Map<String, Object> getItem(GetItemResult getItemResult) {
		return this.getItem(getItemResult.getItem());
	}
	
	
	/**
	 * Get the value from an Attribute Value
	 * @param val
	 * @return object
	 */
	private Object getValue(AttributeValue val){
		if(val.getBOOL()!=null){
			return val.getBOOL();
		}else if(val.getB()!=null){
			return val.getB();
		}else if(val.getBS()!=null){
			return val.getBS();
		}else if(val.getL()!=null){
			return val.getL();
		}else if(val.getM()!=null){
			return val.getM();
		}else if(val.getN()!=null){
			return val.getN();
		}else if(val.getNS()!=null){
			return val.getNS();
		}else if(val.getS()!=null){
				return val.getS();		
		}else if(val.getSS()!=null){
			return val.getSS();
		}else {
			return null;
		}
	}



	/**
	 * Get Delete Item Request
	 * 
	 * @param tableName
	 * @param idName
	 * @param id
	 * @return deleteItemRequest
	 */
	public DeleteItemRequest getDeleteItemRequest(final String tableName, final String idName, final Object id) {
		
		final Map<String, AttributeValue> keys = Maps.newHashMap();
		keys.put(idName, this.createAttributeValue(id));
		final DeleteItemRequest request = new DeleteItemRequest()
		.withTableName(tableName)
		.withKey(keys);
		
		return request;
	}


	/**
	 * Create Items 
	 * @param item
	 * @return item
	 */
	public Map<String, AttributeValue> createItem(final Map<String, Object> item) {
		final Map<String, AttributeValue> attrValues = Maps.newHashMap(); 
		for(Entry<String, Object> entry : item.entrySet()){
			attrValues.put(entry.getKey(), this.createAttributeValue(entry.getValue()));
		}
		return attrValues;
	}



	/**
	 * Create a query request
	 * @param tableName
	 * @param predicates
	 * @return queryRequest
	 */
	public QueryRequest createQueryRequest(final String tableName, final Map<String, Object> predicates) {
		QueryRequest request = new QueryRequest().withTableName(tableName);
		for(Entry<String, Object> predicateEntry : predicates.entrySet()){
			request.withQueryFilter(this.createQueryFilter(predicateEntry));
		}
		return request;
	}



	/***
	 * Create a queryMap
	 * @param predicateEntry
	 * @return conditionMap
	 */
	private Map<String, Condition> createQueryFilter(final Entry<String, Object> predicateEntry) {
		
		final Condition condition = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ)
		.withAttributeValueList(this.createAttributeValue(predicateEntry.getValue()));
		
		final Map<String, Condition> conditionMap = Maps.newHashMap();
		conditionMap.put(predicateEntry.getKey(), condition);
		return conditionMap;
	}



	/**
	 * Get an item from an attribute map
	 * @param item
	 * @return
	 */
	public Map<String, Object> getItem(Map<String, AttributeValue> item) {
		final Map<String, Object> result = Maps.newHashMap();
		for(Entry<String, AttributeValue> entry : item.entrySet()){
			result.put(entry.getKey(), this.getValue(entry.getValue()));
		}
		return result;
	}
}
