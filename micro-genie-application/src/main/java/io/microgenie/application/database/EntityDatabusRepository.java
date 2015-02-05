package io.microgenie.application.database;

import io.microgenie.application.events.StateChangePublisher;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public abstract class EntityDatabusRepository<T> extends EntityRepository<T> {

	
	private Class<T> clazz;
	private StateChangePublisher changePublisher;

	public EntityDatabusRepository(final Class<T> clazz, final StateChangePublisher changePublisher){
		this.clazz = clazz;
		this.changePublisher = changePublisher;
	}

	
	
	public abstract PartitionedDataKeyWithItem<T> createPartitionedDataKey(T item);
	
	
	
	
	public void saveAndNotify(final String partitionKey, Key key, final T item) {
		final T existing = this.get(key);
		this.save(item);
		this.changePublisher.publishChanges(clazz, partitionKey, item, existing);	
	}
	
	
	
	
	public void deleteAndNotify(final String partitionKey, final Key key) {
		final T existing = this.get(key);
		if(existing!=null){
			this.delete(existing);
			this.changePublisher.publishDeleted(partitionKey, existing);	
		}
	}
	

	
	
	/***
	 * Save and notify batch items
	 * @param submittedItems
	 */
	public void saveAndNotify(List<T> submittedItems) {
		
		final Map<String, PartitionedDataKeyWithItem<T>> submittedItemsMap = Maps.newHashMap();
		final Map<String, PartitionedDataKeyWithItem<T>> existingItemsMap = Maps.newHashMap();
		
		/** Map the submitted items to a unique key **/
		for(T item : submittedItems){
			PartitionedDataKeyWithItem<T> partitionedKey = this.createPartitionedDataKey(item);
			submittedItemsMap.put(partitionedKey.getUniqueKey(), partitionedKey);
		}
		
		
		/** Map the existing Items to a unique key**/
		final List<T> existingItems = this.getList(submittedItems);
		if(existingItems!=null){
			for(T existingItem : submittedItems){
				final PartitionedDataKeyWithItem<T> partitionedKey = this.createPartitionedDataKey(existingItem);
				existingItemsMap.put(partitionedKey.getUniqueKey(), partitionedKey);
			}
		}
		
		/** Save the submitted items **/
		this.save(submittedItems);
		
		/** send the submitted and mapped existing items to the publisher to sort out change details **/
		for(Entry<String, PartitionedDataKeyWithItem<T>> submittedItemEntry : submittedItemsMap.entrySet()){
			final PartitionedDataKeyWithItem<T> partitionedSubmittedItem = submittedItemEntry.getValue();
			this.changePublisher.publishChanges(this.clazz, partitionedSubmittedItem.getPartitionKey(), partitionedSubmittedItem.getItem(), existingItemsMap.get(partitionedSubmittedItem.getUniqueKey()));
		}
	}
	
	
	




	public static class Key{
		private String hash;
		private String range;
		public Key(){}
		public Key(final String hash){
			this.hash = hash;
		}
		public Key(final String hash, final String range){
			this.hash = hash;
			this.range = range;
		}
		public static Key create(final String hash){
			return create(hash, null);
		}
		public static Key create(final String hash, final String range){
			return new Key(hash, range);
		}
		public String getHash() {
			return hash;
		}
		public void setHash(final String hash) {
			this.hash = hash;
		}
		public String getRange() {
			return range;
		}
		public void setRange(final String range) {
			this.range = range;
		}
	}
	
	public static class PartitionedDataKey extends Key{
		private final String partitionKey;
		public PartitionedDataKey(final String partitionKey, final String hash){
			this(partitionKey, hash, null);
		}
		public PartitionedDataKey(final String partitionKey, final String hash, final String range){
			super(hash, range);
			this.partitionKey = partitionKey;
		}
		public String getPartitionKey() {
			return partitionKey;
		}
		public static final PartitionedDataKey create(final String partitionKey, final String hash){
			return create(partitionKey, hash, null);
		}
		public static final PartitionedDataKey create(final String partitionKey, final String hash, final String range){
			return new PartitionedDataKey(partitionKey, hash, range);
		}
	}
	
	public static class PartitionedDataKeyWithItem<T> extends PartitionedDataKey{
		private final String partitionKey;
		private final T item;
		
		public PartitionedDataKeyWithItem(final String partitionKey, final String hash, final T item){
			this(partitionKey, hash, null, item);
		}
		public String getUniqueKey() {
			if(!Strings.isNullOrEmpty(this.getHash()) && !Strings.isNullOrEmpty(this.getRange())){
				return (this.getHash()+"#"+this.getRange());
			}else if(!Strings.isNullOrEmpty(this.getHash())){
				return this.getHash();
			}
			return this.partitionKey;
		}
		public PartitionedDataKeyWithItem(final String partitionKey, final String hash, final String range, final T item){
			super(hash, range);
			this.partitionKey = partitionKey;
			this.item = item;
		}
		public String getPartitionKey() {
			return partitionKey;
		}
		public T getItem() {
			return item;
		}
		public static <T> PartitionedDataKeyWithItem<T> create(final String partitionKey, final String hash, T item){
			return create(partitionKey, hash, null, item);
		}
		public static <T> PartitionedDataKeyWithItem<T> create(final String partitionKey, final String hash, final String range, T item){
			return new PartitionedDataKeyWithItem<T>(partitionKey, hash, range, item);
		}
	}
}
