package io.microgenie.application.queue;

import java.util.Map;


/**
 * @author shawn
 */
public interface Message {
	public Map<String, String> getHeaders();
	public String getId();
	public String getQueue();
	public String getBody();
}
