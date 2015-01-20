package io.microgenie.application.database;

import java.util.List;
import java.util.Map;


/**
 * A key value based data repository
 * @author shawn
 *
 */
public interface KeyValueRepository {

	
	public Map<String, Object> get(final Class<?> clazz, String idName, Object id);
	public Map<String, Object> get(Class<?> clazz, String idName, Object id, String rangeName, Object range);
	
	public List<Map<String, Object>> query(final Class<?> clazz, Map<String, Object> predicates);
	public void save(final Class<?> clazz, Map<String, Object> data);
	public void delete(final Class<?> clazz, String idName, Object id);

}
