package io.microgenie.application.events;

import java.io.Closeable;


/**
 * EventApi Subscriber interface
 * @author shawn
 */
public interface Subscriber extends Closeable {
	public String getTopic();
	public void subscribe(EventHandler handler);
	public void stop();
}
