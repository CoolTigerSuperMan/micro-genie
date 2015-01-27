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
 * 			deleted: StudyDeleted
 * 			created: StudyCreated
 * 	    	modified: StudyModified
 */
public class StateChangeConfiguration{
	private Map<String, Map<String, String>> events;
	
	public StateChangeConfiguration(){}
	
	public Map<String, Map<String, String>> getEvents() {
		return events;
	}
	public void setEvents(Map<String, Map<String, String>> events) {
		this.events = events;
	}
	
	
//	public static class ActionTopicPair {
//		private String action;
//		private String topic;
//		public String getAction() {
//			return action;
//		}
//		public void setAction(String action) {
//			this.action = action;
//		}
//		public String getTopic() {
//			return topic;
//		}
//		public void setTopic(String topic) {
//			this.topic = topic;
//		} 
//	}	
}