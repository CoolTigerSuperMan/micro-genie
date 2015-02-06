package io.microgenie.example.data;

import io.microgenie.application.database.EntityDatabusRepository.Key;
import io.microgenie.application.database.EntityRepository;
import io.microgenie.aws.dynamodb.DynamoMapperRepository;
import io.microgenie.example.models.Book;

import java.util.List;

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;


/***
 * Book Repository Interface for data access
 * @author shawn
 */
/***
 * DynamoDb book Repository
 * @author shawn
 */
public class BookRepository extends EntityRepository<Book>{
	
	public static int DEFAULT_PAGE_LIMIT = 20;
	
	private final DynamoMapperRepository mapper;
	public BookRepository(DynamoMapperRepository mapper){
		this.mapper = mapper;
	}
	
	
	public List<Book> getBooksFromLibrary(final String libraryId, final String isbn){
		final Book book = new Book();
		book.setIsbn(isbn);
		book.setLibraryId(libraryId);
		final Condition condition = new Condition();
		condition.setComparisonOperator(ComparisonOperator.EQ);
		final List<Book> books = this.mapper.queryIndexHashAndRangeKey(Book.class, book, Book.GLOBAL_INDEX_LIBRARY_ISBN, "libraryId", condition, DEFAULT_PAGE_LIMIT);
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
	public List<Book> getBooksByIsbnAndStatus(final String isbn, final String status){
		
		final Book book = new Book();
		book.setIsbn(isbn);
		book.setStatus(status);
		
		final Condition condition = new Condition();
		condition.setComparisonOperator(ComparisonOperator.EQ);
		
		final List<Book> books = this.mapper.queryIndexHashKey(Book.class, book, Book.GLOBAL_INDEX_ISBN_STATUS, DEFAULT_PAGE_LIMIT);
		return books;
	}
	@Override
	public void delete(Book book) {
		mapper.delete(book);
	}
	@Override
	public void save(Book book) {
		mapper.save(book);
	}
	@Override
	public void save(List<Book> books) {
		mapper.save(books);
	}
	@Override
	public Book get(final Key key) {
		return this.mapper.get(Book.class, key);
	}
	
	
	public List<Book> getList(String libraryId) {
		final Book book = new Book();
		book.setLibraryId(libraryId);
		return this.mapper.queryIndexHashKey(Book.class, book, Book.GLOBAL_INDEX_LIBRARY_ID, DEFAULT_PAGE_LIMIT);
	}


	@Override
	protected List<Book> getList(List<Book> items) {
		return this.mapper.getList(items);
	}


	@Override
	public void delete(Key key) {
		final Book b = new Book();
		b.setBookId(key.getHash());
		this.mapper.delete(b);
	}
}
