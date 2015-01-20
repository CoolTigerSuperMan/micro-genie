//package io.microgenie.aws.dynamodb;
//
//import io.microgenie.application.database.KeyValueRepository;
//
//import java.util.List;
//import java.util.Map;
//
//
//
//
//
//
//
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
//import com.amazonaws.services.dynamodbv2.model.AttributeValue;
//import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
//import com.amazonaws.services.dynamodbv2.model.GetItemResult;
//import com.amazonaws.services.dynamodbv2.model.QueryResult;
//import com.google.common.collect.Lists;
//
//
///**
// * Represents a dynamodb backed repository
// * @author shawn
// */
//public class DynamoRepository implements KeyValueRepository{
//
//	private final AmazonDynamoDBClient client;
//	private final ItemConverter itemConverter = new ItemConverter();
//
//	
//	/**
//	 * 
//	 * @param client
//	 */
//	public DynamoRepository(AmazonDynamoDBClient client){
//		this.client = client;
//	}
//	
//	
//	
//	/**
//	 * 
//	 * Get a single item
//	 */
//	@Override
//	public Map<String, Object> get(final Class<?> clazz, final String idName, Object id) {
//		final GetItemRequest request = itemConverter.getItemRequest(fixName(clazz), idName, id);
//		final GetItemResult result = this.client.getItem(request);
//		return itemConverter.getItem(result);
//	}
//	
//	
//	
//	/**
//	 * 
//	 * Get a single item
//	 */
//	@Override
//	public Map<String, Object> get(final Class<?> clazz, final String idName,  Object id, final String rangeName, Object range) {
//		final GetItemRequest request = itemConverter.getItemRequest(fixName(clazz), idName, id, rangeName, range);
//		final GetItemResult result = this.client.getItem(request);
//		return itemConverter.getItem(result);
//	}
//
//	
//	
//	/**
//	 * Get query results
//	 */
//	@Override
//	public List<Map<String, Object>> query(final Class<?> clazz, Map<String, Object> predicates) {
//		final QueryResult result = this.client.query(this.itemConverter.createQueryRequest(fixName(clazz), predicates));
//		final List<Map<String, Object>> items = Lists.newArrayList();
//		for(Map<String,AttributeValue> itemAttributes : result.getItems()){
//			Map<String, Object> item  =this.itemConverter.getItem(itemAttributes);
//			items.add(item);
//		}
//		return items;
//	}
//
//	
//	
//	/**
//	 * Save an item
//	 */
//	@Override
//	public void save(final Class<?> clazz, Map<String, Object> data) {
//		this.client.putItem(fixName(clazz), this.itemConverter.createItem(data));
//	}
//
//	
//
//
//	/**
//	 * Delete an item from dynamodb
//	 */
//	@Override
//	public void delete(final Class<?> clazz, String idName, Object id){
//		this.client.deleteItem(this.itemConverter.getDeleteItemRequest(fixName(clazz), idName, id));
//	}
//	
//	
//	/**
//	 * Fix the table name
//	 * @param clazz
//	 * @return
//	 */
//	public String fixName(Class<?> clazz){
//		return clazz.getSimpleName().toLowerCase();
//	}
//}
