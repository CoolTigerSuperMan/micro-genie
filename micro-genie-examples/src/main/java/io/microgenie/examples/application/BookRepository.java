package io.microgenie.examples.application;

import io.microgenie.application.database.EntityRepository;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.Publisher;
import io.microgenie.aws.dynamodb.DynamoMapperRepository;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;



/***
 * Example Book Repository
 * @author shawn
 *
 */
public class BookRepository extends EntityRepository<Book, String, String>{
		
		public static int DEFAULT_PAGE_LIMIT = 1000;
		
		private final DynamoMapperRepository mapper;
		private final Publisher publisher;
		private final ObjectMapper objectMapper;
		
		
		public BookRepository(final DynamoMapperRepository mapper, final ObjectMapper objectMapper, final Publisher publisher){
			this.mapper = mapper;
			this.objectMapper = objectMapper;
			this.publisher = publisher;
		}
		
		
		public List<Book> getBooksFromLibrary(final String libraryId, final String isbn){
			final Book book = new Book();
			book.setLibraryId(libraryId);
			book.setIsbn(isbn);
			

			Condition condition = null; 
			if(!Strings.isNullOrEmpty(isbn)){
				condition = new Condition();
 				List<AttributeValue> isbns = new ArrayList<AttributeValue>();
				isbns.add(new AttributeValue(isbn).withS(isbn));
				condition.setAttributeValueList(isbns);
				condition.setComparisonOperator(ComparisonOperator.EQ);
			}
			
			final List<Book> books = this.mapper.queryIndexHashAndRangeKey(Book.class, book, Book.GLOBAL_INDEX_LIBRARY_ISBN, "isbn", condition, DEFAULT_PAGE_LIMIT);
			return books;
		}


		public List<Book> getBooksByIsbn( final String isbn){
			final Book book = new Book();
			book.setIsbn(isbn);
			final List<Book> books = this.mapper.queryIndexHashKey(Book.class, book, Book.GLOBAL_INDEX_ISBN, DEFAULT_PAGE_LIMIT);
			return books;
		}

		
		/***
		 * Get books by ISBN and Status
		 * @param isbn
		 * @param status
		 * @return bookList
		 */
//		public List<Book> getBooksByIsbnAndStatus(final String isbn, final String checkedOutBy){
//			
//			final Book book = new Book();
//			book.setIsbn(isbn);
//			book.setCheckedOutBy(checkedOutBy);
//			
//			final Condition condition = new Condition();
//			condition.setComparisonOperator(ComparisonOperator.EQ);
//			
//			final List<Book> books = this.mapper.queryIndexHashKey(Book.class, book, Book.GLOBAL_INDEX_ISBN_CHECKED_OUT_BY, DEFAULT_PAGE_LIMIT);
//			return books;
//		}
		
		
		
		
		@Override
		public void delete(Book book) {
			mapper.delete(book);
		}
		@Override
		public void save(Book book) {
			mapper.save(book);
			this.publisher.submit(this.createEvent(book));
		}
		

		@Override
		public void save(List<Book> books) {
			mapper.save(books);
			this.publisher.submitBatch(this.createEvents(books));
		}
		
		

		/**
		 * Get a book from a library by bookId and LibraryId
		 */
		@Override
		public Book get(String id) {
			return this.mapper.get(Book.class, id);
		}
		
		
		
		/** Get a page of books from a library **/
		@Override
		public List<Book> getList(String isbn) {
			final Book book = new Book();
			book.setIsbn(isbn);
			return this.mapper.queryIndexHashKey(Book.class, book, Book.GLOBAL_INDEX_ISBN, DEFAULT_PAGE_LIMIT);
		}
		
		
		/** not implemented for book **/
		@Override
		protected Book get(String id, String libraryId) {
			throw new RuntimeException("Not supported - for Book Repository");
		}


		public List<Book> getBooksCheckedOutByUser(String user) {
			final Book book = new Book();
			book.setCheckedOutBy(user);
			return this.mapper.queryIndexHashKey(Book.class, book, Book.GLOBAL_INDEX_CHECKED_OUT_BY, DEFAULT_PAGE_LIMIT);
		}
		
		
		
		/***
		 * Save the book if the expected condition holds up, then fire a change event
		 * 
		 * @param book
		 * @param operator
		 * @param attributeName
		 * @param expectedValue
		 */
		public void saveIf(final Book book, final ComparisonOperator operator,final String attributeName, final String expectedValue){
			final ExpectedAttributeValue expected = new ExpectedAttributeValue();
			expected.withComparisonOperator(operator);
			expected.withValue(new AttributeValue(expectedValue));
			this.mapper.saveIf(book, null,attributeName, expected);
			this.publisher.submit(this.createEvent(book));
		}
		
		
		/***
		 * Create an event when a book changes
		 * @param book
		 * @return event
		 */
		private Event createEvent(Book book) {
			try {
				final byte[] eventData = this.objectMapper.writeValueAsBytes(book);
				final Event event = new Event(EventHandlers.TOPIC_BOOK_CHANGE_EVENT, book.getBookId(), eventData);
				return event;
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		
		/***
		 * Create Events
		 * @param books
		 * @return events
		 */
		private List<Event> createEvents(List<Book> books) {
			final List<Event> events = new ArrayList<Event>();
			for(Book book : books){
				events.add(this.createEvent(book));
			}
			return events;
		}
	}