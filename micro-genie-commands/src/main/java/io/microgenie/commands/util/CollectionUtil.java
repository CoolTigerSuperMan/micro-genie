package io.microgenie.commands.util;

import java.util.Collection;

/**
 * Genie Collection Utilities
 * 
 * @author shawn
 *
 */
public class CollectionUtil {

	public static boolean isNullOrEmpty(Collection<?> collection){
		return collection == null || collection.isEmpty();
	}
	public static boolean hasElements(Collection<?> collection){
		return collection != null && !collection.isEmpty();
	}
}
