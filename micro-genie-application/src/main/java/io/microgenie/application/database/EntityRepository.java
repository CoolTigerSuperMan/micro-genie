package io.microgenie.application.database;

import java.util.List;





/***
 * Primary interface abstraction for Entity based repositories
 * @author shawn
 *
 * @param <T>
 */
public abstract class EntityRepository<T> {

	protected abstract T get(Object id);
	protected abstract T get(Object id, Object rangeId);
	
	protected abstract void delete(T item);
	
	protected abstract void save(T item);
	protected abstract void save(List<T> items);

}
