package io.microgenie.aws.dynamodb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/***
 * DynamoDb Admin, used to create Tables and indexes programmatically
 * @author shawn
 *
 */
public class DynamoAdmin {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoAdmin.class);
	private static final long DEFAULT_PROVISIONED_THROUGHPUT_VALUE = 10;
	private static final long DEFAULT_PAUSE_TIME_SECONDS = 5;
	private static final long DEFAULT_MAX_BLOCK_TIME_SECONDS = 45;
	
	
	private static final ProvisionedThroughput DEFAULT_PROVISIONED_THROUGHPUT = new ProvisionedThroughput(DEFAULT_PROVISIONED_THROUGHPUT_VALUE, DEFAULT_PROVISIONED_THROUGHPUT_VALUE);
	
	private AmazonDynamoDBClient client;

	public DynamoAdmin(AmazonDynamoDBClient client){
		this.client = client;
	}
	
	
	
	/****
	 * Scan classes in the specified package prefix to determine which tables and indexes need to be created
	 * <p>
	 * This method will use default settings which block until the tables become active or until the blocking timeout period
	 * expires. The maximum blocking timeout period per table is 45 seconds
	 * <p>
	 * @param packagePrefix
	 */
	public void scan(final String packagePrefix){
		this.scan(packagePrefix, true, DEFAULT_MAX_BLOCK_TIME_SECONDS);
	}
	
	
	/***
	 * Scan classes to determine which tables and indexes need to be created
	 * 
	 * TODO - DynamoDB has a limit of how many tables can be created at once, I think 10 as of now.
	 * This method does not batch but really needs to, so it only tries to create up 10 tables at the same time
	 *  
	 * @param packagePrefix
	 * @param blockUntilActive - If true this method will not return until the table is active or maxBlockTimeSeconds has expired
	 * @param  maxBlockTimeSeconds - The maximum amount of time to block for each table until the table becomes active
	 */
	public void scan(final String packagePrefix, boolean blockUntilActive, long maxBlockTimeSeconds){
		
		final Reflections reflections = new Reflections(packagePrefix);
		final Set<Class<?>> tableClasses = reflections.getTypesAnnotatedWith(DynamoDBTable.class);
		for(Class<?> clazz : tableClasses){
			if(!tableExists(clazz)){
				this.createTable(clazz);	
			}
		}
		
		/** If specified, wait for all the tables to become if active **/
		if(blockUntilActive){
			for(Class<?> clazz : tableClasses){
				this.waitForTableToBecomeActive(clazz, maxBlockTimeSeconds, DEFAULT_PAUSE_TIME_SECONDS);
			}			
		}
	}


	/***
	 * Return true if the table exists other false
	 * @param clazz
	 * @return tableExists
	 */
	public boolean tableExists(Class<?> clazz) {
		try{
			this.describeTable(clazz);	
		}catch(ResourceNotFoundException rnf){
			return false;
		}
		return true;
	}




	/***
	 * Creates a Create Table Request
	 * @param tableName
	 * @param hashKeyName
	 * @param rangeKeyName
	 * @param globalIndexes
	 * @param localIndexes
	 * @return createTableRequest
	 */
	private CreateTableRequest createCreateTableRequest(final String tableName, final String hashKeyName, final String rangeKeyName, final Map<String, GlobalIndex> globalIndexes,  final Map<String, RangeKeyIndexField> localIndexes){
		
		final Collection<KeySchemaElement> tableKeySchema = new ArrayList<KeySchemaElement>();
		final Map<String, AttributeDefinition> attrDefinitions = Maps.newHashMap();
		
		//TODO fix this, Find the type for the hash and range key for the table
		final KeySchemaElement tableHashKey = new KeySchemaElement(hashKeyName, "HASH");
		tableKeySchema.add(tableHashKey);
		attrDefinitions.put(hashKeyName, new AttributeDefinition(hashKeyName, "S"));
		
		if(!Strings.isNullOrEmpty(rangeKeyName)){
			tableKeySchema.add(new KeySchemaElement(rangeKeyName, "RANGE"));
			attrDefinitions.put(rangeKeyName, new AttributeDefinition(rangeKeyName, "S"));
		}
		
		/** Set the table, hashKey and rangeKey **/
		final CreateTableRequest request = new CreateTableRequest();
		request.withTableName(tableName);
		request.withProvisionedThroughput(DEFAULT_PROVISIONED_THROUGHPUT);
		request.withKeySchema(tableKeySchema);
		
		/** Set the Global Secondary Indexes **/
		if(globalIndexes!=null && globalIndexes.size()>0){
			final List<GlobalSecondaryIndex> globalSecondaryIndexes = new ArrayList<GlobalSecondaryIndex>();
			for(Entry<String, GlobalIndex> globalEntry: globalIndexes.entrySet()){
				
				final GlobalIndex global = globalEntry.getValue();
				
				/** Aws GlobalSecondary Index **/
				GlobalSecondaryIndex awsGlobal = new GlobalSecondaryIndex();
				awsGlobal.withIndexName(global.getName());
				awsGlobal.withProvisionedThroughput(DEFAULT_PROVISIONED_THROUGHPUT);
				
				/** Aws GlobalSecondary Index Hash Key Element**/
				final Collection<KeySchemaElement> indexKeySchema = new ArrayList<KeySchemaElement>();
				indexKeySchema.add(new KeySchemaElement(global.getHashKey(), "HASH"));
				
				/** If not already set **/
				if(!attrDefinitions.containsKey(global.getHashKey())){
					attrDefinitions.put(global.getHashKey(), new AttributeDefinition(global.getHashKey(), global.getHashKeyType()));
				}
				
				/** Aws GlobalSecondary Index Range Key Element**/
				
				/** If a range key exists set the key schema element and the attribute definition if not already set **/
				if(global.getRangeKeyField()!=null){
					indexKeySchema.add(new KeySchemaElement(global.getRangeKeyField().getRangeKeyField(), "RANGE"));
				
					/** If not already set **/
					if(!attrDefinitions.containsKey(global.getRangeKeyField().getRangeKeyField())){
						attrDefinitions.put(global.getRangeKeyField().getRangeKeyField(), new AttributeDefinition(global.getRangeKeyField().getRangeKeyField(), global.getRangeKeyField().getRangeKeyType()));
					}
				}
				
				/** Add  key schema elements for global secondary indexes **/
				awsGlobal.withKeySchema(indexKeySchema);
				awsGlobal.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
				globalSecondaryIndexes.add(awsGlobal);				
			}
			request.withGlobalSecondaryIndexes(globalSecondaryIndexes);
		}
		
		
		/** Set the local secondary indexes **/
		if(localIndexes!=null && localIndexes.size()>0){
			final List<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
			for(Entry<String, RangeKeyIndexField> localEntry: localIndexes.entrySet()){
				
				final LocalSecondaryIndex awsLocalIndex = new LocalSecondaryIndex();
				final KeySchemaElement indexRangeKeySchemaElement = new KeySchemaElement(localEntry.getValue().getRangeKeyField(), "RANGE");
				awsLocalIndex.withIndexName(localEntry.getValue().getIndexName());
				awsLocalIndex.withKeySchema(tableHashKey, indexRangeKeySchemaElement);
				awsLocalIndex.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
				localSecondaryIndexes.add(awsLocalIndex);
				
				/** Add the range key to the attribute definitions if not already set **/
				if(!attrDefinitions.containsKey(localEntry.getValue().getRangeKeyField())){
					attrDefinitions.put(localEntry.getValue().getRangeKeyField(), new AttributeDefinition(localEntry.getValue().getRangeKeyField(), localEntry.getValue().getRangeKeyType()));
				}
			}
			request.withLocalSecondaryIndexes(localSecondaryIndexes);
		}
		request.withAttributeDefinitions(attrDefinitions.values());
		return request;
	}


	
	/***
	 * Create the table and the associated indexes if it does not already exist
	 * @param reflections
	 * @param clazz
	 */
	private void createTable(Class<?> clazz) {

		final String tableName = this.getClassAnnotationValue(clazz, DynamoDBTable.class, String.class, "tableName");
		
		final Method hashKeyMember = this.getMethodForAnnotation(clazz, DynamoDBHashKey.class);
		final DynamoDBHashKey hashKeyAnno = hashKeyMember.getAnnotation(DynamoDBHashKey.class);
		final String hashKeyName = this.getAnnotationValue(hashKeyAnno, "attributeName", String.class);
		String rangeKeyName = null;
		
		
		final Method rangeKeyMember = this.getMethodForAnnotation(clazz, DynamoDBRangeKey.class);
		if(rangeKeyMember!=null){
			DynamoDBRangeKey rangeKeyAnno = rangeKeyMember.getAnnotation(DynamoDBRangeKey.class);	
			rangeKeyName = this.getAnnotationValue(rangeKeyAnno, "attributeName", String.class);
		}
		

		final Set<Method> hashKeyIndexFields = this.getMethodsAnnotatedWith(DynamoDBIndexHashKey.class, clazz);
		final Set<Method> rangeKeyIndexFields = this.getMethodsAnnotatedWith(DynamoDBIndexRangeKey.class, clazz);
		
		final Map<String, GlobalIndex> globalIndexes = this.createGlobalIndexes(hashKeyIndexFields, rangeKeyIndexFields, clazz);
		final Map<String, RangeKeyIndexField> localIndexes = this.createLocalIndexMap(rangeKeyIndexFields);
		
		final CreateTableRequest tableRequest = this.createCreateTableRequest(tableName, hashKeyName, rangeKeyName, globalIndexes, localIndexes);
		this.client.createTable(tableRequest);
	}

	
	
	
	
	
	/***
	 * Get the method containing the given annotation
	 * @param clazz
	 * @param annotationClazz
	 * @return method - null if the method does not exists
	 */
	private Method getMethodForAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClazz) {

		Method[] methods = clazz.getMethods();
		if(methods!=null){
			for(Method m : methods){
				if(m.isAnnotationPresent(annotationClazz)){
					return m;
				}
			}
		}
		return null;
	}




	/***
	 * Get data required to build out global secondary indexes
	 * 
	 * @param hashKeyIndexFields
	 * @param rangeKeyIndexFields
	 * @param clazz
	 * 
	 * @return globalIndexMap
	 */
	private Map<String, GlobalIndex> createGlobalIndexes(final Set<Method> hashKeyIndexFields, final Set<Method> rangeKeyIndexFields, Class<?> clazz) {
		
		final Map<String, GlobalIndex> globalIndexes = Maps.newHashMap();	
		
		
		for(Method m : hashKeyIndexFields){
			
			final Set<String> globalIndexNames = Sets.newHashSet();
			
			final DynamoDBIndexHashKey indexHashKeyAnno = m.getAnnotation(DynamoDBIndexHashKey.class);
			String[] indexNames = this.getAnnotationValue(indexHashKeyAnno, "globalSecondaryIndexNames", String[].class);
			if(indexNames==null || indexNames.length==0){
				String indexName = this.getAnnotationValue(indexHashKeyAnno, "globalSecondaryIndexName", String.class);
				Preconditions.checkArgument(!Strings.isNullOrEmpty(indexName), String.format("Index Name is required for DynamoDBIndexHashKey attribute for class %s field %s", clazz.getName(), m.getName()));
				globalIndexNames.add(indexName);
			}else{
				globalIndexNames.addAll(Sets.newHashSet(indexNames));
			}
			
			String attributeName = this.getAnnotationValue(indexHashKeyAnno, "attributeName", String.class);
			String dataType = this.getKeyType(m);
			
			final Map<String, RangeKeyIndexField> globalIndexRangeKeys = this.getGlobalIndexRangeKeys(rangeKeyIndexFields);
			
			/** Process Global Indexes **/
			for(String name : globalIndexNames){
				final GlobalIndex global = new GlobalIndex();
				global.setName(name);
				global.setHashKey(attributeName);
				global.setHashKeyType(dataType);
				/** Range key is optional for a global secondary index **/
				final RangeKeyIndexField rangeKey = globalIndexRangeKeys.get(name);
				if(rangeKey!=null){
					global.setRangeKeyField(rangeKey);
				}
				globalIndexes.put(name, global);
			}
		}
		return globalIndexes;
	}
	
	
	
	
	/****
	 * Get the rangeKey and Local Secondary indexes
	 * @param rangeKeyMethods
	 * @return rangeKeyIndexMap
	 */
	private Map<String, RangeKeyIndexField> createLocalIndexMap(Set<Method> rangeKeyMethods){
		final Map<String, RangeKeyIndexField> locallIndexRangeKeys = Maps.newHashMap();
		if(rangeKeyMethods!=null){
			for(Method m : rangeKeyMethods){
				final DynamoDBIndexRangeKey indexRangeKeyAnno = m.getAnnotation(DynamoDBIndexRangeKey.class);
				final String attrName = this.getAnnotationValue(indexRangeKeyAnno, "attributeName", String.class);
				final String keyType = this.getKeyType(m);
				/** Get Index Names **/
				String[] indexNames = this.getAnnotationValue(indexRangeKeyAnno, "localSecondaryIndexNames", String[].class);
				if(indexNames==null || indexNames.length==0){
					String indexName = this.getAnnotationValue(indexRangeKeyAnno, "localSecondaryIndexName", String.class);
					if(!Strings.isNullOrEmpty(indexName)){
						locallIndexRangeKeys.put(indexName, new RangeKeyIndexField(indexName, attrName, keyType));	
					}
					
				}else{
					for(String indexName : indexNames){
						locallIndexRangeKeys.put(indexName, new RangeKeyIndexField(indexName, attrName, keyType));
					}
				}
			}
		}
		return locallIndexRangeKeys;
	}
	
	
	
	
	/***
	 * Get a map of Index names and the class property range key for this index
	 * @param methods
	 * @return globalIndexRangeKey
	 */
	private Map<String, RangeKeyIndexField> getGlobalIndexRangeKeys(Set<Method> methods){
		
		final Map<String, RangeKeyIndexField> globalIndexRangeKeys = Maps.newHashMap();
		
		for(Method m : methods){
			final DynamoDBIndexRangeKey indexRangeKeyAnno = m.getAnnotation(DynamoDBIndexRangeKey.class);
			final String attrName = this.getAnnotationValue(indexRangeKeyAnno, "attributeName", String.class);
			final String keyType = this.getKeyType(m);
			/** Get Index Names **/
			String[] indexNames = this.getAnnotationValue(indexRangeKeyAnno, "globalSecondaryIndexNames", String[].class);
			if(indexNames==null || indexNames.length==0){
				String indexName = this.getAnnotationValue(indexRangeKeyAnno, "globalSecondaryIndexName", String.class);
				globalIndexRangeKeys.put(indexName, new RangeKeyIndexField(indexName, attrName, keyType));
			}else{
				for(String indexName : indexNames){
					globalIndexRangeKeys.put(indexName, new RangeKeyIndexField(indexName, attrName, keyType));
				}
			}
		}
		return globalIndexRangeKeys;
	}

	
	


	/***
	 * Get the DynamoDb type for the Java class type
	 * @param method
	 * @return dynamoDbKeyType
	 */
	private String getKeyType(Method method) {
		Class<?> returnClass = method.getReturnType();
		if(String.class.isAssignableFrom(returnClass) || 
				Date.class.isAssignableFrom(returnClass) ||
				Enum.class.isAssignableFrom(returnClass)){
			return "S";
		}else if(Number.class.isAssignableFrom(returnClass)){
			return "N";
		}
		throw new IllegalArgumentException("Unsupported HashKey Type found, only String and Numbers Supported");
	}




	/***
	 * Get the methods Annotated with a given annotation
	 * @param annotationClazz - The annotation class to look for
	 * @param clazz  - The class to search annotated methods in
	 * @return  methodSet 
	 */
	private <T> Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClazz, Class<T> clazz){
		final Set<Method> methodSet = Sets.newHashSet();
		Method[] methods = clazz.getMethods();
		if(methods!=null){
			for(Method method: methods){
				if(method.isAnnotationPresent(annotationClazz)){
					methodSet.add(method);
				}
			}			
		}
		return methodSet;
	}
	
	
	
	private <A extends Annotation, T> T getAnnotationValue(A annotation, final String fieldName, Class<T> dataType){
		try{
			@SuppressWarnings("unchecked")
			T value = (T) annotation.annotationType().getMethod(fieldName).invoke(annotation);
			return value;
		}catch(Exception ex){}
		return null;
	}
	
	
	
	
	
	public static class RangeKeyIndexField{
		private String indexName;
		private String rangeKeyField;
		private String rangeKeyType;
		
		public RangeKeyIndexField(final String indexName, final String rangeKeyField, final String rangeKeyType){
			this.indexName = indexName;
			this.rangeKeyField = rangeKeyField;
			this.rangeKeyType = rangeKeyType;
		}
		
		public String getRangeKeyType() {
			return rangeKeyType;
		}
		public void setRangeKeyType(String rangeKeyType) {
			this.rangeKeyType = rangeKeyType;
		}
		public String getIndexName() {
			return indexName;
		}
		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}
		public String getRangeKeyField() {
			return rangeKeyField;
		}
		public void setRangeKeyField(String rangeKeyField) {
			this.rangeKeyField = rangeKeyField;
		}
	}
	
	
	
	
	/**
	 * A container for Global Secondary Index Parameters
	 * @author shawn
	 *
	 */
	static class GlobalIndex {
		private String name;
		private String hashKey;
		private String hashKeyType;
		private RangeKeyIndexField rangeKeyField;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getHashKey() {
			return hashKey;
		}
		public void setHashKey(String hashKey) {
			this.hashKey = hashKey;
		}
		public String getHashKeyType() {
			return hashKeyType;
		}
		public void setHashKeyType(String hashKeyType) {
			this.hashKeyType = hashKeyType;
		}
		public RangeKeyIndexField getRangeKeyField() {
			return rangeKeyField;
		}
		public void setRangeKeyField(RangeKeyIndexField rangeKeyField) {
			this.rangeKeyField = rangeKeyField;
		}
	}
	
	

	@SuppressWarnings("unchecked")
	private <T> T getClassAnnotationValue(final Class<?> classType, final Class<? extends Annotation> annotationType, final Class<T> type, final String attributeName) {
		T value = null;
        final Annotation annotation = classType.getAnnotation(annotationType);
        if (annotation != null) {
            try {
                value = (T)annotation.annotationType().getMethod(attributeName).invoke(annotation);
            } catch (Exception ex) {
            }
        }
        return value;
    }


	
	/***
	 * Wait the specified time for the table to become active
	 * @param clazz
	 * @param maxWaitTimeSeconds
	 * @param timeBetweenChecksSeconds
	 */
	private void waitForTableToBecomeActive(Class<?> clazz, long maxWaitTimeSeconds, long timeBetweenChecksSeconds){
		
		try{
		
			final String ACTIVE = "ACTIVE";
			
			long waitUntil = (DateTime.now().getMillis() + (maxWaitTimeSeconds * 1000L));
			String status = null;
			while(!ACTIVE.equals(status) && DateTime.now().getMillis() < waitUntil){
			
				status = this.getTableStatus(clazz);
				
				/** If it's active then return **/
				if(ACTIVE.equals(status)){
					LOGGER.info("Table for model: {} is active!", clazz);
					return;
				}
				
				LOGGER.info("Table for model: {} has status of {}. Waiting {} seconds for next check", clazz.getName(), status, timeBetweenChecksSeconds);
				Thread.sleep(timeBetweenChecksSeconds * 1000L);
			}
			
			
			if(!ACTIVE.equals(status) && DateTime.now().getMillis() > waitUntil){
				LOGGER.warn("The timeout period expired while waiting for table: {} to become active. Status is {} maxWaitTimeInSeconds: {}", clazz, status, maxWaitTimeSeconds);
			}
			
		}catch(ResourceNotFoundException rnf){
			LOGGER.warn("Table for model: {} does not exist", clazz.getName());
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
		}
	}
	
	/***
	 * Get Table Status
	 * @param clazz
	 * @return status
	 */
	public String getTableStatus(Class<?> clazz) throws ResourceNotFoundException{
		final DescribeTableResult description = describeTable(clazz);
		if(description.getTable()!=null){
			return description.getTable().getTableStatus();
		}
		return null;
	}
	
	
	/***
	 * Describe Table
	 * @param clazz
	 * @return describeTableResult
	 */
	private DescribeTableResult describeTable(Class<?> clazz) throws ResourceNotFoundException{
		final String tableName = this.getClassAnnotationValue(clazz, DynamoDBTable.class, String.class, "tableName");
		return this.client.describeTable(new DescribeTableRequest(tableName));
	}
}
