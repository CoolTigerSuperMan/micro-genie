package io.microgenie.application.database;


import java.io.Closeable;
import java.util.Map;

import com.google.common.collect.Maps;






/***
 * DB Factory
 * @author shawn
 */
public abstract class DatabaseFactory implements Closeable {

	
	private final Map<Class<?>, EntityRepository<?>> repositories = Maps.newHashMap();
	
	public DatabaseFactory(){}

	
	@SuppressWarnings("unchecked")
	public <T,R extends EntityRepository<T>> R repos(Class<T> clazz) {
		final EntityRepository<?> repo =  repositories.get(clazz);
		if(repo!=null){
			return (R) repo;	
		}
		return null;
	}
	
	
	
	
	/***
	 *  Register a repository class
	 * @param clazz
	 * @param repo
	 */
	public <T> EntityRepository<T> registerRepo(Class<T> clazz, EntityRepository<T> repo){
		this.repositories.put(clazz, repo);
		return repo;
	}
}
