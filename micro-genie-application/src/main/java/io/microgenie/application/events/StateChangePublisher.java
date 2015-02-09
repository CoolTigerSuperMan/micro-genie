package io.microgenie.application.events;

import io.microgenie.application.StateChangeConfiguration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;


/****
 * Capable of detecting changes between to object instances and publishing those changes 
 * to a given topic for other interested parties to consume
 * 
 * @author shawn
 */
public class StateChangePublisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(StateChangePublisher.class);
	
	/** default actions **/
	public static final String CREATED_ACTION = "Created";
	public static final String DELETED_ACTION = "Deleted";
	public static final String MODIFIED_ACTION = "Updated";
	
	
	/** json path fields **/
	private static final String OP = "op";
	private static final String PATH = "path";
	private static final String VALUE = "value";
	
	private static final String ADD = "add";
	private static final String REMOVE = "remove";
	private static final String REPLACE = "replace";
	
	
	private final StateChangeConfiguration config;
	private final Publisher publisher;
	private final ObjectMapper mapper;
	
	
	private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE= new TypeReference<Map<String,Object>>() {};
	
	
	/***
	 * Creates a state change publisher that can published state changes based on the given configuration
	 * and publishes to the supplied publisher
	 * <p>
	 * This constructor creates a default instance of ObjectMapper for serialization
	 * 
	 * @param config
	 * @param publisher
	 */
	public StateChangePublisher(final StateChangeConfiguration config, final Publisher publisher) {
		this(config, publisher, new ObjectMapper().setSerializationInclusion(Include.NON_NULL));
	}
	
	
	/***
	 * Creates a state change publisher that can published state changes based on the given configuration
	 * and publishes to the supplied publisher
	 * 
	 * @param config
	 * @param publisher
	 * @param mapper
	 */
	public StateChangePublisher(final StateChangeConfiguration config, final Publisher publisher, final ObjectMapper mapper) {
		this.config = config;
		this.publisher = publisher;
		this.mapper = mapper;
	}

	
	

	
	
	/***
	 * Publish data for the given model. The data will be published as the specified action
	 * 
	 * @param model - The model representing the data
	 * @param action - The action that has happened, e.g, created, updated, deleted
	 * @param key - The partition key
	 * @param data - The data that will be serialized and published
	 * @return wasPublished - true if the data was published, otherwise false 
	 */
	public boolean publishAction(final Class<?> model, final String action, final String key, final Object data) {
		
		final String topic = this.getTopic(model, action);
		
		/** convert the data to a Map, then create and publish the event **/
		final Map<String, Object> dataMap = this.mapper.convertValue(data, MAP_TYPE_REFERENCE);
		final Event event = this.createEvent(topic, action, key, dataMap);
		this.publisher.submit(event);
		return true;
	}


	
	


	/**
	 * Publish an event change for a newly created item
	 * 
	 * @param key - The partition key for the topic being published to
	 * @param submitted - The created item whose values will be published
	 * @return wasPublished - True if the item was published, otherwise false
	 */
	public boolean publishCreated(final String key, final Object submitted) {
		return this.publishAction(submitted.getClass(), CREATED_ACTION, key, 
				Preconditions.checkNotNull(submitted, "unable to publish the created event for key: %s since submitted is null"));
	}
	
	
	/**
	 * Publish an event change for a deleted item
	 * 
	 * @param key - The partition key for the topic being published to
	 * @param existing - The existing data for the item being deleted
	 * @return wasPublished - True if the event was published, otherwise false
	 */
	public boolean publishDeleted(final String key, final Object existing) {
		return this.publishAction(existing.getClass(), DELETED_ACTION, key, 
				Preconditions.checkNotNull(existing, "unable to publish the deleted event for key: %s since existing data is null"));
	}
	
	
	/***
	 * Publish modified changes where an existing item has been modified
	 * 
	 * @param key - The topic partition key
	 * @param submitted - The item being submitted
	 * @param existing - The existing item before the update has been applied
	 * @return wasPublished - True the change event was published, otherwise false
	 */
	public boolean publishModification(final String key, final Object submitted, final Object existing) {
		Preconditions.checkNotNull(submitted, "unable to publish the modified event for key: %s since submitted data is null");
		Preconditions.checkNotNull(existing, "unable to publish the modified event for key: %s since existing data is null");
		return this.publishChanges(submitted.getClass(), key, submitted, existing);
	}
	
	

	/***
	 * Create and publish data changes as an event to the configured topic for all any interested consumers
	 * 
	 * @param clazz - The class is used as a lookup for topic and event action configuration
	 * @param key
	 * @param submitted
	 * @param existing
	 * @throws IllegalArgumentException if both submitted and existing are null
	 */
	public boolean publishChanges(final Class<?> clazz, final String key, final Object submitted, final Object existing) {
		
		Event event = null;
		if(submitted==null && existing==null){
			throw new IllegalArgumentException("Both the submitted and existing instances are null. Both cannot be null");
		}
		
		/** item was deleted **/
		if (submitted == null){ 
			event = this.createItemChangeEvent(clazz, DELETED_ACTION, key, new HashMap<String, Object>(), existing);
		/** item was created **/
		}else if (existing == null){
			event = this.createItemChangeEvent(clazz, CREATED_ACTION, key, submitted, new HashMap<String, Object>());
		/** item was modified **/
		}else {
			event = this.createItemChangeEvent(clazz, MODIFIED_ACTION, key, submitted, existing);
		}
		
		
		if(event==null){
			LOGGER.debug("state change publisher did not detect any changes for item key: {}", key);
			return false;
		}else{
			LOGGER.debug("state change publisher has detected item key: {} has been {}", key, event.getEventData().getAction());
			this.publisher.submit(event);
			return true;			
		}
	}
	
	
	
	/***
	 * Performs a diff against two java objects and returns as JsonNode as a Patch Node 
	 * 
	 * @param oldItem - The existing Java object instance
	 * @param newItem - The newer Java Object instance
	 * @return patchNode - The diff between the two
	 */
	private JsonNode diff(final Map<String, Object> source, final Map<String, Object> target){
		
		try{
			final JsonNode sourceNode = mapper.convertValue(source, JsonNode.class);
			final JsonNode targetNode = mapper.convertValue(target, JsonNode.class);
			final JsonNode diff = JsonDiff.asJson(targetNode, sourceNode);
			LOGGER.debug("diff: {}", diff.toString());
			return diff;
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	
	
	/***
	 * Creates an Event that reveals fields that have been modified
	 * 
	 * @param topic - The topic being published to
	 * @param key - The partition key for the topic
	 * @param submitted - The item containing the data being submitted, null if this is an item being deleted
	 * @param existing - The existing item, if the item already exists, null if this is an item being created
	 * @return event - The event holding the change state
	 */
	private Event createItemChangeEvent(final Class<?> clazz, final String action, final String key, final Object submitted, final Object existing){
		final Map<String, Object> submittedMap = this.mapper.convertValue(submitted, MAP_TYPE_REFERENCE);
		final Map<String, Object> existingMap = this.mapper.convertValue(existing, MAP_TYPE_REFERENCE);
		final JsonNode diffNode = this.diff(submittedMap, existingMap);
		if(diffNode==null || diffNode.size()==0 || !diffNode.isArray()){
			return null;
		}
		final String topic = this.getTopic(clazz, action);
		return this.createEventForDataChanges(topic, action, key, submittedMap, diffNode);			
	}


	
	
	/**
	 * 
	 * Creates an event ready to be published as a DataChange event
	 * 
	 * Creates a {@link DataChanges} instance based on the diff represented by a {@link JsonNode}
	 * and embeds the data changes into the event
	 * 
	 * @param topic - The topic to publish changes to
	 * @param key - The partition key
	 * @param submitted - The data that has been submitted
	 * @param diff - a {@link JsonNode} containing items that were added, removed or values that were modified
	 * @return event
	 */
	private Event createEventForDataChanges(final String topic, final String action, final String key, final Map<String, Object> submitted, JsonNode diff){
		final DataChanges changes = this.createDataChanges(submitted, diff);
		final Event event = this.createEvent(topic, action, key, changes);
		return event;
	}
	
	
	
	/***
	 * Used to create an event with DataChanges embedded
	 * @param topic - The topic to publish changes to
	 * @param key - The partition key
	 * @param changes - the {@link DataChanges} to embed
	 * @return event
	 */
	private Event createEvent(final String topic, final String action, final String key, final DataChanges changes) {
		final Map<String, Object> data = this.mapper.convertValue(changes, MAP_TYPE_REFERENCE);
		return this.createEvent(topic, action, key, data);
	}
	
	
	
	/***
	 * Create Event Data
	 * 
	 * @param topic - The topic to publish changes to
	 * @param action - The action key for this event - comparable to an event subtype
	 * @param key - The partition key
	 * @param data - The action data payload represented as Map<String, Object>
	 * @return event  - The event to publish
	 */
	private Event createEvent(final String topic, final String action, final String key, final Map<String, Object> data){
		final EventData eventData = new EventData(action, data);
		final Event event = new Event(topic, key, eventData);
		return event;
	}
	
	
	/***
	 * Create an instance of data changes
	 * @param submitted
	 * @param diff
	 * @return dataChanges
	 */
	private DataChanges createDataChanges(final Map<String, Object> submitted, final JsonNode diff){
		final Map<String, Object> added = Maps.newHashMap();
		final Map<String, Object> removed = Maps.newHashMap();
		final Map<String, Object> modified = Maps.newHashMap();
		
		for(JsonNode node : diff){
			if(node.get(OP) != null && node.get(PATH) != null){
				
				final String op = node.get(OP).asText();
				final String path = node.get(PATH).asText();
				
				/** Value can be null in some cases, during all 'removes' and when a value is being nulled out. **/
				String value = null;
				if(node.get(VALUE) !=null){
					value = node.get(VALUE).asText();	
				}
				
				if(ADD.equals(op)){
					added.put(path, value);
				}else if(REMOVE.equals(op)){
					removed.put(path, value);
				}else if(REPLACE.equals(op)){
					modified.put(path, value);
				}else{
					LOGGER.warn("Un recognized operation encountered - operation:{} - path: {}", op, path);
				}
			}
		}
		
		return DataChanges.create(submitted, added, removed, modified);
	}
	
	
	/***
	 * Get the topic from configuration
	 * @param model - The class of the event / model association with the action and event topic 
	 * @param action - The action or event subtype
	 * @return topic
	 */
	private String getTopic(final Class<?> model, final String action) {

		/** ensure this this model has been configured for actions **/
		Map<String, String> actions = getActions(model);
		if(actions==null || actions.isEmpty()){
			throw new IllegalArgumentException(String.format("No configured actions-> topic pair was found for model: %s", model.getName()));
		}
		/** ensure this action has a configured topic **/
		final String topic = actions.get(action);
		if(Strings.isNullOrEmpty(topic)){
			throw new IllegalArgumentException(String.format("state change action %s has not been configured for model: %s. Unable to publish state changes for model: %s", action, model.getName(), model.getName()));
		}
		return topic;
	}
	
	
	/***
	 * Get configured actions for the given model
	 * @param model - The class to look for configured actions for
	 * @return map - a map containing action to topic lookups
	 */
	private Map<String, String> getActions(Class<?> model) {
		if(this.config.getEvents()==null){
			throw new NullPointerException(String.format("No Events have been configured, unable to lookup topics by action for model %s", model.getName()));
		}
		
		Map<String, String> actions = this.config.getEvents().get(model.getName());
		if(actions==null){
			/** try with the simple name **/
			actions = this.config.getEvents().get(model.getSimpleName());
		}
		return actions;
	}
}
