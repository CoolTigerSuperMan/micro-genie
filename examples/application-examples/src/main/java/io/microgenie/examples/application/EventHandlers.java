package io.microgenie.examples.application;

import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventData;
import io.microgenie.application.events.EventHandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;



/****
 * Example Event Handlers
 * @author shawn
 */
public class EventHandlers {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlers.class);
	
	
	public static final String DEFAULT_CLIENT_ID = "default-example-client";
	
	

	public static final String TOPIC_CHECKOUT_BOOK_REQUEST = "CheckoutBookRequest";
	public static final int TOPIC_CHECKOUT_BOOK_REQUEST_SHARDS = 1;
	
	public static final String TOPIC_BOOK_CHANGE_EVENT = "BookChangeEvent";
	public static final int TOPIC_BOOK_CHANGE_EVENT_SHARDS = 1;
	
	/***
	 * Event Handler that receives Book Check out submissions
	 * @author shawn
	 *
	 */
	static class CheckOutRequestEventHandler implements EventHandler{
		private final BookRepository repository;
		public CheckOutRequestEventHandler(final BookRepository repository){
			this.repository = repository;
		}
		@Override
		public void handle(Event event) {
			
			final EventData eventData = event.getEventData();
			
			try {
				final String bookId = new String(eventData.getData().get("bookId").toString());
				final String userId = new String(eventData.getData().get("userId").toString());
				
				final Book book = this.repository.get(bookId);
				if(Book.CHECKED_OUT_BY_NOBODY.equals(book.getCheckedOutBy())){
					book.setCheckedOutBy(userId);
					this.repository.saveIf(book,ComparisonOperator.EQ, "checkedOutBy", Book.CHECKED_OUT_BY_NOBODY);
					LOGGER.info("=========================== SAVING BOOK CHECKOUT REQUEST ===========================");
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			
			
			
		}
		@Override
		public void handle(List<Event> events) {
			for(Event event : events){
				this.handle(event);
			}
		}
	}
	
	
	
	/****
	 * Event Handler that receives change events for books
	 * @author shawn
	 *
	 */
	static class BookChangeEventHandler implements EventHandler{
		public BookChangeEventHandler(){}
		@Override
		public void handle(Event event) {
			
			final EventData eventData = event.getEventData();
			
			try {

				final Object bookId = eventData.getData().get("bookId");
				final Object checkedOutBy = eventData.getData().get("checkedOutBy");
				
				if(!Book.CHECKED_OUT_BY_NOBODY.equals(checkedOutBy)){
					LOGGER.info("========================= Book has successfully been checked out ========================");
					LOGGER.info("**** received book change event: eventId: {}, eventCorrelationId:{},  bookId: {} checkedOutBy:{}", 
							event.getId(), event.getCorrelationId(), bookId, checkedOutBy);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		@Override
		public void handle(List<Event> events) {
			for(Event event : events){
				this.handle(event);
			}
		}
	}
	
	
	
	
	/*** Events **/
	static class BookChangeEvent {
		private Book book;
		public BookChangeEvent(){}
		public BookChangeEvent(final Book book){
			this.book = book;
		}
		public Book getBook() {
			return book;
		}
		public void setBook(Book book) {
			this.book = book;
		}	
	}
	
	
	
	/*** Events **/
	 static class CheckoutBookRequest {
		public CheckoutBookRequest(){}
		public CheckoutBookRequest(final String bookId, final String userId){
			this.bookId = bookId;
			this.userId = userId;
		}
		private String userId;
		private String bookId;
		public String getUserId() {
			return userId;
		}
		public void setUserId(String userId) {
			this.userId = userId;
		}
		public String getBookId() {
			return bookId;
		}
		public void setBookId(String bookId) {
			this.bookId = bookId;
		}
	}
}
