package io.microgenie.events;



/***
 * BaseEvent
 * @author shawn
 */
public abstract class BaseEvent {
	public BaseEvent(){}
	public abstract String getPrincipal();
	public abstract String getKey();
	public abstract  String getTopic();
}
