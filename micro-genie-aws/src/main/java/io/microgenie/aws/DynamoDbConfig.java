package io.microgenie.aws;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


/***
 * DynamoDb Configuration
 * @author shawn
 */
public class DynamoDbConfig {

	
	private String packagePrefix;
	private List<Table> tables = new ArrayList<Table>();
	private boolean blockUntilReady = true;
	
	
	@JsonProperty(value="blockUntilReady")
	public boolean isBlockUntilReady() {
		return blockUntilReady;
	}
	@JsonProperty(value="blockUntilReady")
	public void setBlockUntilReady(boolean blockUntilReady) {
		this.blockUntilReady = blockUntilReady;
	}
	
	
	/***
	 * DynamoDb Table Configuration
	 * @author shawn
	 *
	 */
	public static class Table{
		
		private String name;
		private List<Key> keys = new ArrayList<Key>();
		private Long writeCapacityUnits;
		private Long readCapacityUnits;
		
		private List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
		private List<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
		private List<GlobalSecondaryIndex> globalSecondaryIndexes = new ArrayList<GlobalSecondaryIndex>();

		
		@JsonProperty("name")
		public String getName() {
			return name;
		}
		@JsonProperty("name")
		public void setName(String name) {
			this.name = name;
		}

		/** Key Schema Elements, required for any attribute that is part of an index or primary key **/
		@JsonProperty("keys")
		public List<Key> getKeys() {
			return keys;
		}
		@JsonProperty("keys")
		public void setKeys(List<Key> keys) {
			this.keys = keys;
		}
		
		
		@JsonProperty("readCapacityUnits")
		/** Read Write capacity units **/
		public Long getReadCapacityUnits() {
			return readCapacityUnits;
		}
		@JsonProperty("readCapacityUnits")
		public void setReadCapacityUnits(Long readCapacityUnits) {
			this.readCapacityUnits = readCapacityUnits;
		}
		
		@JsonProperty("writeCapacityUnits")
		public Long getWriteCapacityUnits() {
			return writeCapacityUnits;
		}
		@JsonProperty("writeCapacityUnits")
		public void setWriteCapacityUnits(Long writeCapacityUnits) {
			this.writeCapacityUnits = writeCapacityUnits;
		}
		
	
		
		/** Attribute Definitions required for any attribute that is part of an index or primary key **/
		@JsonProperty("attributeDefinitions")
		public List<AttributeDefinition> getAttributeDefinitions() {
			return attributeDefinitions;
		}
		@JsonProperty("attributeDefinitions")
		public void setAttributeDefinitions(List<AttributeDefinition> attributeDefinitions) {
			this.attributeDefinitions = attributeDefinitions;
		}
		
		
		@JsonProperty("localSecondaryIndexes")
		public List<LocalSecondaryIndex> getLocalSecondaryIndexes() {
			return localSecondaryIndexes;
		}
		@JsonProperty("localSecondaryIndexes")
		public void setLocalSecondaryIndexes(List<LocalSecondaryIndex> localSecondaryIndexes) {
			this.localSecondaryIndexes = localSecondaryIndexes;
		}
		
		@JsonProperty("globalSecondaryIndexes")
		public List<GlobalSecondaryIndex> getGlobalSecondaryIndexes() {
			return globalSecondaryIndexes;
		}
		@JsonProperty("globalSecondaryIndexes")
		public void setGlobalSecondaryIndexes(List<GlobalSecondaryIndex> globalSecondaryIndexes) {
			this.globalSecondaryIndexes = globalSecondaryIndexes;
		}
	}
	

	
	
	/***
	 * Local Secondary Index Configuration
	 * @author shawn
	 */
	public static class LocalSecondaryIndex extends SecondaryIndex {
		public LocalSecondaryIndex(){}
	}


	/***
	 * Global Secondary Index Configuration
	 * @author shawn
	 */
	public static class GlobalSecondaryIndex extends SecondaryIndex{
		private Long readCapacityUnits;
		private Long writeCapacityUnits;
		
		public GlobalSecondaryIndex(){}
		
		@JsonProperty("readCapacityUnits")
		public Long getReadCapacityUnits() {
			return readCapacityUnits;
		}
		@JsonProperty("readCapacityUnits")
		public void setReadCapacityUnits(Long readCapacityUnits) {
			this.readCapacityUnits = readCapacityUnits;
		}
		
		@JsonProperty("writeCapacityUnits")
		public Long getWriteCapacityUnits() {
			return writeCapacityUnits;
		}
		@JsonProperty("writeCapacityUnits")
		public void setWriteCapacityUnits(Long writeCapacityUnits) {
			this.writeCapacityUnits = writeCapacityUnits;
		}
	}


	/***
	 * Common Attributes for all Secondary Indexes
	 * @author shawn
	 */
	public static class SecondaryIndex{ 
		
		private String name;
		private Projection projection; 
		private List<Key> keys = new ArrayList<Key>();
		
		protected SecondaryIndex(){}
		
		@JsonProperty("name")
		public String getName() {
			return name;
		}
		@JsonProperty("name")
		public void setName(String name) {
			this.name = name;
		}
		
		@JsonProperty("projection")
		public Projection getProjection() {
			return projection;
		}@JsonProperty("projection")
		public void setProjection(Projection projection) {
			this.projection = projection;
		}
		
		@JsonProperty("keys")
		public List<Key> getKeys() {
			return keys;
		}
		@JsonProperty("keys")
		public void setKeys(List<Key> keys) {
			this.keys = keys;
		}
	}
	
	
	/***
	 * Projection Configuration determines  which attributes are copied over with a given index
	 * @author shawn
	 *
	 */
	public static class Projection{
		private String type; // Valid Values are ALL, KEYS_ONLY, INCLUDE
		private List<String> nonKeyAttributes;
	
		public Projection(){}
		
		@JsonProperty("type")
		public String getType() {
			return type;
		}
		@JsonProperty("type")
		public void setType(String type) {
			this.type = type;
		}
		
		@JsonProperty("nonKeyAttributes")
		public List<String> getNonKeyAttributes() {
			return nonKeyAttributes;
		}
		@JsonProperty("nonKeyAttributes")
		public void setNonKeyAttributes(List<String> nonKeyAttributes) {
			this.nonKeyAttributes = nonKeyAttributes;
		}
	}
	
	

	/**
	 * Part of a primary key or index
	 * @author shawn
	 */
	public static class Key{
		public Key(){}
		private String attributeName;
		private String keyType;
		
		public Key(final String attributeName){
			this.attributeName = attributeName;
		}
		
		@JsonProperty("attributeName")
		public String getAttributeName() {
			return attributeName;
		}
		@JsonProperty("attributeName")
		public void setName(String attributeName) {
			this.attributeName = attributeName;
		}
		@JsonProperty("keyType")
		public String getKeyType() {
			return this.keyType;
		}
		@JsonProperty("keyType")
		public void setKeyType(final String keyType){
			this.keyType = keyType;
		}
	}
	
	

	
	/***
	 * An Attribute Definition for a Key Schema Element
	 * @author shawn
	 */
	public static class AttributeDefinition{
		private String name;
		private String type; // Valid Values are S, B, N
		
		@JsonProperty("name")
		public String getName() {
			return name;
		}
		@JsonProperty("name")
		public void setName(String name) {
			this.name = name;
		}
		
		@JsonProperty("type")
		public String getType() {
			return type;
		}
		@JsonProperty("type")
		public void setType(String type) {
			this.type = type;
		}
	}

	@JsonProperty("tables")
	public List<Table> getTables() {
		return tables;
	}
	
	@JsonProperty("tables")
	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	@JsonProperty("packageScanPrefix")
	public String getPackagePrefix() {
		return packagePrefix;
	}

	@JsonProperty("packageScanPrefix")
	public void setPackagePrefix(String packagePrefix) {
		this.packagePrefix = packagePrefix;
	}
}
