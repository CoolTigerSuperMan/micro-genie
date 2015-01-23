package io.microgenie.events;

import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventHandler;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


/***
 * 
 * @author shawn
 *
 */
public class LoggingOutputHandler implements EventHandler{

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingOutputHandler.class); 
	private final ObjectMapper mapper;
	private final Map<String, Class<?>> topicTypeMap;
	
	
	
	public LoggingOutputHandler(final Map<String, Class<?>> topicTypeMap, final ObjectMapper mapper){
		this.topicTypeMap = topicTypeMap;
		this.mapper = mapper;
	}
	
	
	/***
	 * Handle events
	 */
	@Override
	public void handle(Event event) {
		try {
			Object o = this.mapper.readValue(event.getBody(), topicTypeMap.get(event.getTopic()));
			LOGGER.info("Received event: {}", o.toString());
		}catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	
	
	/**
	 * handle event batch
	 */
	@Override
	public void handle(List<Event> events) {
		for(Event event : events){
			this.handle(event);
		}
	}
}
