package io.microgenie.event;

import io.microgenie.application.events.DataChanges;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventData;
import io.microgenie.event.StateChangePublisherTest.Book;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/***
 * Event Test Utilities
 * @author shawn
 *
 */
public class EventTestUtil {
	

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String,Object>>() {};
	
	
	/***
	 * Convert any instance to Map<String, Object>
	 * @param instance
	 * @return map
	 */
	public static Map<String, Object> convertToMap(Object instance){
		return MAPPER.convertValue(instance, MAP_TYPE);
	}
	

	/***
	 * Used to create an event with DataChanges embedded
	 * @param topic - The topic to publish changes to
	 * @param key - The partition key
	 * @param submittedBook - The Book instance being submitted
	 * @param changes - the complete map of key / value pairs of the data being submitted.
	 * @param added - A map of key / value pairs of items being added, where the key is the json path to the item being added
	 * 	and value is the new value being added
	 * @param removed - A map of key / value pairs of items being removed, where the key is the json path to the item being removed
	 * 		value will always be null for removal
	 * @param modified - A map of key / value pairs of items being added, where the key is the json path to the item being modified 
	 * 	and value is the new value
	 * 
	 * @return event
	 */
	public static Event createEvent(final String topic, final String action, final String key, final Book submittedBook, final Map<String, Object> added, final Map<String, Object> removed, final Map<String, Object> modified) {
		
		final Map<String, Object> submittedBookMap = EventTestUtil.convertToMap(submittedBook);
		final DataChanges changes = DataChanges.create(submittedBookMap, added, removed, modified);
		final Map<String, Object> data = MAPPER.convertValue(changes, MAP_TYPE);
		final EventData eventData = new EventData(action, data);
		final Event event = new Event(topic, key, eventData);
		return event;
	}
	
	
	/***
	 * Helper method to create book instances for tests
	 * @param isbn
	 * @param author
	 * @param title
	 * @param description
	 * @return book
	 */
	public static Book createTestBook(final String id, final String isbn, final String author, final String title, final String description){
		final Book book = new Book();
		book.setBookId(id);
		book.setIsbn(isbn); 
		book.setAuthor(author);
		book.setTitle(title);
		book.setDescription(description);
		return book;
	}
}
