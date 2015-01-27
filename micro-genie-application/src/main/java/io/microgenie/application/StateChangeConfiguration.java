package io.microgenie.application;


import java.util.Map;


/**
 * Configuration used to drive state change actions and where they are published to
 * @author shawn
 * 
 * Example configuration for the common case in yaml would be 
 * 
 * Whenever a book is deleted, created, or modified, an event is submitted to the associated topic
 * 
 *	stateChanges: 
 * 		Book:
 * 			Deleted: BookDeleted
 * 			Created: BookCreated
 * 	    	Updated: BookModified
 */
public class StateChangeConfiguration{
	private Map<String, Map<String, String>> events;
	public StateChangeConfiguration(){}
	public StateChangeConfiguration(final Map<String, Map<String, String>> events){
		this.events = events;
	}
	public Map<String, Map<String, String>> getEvents() {
		return events;
	}
	public void setEvents(Map<String, Map<String, String>> events) {
		this.events = events;
	}
}