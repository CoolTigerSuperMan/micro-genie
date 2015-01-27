package io.microgenie.application.events;

import java.util.Map;



/***
 * Represents Event data where schemaId is the Id of the schema that describes the event data
 * @author shawn
 */
public class EventData {
	
	private String schemaId;
	private String action;
	private Map<String, Object> data;
	
	public EventData(){}
	
	public EventData(final Map<String, Object> data){
		this(null, data);
	}
	public EventData(final String action, final Map<String, Object> data){
		this(null, action, data);
	}
	public EventData(final String schemaId, final String action, final Map<String, Object> data){
		this.schemaId = schemaId;
		this.action = action;
		this.data = data;
		
	}
	public String getSchemaId() {
		return schemaId;
	}
	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(final Map<String, Object> data) {
		this.data = data;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
}
