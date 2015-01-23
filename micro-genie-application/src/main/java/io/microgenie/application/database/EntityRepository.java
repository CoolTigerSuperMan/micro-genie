package io.microgenie.application.database;

import java.util.List;





/***
 * Primary interface abstraction for Entity based repositories
 * @author shawn
 *
 * @param <T>
 */
public abstract class EntityRepository<T, H, R> {

	/** These are protected because some models support hash key only and 
	*  some support hash and range key its up to the implementation
	*  class to expose the correct method for get and getList.
	*/
	protected abstract T get(H id);
	protected abstract T get(H id, R rangeKey);
	protected abstract List<T> getList(H hashKey);
	
	public abstract void delete(T item);
	public abstract void save(T item);
	public abstract void save(List<T> items);
}
