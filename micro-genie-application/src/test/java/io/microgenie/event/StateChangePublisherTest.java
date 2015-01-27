package io.microgenie.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import io.microgenie.application.StateChangeConfiguration;
import io.microgenie.application.events.DataChanges;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventData;
import io.microgenie.application.events.Publisher;
import io.microgenie.application.events.StateChangePublisher;

import java.util.Map;

import org.assertj.core.util.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;



/***
 * Test the publishing of state changes
 * @author shawn
 *
 */
public class StateChangePublisherTest {
	
	
	private static final String TOPIC = "TopicTestBookChanged";
	
	private final Publisher publisher = mock(Publisher.class);
	private StateChangeConfiguration config;
	private StateChangePublisher changePublisher;
	
	
	/** 
	 * Configured the actions of 'created', 'updated', and 'deleted' for Book changes 
	 * and which topic the book change events are published to
	 */
	@Before
	public void setup(){
		
		config = new StateChangeConfiguration();
		
		final Map<String, String> eventMap = Maps.newHashMap();
		eventMap.put("Created", TOPIC);
		eventMap.put("Deleted", TOPIC);
		eventMap.put("Updated", TOPIC);
		
		final Map<String, Map<String, String>> modelMap = Maps.newHashMap();
		modelMap.put(Book.class.getName(), eventMap);
		config.setEvents(modelMap);
		changePublisher = new StateChangePublisher(config, this.publisher);
	}
	
	
	
    @After
    public void tearDown() {
        reset(publisher);
    }
    
    

    

    /***
     * Test that when an item is created that does not exist, the correct data change Event is published
     */
   	@Test
   	public void shouldCorrectlyPublishCreatedEventForNewItem(){
   		final Book submittedBook = EventTestUtil.createTestBook("124", "ABC-E2-2334", "F. Kafka", "In the Penal Colony", "This is a great book");
   		
   		boolean published = this.changePublisher.publishChanges(Book.class, submittedBook.getIsbn(), submittedBook, null);
   		assertThat(published).isTrue();
   		
   		final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(publisher).submit(eventCaptor.capture());
		
		 final Event actualEvent = eventCaptor.getValue();
		 this.performCommonEventAssertions(actualEvent, TOPIC, submittedBook.getIsbn());
		 
		 /** ensure the event reveals the book as created**/
		 final EventData actualData = actualEvent.getEventData();
		 assertThat(actualData.getAction()).isEqualTo("Created");
		 
		 final DataChanges changes = DataChanges.from(actualData);
		 
		 
		 /** All fields should show up in the submitted collection **/
		 assertThat(changes.getSubmitted())
		 	.containsOnly(
					 entry("bookId", submittedBook.getBookId()),
					 entry("isbn", submittedBook.getIsbn()),
					 entry("author", submittedBook.getAuthor()),
					 entry("title", submittedBook.getTitle()),
					 entry("description", submittedBook.getDescription())
				 );
		 
		 
		 /** Since this was a create, all fields should show up in the added collection **/
		 assertThat(changes.getAdded())
		 	.containsOnly(
					 entry("/bookId", submittedBook.getBookId()),
					 entry("/isbn", submittedBook.getIsbn()),
					 entry("/author", submittedBook.getAuthor()),
					 entry("/title", submittedBook.getTitle()),
					 entry("/description", submittedBook.getDescription())
				 );
		 
		 assertThat(changes.getRemoved()).isEmpty();
		 assertThat(changes.getRemoved()).isEmpty();
    }
   	
   	
   	
   	


   	/****
   	 * Check that when an existing item is deleted, the correct delete event is published
   	 */
   	@Test
   	public void shouldCorrectlyPublishDeletedEventForDeletedItem(){
   	
   		final Book existingBook = EventTestUtil.createTestBook("124", "ABC-E2-2334", "F. Kafka", "In the Penal Colony", "This is a great book");

   		/** submitted book is null (it was deleted, but the existing book 'data before the deletion' is passed in **/
   		boolean published = this.changePublisher.publishChanges(Book.class, existingBook.getIsbn(), null, existingBook);
   		
   		assertThat(published).isTrue();
   		
   		final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(publisher).submit(eventCaptor.capture());
		
		 final Event actualEvent = eventCaptor.getValue();
		 this.performCommonEventAssertions(actualEvent, TOPIC, existingBook.getIsbn());
		 
		 
		 /** ensure the eventData action reveals the book as deleted **/
		 final EventData actualData = actualEvent.getEventData();
		 assertThat(actualData.getAction()).isEqualTo("Deleted");
		 
		 
		 DataChanges changes = DataChanges.from(actualData);
		 
		 
		 /** submitted, added, and modified field collections should be empty since this entire book was deleted**/
		 assertThat(changes.getSubmitted()).isEmpty();
		 assertThat(changes.getAdded()).isEmpty();
		 assertThat(changes.getModified()).isEmpty();
		 
		 
		 /** The removed fields collection should reflect all fields since the entire book was deleted**/
		 assertThat(changes.getRemoved())
		 	.containsOnly(
					 entry("/bookId", null),
					 entry("/isbn", null),
					 entry("/author", null),
					 entry("/title", null),
					 entry("/description", null)
				 );
		 
    }
   	
   	
   	
   	

   	/***
   	 * Test that when a Book already exists and that book is updated that an event will be published.
   	 * 
   	 * The EventData action should be modified, specifying that the event was modified.
   	 * The EventData should contain collections of fields for submitted, added, deleted, and modified.
   	 * 
   	 * All Fields that were submitted should be in the submitted collection with the associated values that were submitted.
   	 * Fields that were added should be in the added collection with their associated added values
   	 * Fields that were deleted should be in the deleted collection
   	 * Fields that were modified should be in the modified collection with their associated modified values
   	 */
	@Test
	public void shouldDetectChangesForModifiedEvents(){

		
		/** Create two books one that exists already and another that represents the update **/
		final Book existingBook = EventTestUtil.createTestBook("124", "ABC-E2-2334", "F. Kafka", "In the Penal Colony", null);
		final Book submittedBook = EventTestUtil.createTestBook("124","ABC-E2-2334", null, "In the Penal Colony - The original", "This is a good book");
		
		
		/** Execute **/
		boolean publishedChanges = this.changePublisher.publishChanges(Book.class, submittedBook.getIsbn(), submittedBook, existingBook);
		
		
		
		/** Run assertions **/
		
		/** assert we received true and that an event was actually published **/
		assertThat(publishedChanges).isTrue();
		final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(publisher).submit(eventCaptor.capture());
		
		
		/** inspect the event that was published **/
		final Event actualEvent = eventCaptor.getValue();	
		this.performCommonEventAssertions(actualEvent, TOPIC, submittedBook.getIsbn());
		
		final EventData actualEventData = actualEvent.getEventData();
		
		/** Check that this was a modification to an existing book **/
		assertThat(actualEventData.getAction()).isEqualTo("Updated");
		
		/** check that the values for field level changes are correctly reported **/
		final DataChanges changes = DataChanges.from(actualEventData);
		assertThat(changes.getAdded()).containsOnly(entry("/description", submittedBook.getDescription()));
		assertThat(changes.getRemoved()).containsOnly(entry("/author", submittedBook.getAuthor()));
		assertThat(changes.getModified()).containsOnly(entry("/title", submittedBook.getTitle()));
		
		/** submitted should contain all field / value pairs that were submitted **/ 
		 assertThat(changes.getSubmitted())
		 	.containsOnly(
					 entry("bookId", submittedBook.getBookId()),
					 entry("isbn", submittedBook.getIsbn()),
					 entry("title", submittedBook.getTitle()),
					 entry("description", submittedBook.getDescription())
				);
	}
	
	
    
   	/***
   	 * Common Event assertions, since event is the outer container all events contain these properties and
   	 * need these assertions
   	 * 
   	 * @param event - The actual event sent to the publisher in the test
   	 * @param topic - The actual topic the event should have been published to in the test
   	 * @param expectedPatitionKey - The actual partitionKey that should have been used in the test
   	 */
   	public void performCommonEventAssertions(final Event event, final String topic, final String expectedPatitionKey){
		assertThat(event.getTopic()).isEqualTo(topic);
		assertThat(event.getPartitionKey()).isEqualTo(expectedPatitionKey);
		assertThat(event.getId()).isNotEmpty();
		/** If an event is not correlated to another event, then the correlationId should be equal to the eventId **/
		assertThat(event.getCorrelationId()).isEqualTo(event.getId());
   	}
   	
   	
    
    /**
     * Mock book item for testing
     * @author shawn
     *
     */
	public static class Book{
		private String bookId;
		private String isbn;
		private String title;
		private String author;
		private String description;
		public String getIsbn() {
			return isbn;
		}
		public void setIsbn(String isbn) {
			this.isbn = isbn;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getBookId() {
			return bookId;
		}
		public void setBookId(String bookId) {
			this.bookId = bookId;
		}
	}
}
