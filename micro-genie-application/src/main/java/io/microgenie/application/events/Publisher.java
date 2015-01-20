package io.microgenie.application.events;

import java.io.Closeable;
import java.util.List;


/**
 * EventApi Publisher
 * @author shawn
 */
public interface Publisher extends Closeable {
	public String clientId();
	public void submit(Event event);
	public void submitBatch(List<Event> event);
}
