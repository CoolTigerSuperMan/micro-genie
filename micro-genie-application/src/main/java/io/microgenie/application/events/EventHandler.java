package io.microgenie.application.events;

import java.util.List;

/***
 * The EventApi Handler
 * @author shawn
 */
public interface EventHandler {
	public void handle(Event event);
	public void handle(List<Event> events);
}
