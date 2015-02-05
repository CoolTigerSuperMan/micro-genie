package io.microgenie.examples.application;

import io.microgenie.application.database.EntityDatabusRepository.Key;
import io.microgenie.application.database.EntityRepository;
import io.microgenie.application.events.StateChangePublisher;
import io.microgenie.aws.dynamodb.DynamoMapperRepository;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.common.base.Strings;

/***
 * Example Book Repository
 * 
 * @author shawn
 *
 */
public class BookRepository extends EntityRepository<Book> {

	public static final String TOPIC_BOOK_CHANGE = "BookChange";
	
	public static int DEFAULT_PAGE_LIMIT = 1000;

	private final DynamoMapperRepository mapper;
	private final StateChangePublisher changePublisher;

	
	/***
	 * Construct a book repository that publishes state changes
	 * @param mapper
	 * @param changePublisher
	 */
	public BookRepository(final DynamoMapperRepository mapper, final StateChangePublisher changePublisher) {
		this.mapper = mapper;
		this.changePublisher = changePublisher;
	}

	
	
	
	/***
	 * Get all copies of a given ISBN from a specific library 
	 * @param libraryId
	 * @param isbn
	 * @return bookList
	 */
	public List<Book> getBooksFromLibrary(final String libraryId, final String isbn) {
		final Book book = new Book();
		book.setLibraryId(libraryId);
		book.setIsbn(isbn);

		Condition condition = null;
		if (!Strings.isNullOrEmpty(isbn)) {
			condition = new Condition();
			List<AttributeValue> isbns = new ArrayList<AttributeValue>();
			isbns.add(new AttributeValue(isbn).withS(isbn));
			condition.setAttributeValueList(isbns);
			condition.setComparisonOperator(ComparisonOperator.EQ);
		}

		final List<Book> books = this.mapper.queryIndexHashAndRangeKey(
				Book.class, book, Book.GLOBAL_INDEX_LIBRARY_ISBN, "isbn",
				condition, DEFAULT_PAGE_LIMIT);
		return books;
	}

	
	
	/***
	 * Get Books by Id
	 * @param isbn
	 * @return bookList
	 */
	public List<Book> getBooksByIsbn(final String isbn) {
		final Book book = new Book();
		book.setIsbn(isbn);
		final List<Book> books = this.mapper.queryIndexHashKey(Book.class,
				book, Book.GLOBAL_INDEX_ISBN, DEFAULT_PAGE_LIMIT);
		return books;
	}

	
	
	
	/***
	 * Delete a book and publish a notification of deleted values
	 */
	@Override
	public void delete(Book book) {
		final Book existing = this.get(Key.create(book.getBookId()));
		mapper.delete(book);
		this.changePublisher.publishDeleted(book.getIsbn(), existing);
	}

	
	
	
	/***
	 * Save a book and publish a notification of modified values
	 */
	@Override
	public void save(Book book) {
		mapper.save(book);
	}


	
	/**
	 * Save a list of books, TODO, add change notifications to bulk calls
	 */
	@Override
	public void save(List<Book> books) {
		mapper.save(books);
	}
	
	

	/**
	 * Get a book from a library by bookId and LibraryId
	 */
	@Override
	public Book get(final Key key) {
		return this.mapper.get(Book.class, key);
	}

	
	
	/** Get a page of books from a library **/
	public List<Book> getList(String isbn) {
		final Book book = new Book();
		book.setIsbn(isbn);
		return this.mapper.queryIndexHashKey(Book.class, book,Book.GLOBAL_INDEX_ISBN, DEFAULT_PAGE_LIMIT);
	}

	
	
	
	/***
	 * Get books that are checked out by the supplied user
	 * @param user
	 * @return checkedOutBooks
	 */
	public List<Book> getBooksCheckedOutByUser(String user) {
		final Book book = new Book();
		book.setCheckedOutBy(user);
		return this.mapper.queryIndexHashKey(Book.class, book,
				Book.GLOBAL_INDEX_CHECKED_OUT_BY, DEFAULT_PAGE_LIMIT);
	}
	

	
	/***
	 * Save the book if the expected condition holds up, then fire a change
	 * event
	 * 
	 * @param book
	 * @param operator
	 * @param attributeName
	 * @param expectedValue
	 */
	public void saveIf(final Book book, final ComparisonOperator operator,
			final String attributeName, final String expectedValue) {

		final ExpectedAttributeValue expected = new ExpectedAttributeValue();
		expected.withComparisonOperator(operator);
		expected.withValue(new AttributeValue(expectedValue));
		final Book existingBook = this.get(Key.create(book.getBookId()));
		this.mapper.saveIf(book, null, attributeName, expected);
		this.changePublisher.publishChanges(Book.class, book.getIsbn(), book, existingBook);
	}
	@Override
	protected List<Book> getList(List<Book> items) {
		return this.mapper.getList(items);
	}
	@Override
	public void delete(Key key) {
		final Book b = new Book();
		b.setBookId(key.getHash());
		this.delete(b);
	}
}