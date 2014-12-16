package io.microgenie.commands.core;

import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Specify a default fallback value
 * @author shawn
 *
 * @param <T>
 */
public class Fallback<T> implements FutureFallback<T>{
	private T defaultValue;
	public Fallback(T defaultValue){
		this.defaultValue = defaultValue;
	}
	@Override
	public ListenableFuture<T> create(Throwable t) throws Exception {
		return Futures.immediateFuture(defaultValue);
	}
	/**
	 * Create a fallback Future with a default value
	 * @param defaultValue
	 * @return
	 */
	public static <T>  Fallback<T> create(T defaultValue){
		return new Fallback<T>(defaultValue);
	}
}
