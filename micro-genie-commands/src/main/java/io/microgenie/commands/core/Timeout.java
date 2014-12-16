package io.microgenie.commands.core;

import java.util.concurrent.TimeUnit;


/**
 * Command Timeout
 */
public class Timeout {

	private final long timeout;
	private final TimeUnit unit;
	protected Timeout(Timeout timeout){
		this.timeout = timeout.timeout;
		this.unit = timeout.unit;
	}
	protected Timeout(final long timeout, final TimeUnit unit){
		this.timeout = timeout;
		this.unit = unit;
	}
	public long getTimeout() {
		return timeout;
	}
	public TimeUnit getUnit() {
		return unit;
	}
	public static Timeout create(long timeout, TimeUnit unit){
		return new Timeout(timeout, unit);
	}
	public static TimeoutMillis inMilliseconds(long timeout){
		return new TimeoutMillis(timeout);
	}
	public static TimeoutSeconds inSeconds(long timeout){
		return new TimeoutSeconds(timeout);
	}
	public static TimeoutMinutes inMinutes(long timeout){
		return new TimeoutMinutes(timeout);
	}
	
	/**
	 * Specific Timeout Units
	 */
	static class TimeoutMillis extends Timeout {
		protected TimeoutMillis(long timeout) {
			super(timeout, TimeUnit.MILLISECONDS);
		}
	}
	static class TimeoutSeconds extends Timeout {
		protected TimeoutSeconds(long timeout) {
			super(timeout, TimeUnit.SECONDS);
		}
	}
	static class TimeoutMinutes extends Timeout {
		protected TimeoutMinutes(long timeout) {
			super(timeout, TimeUnit.MINUTES);
		}
	}
}

	
