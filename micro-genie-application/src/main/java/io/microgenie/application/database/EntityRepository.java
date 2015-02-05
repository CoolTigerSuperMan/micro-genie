package io.microgenie.application.database;

import io.microgenie.application.database.EntityDatabusRepository.Key;

import java.util.List;





/***
 * Primary interface abstraction for Entity based repositories
 * @author shawn
 *
 * @param <T>
 */
public abstract class EntityRepository<T> {


	protected abstract void delete(T item);
	protected abstract List<T> getList(List<T> items);
	
	
	public abstract T get(Key key);	
	public abstract void delete(final Key key);
	public abstract void save(T item);
	public abstract void save(List<T> items);
}
