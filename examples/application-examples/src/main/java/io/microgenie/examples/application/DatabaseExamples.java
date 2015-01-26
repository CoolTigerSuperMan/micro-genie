package io.microgenie.examples.application;

import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.database.EntityRepository;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.AwsConfig;
import io.microgenie.aws.dynamodb.DynamoMapperRepository;
import io.microgenie.examples.ExampleConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;


/**
 * Database Examples
 * @author shawn
 */
public class DatabaseExamples {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseExamples.class);
	
	
	
	/****
	 * Execute example database calls using the dynamodb database factory
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args ) throws IOException{

		final AwsConfig aws  = ExampleConfig.createConfigForDatabaseExamples();
		
		
		final DynamoMapperRepository mapperRepository = DynamoMapperRepository.create(new AmazonDynamoDBClient());
		
		
		/*** TODO - Need to add the 'wait until table creation complete' logic **/
		try (ApplicationFactory app = new AwsApplicationFactory(aws, false)) {
			
			LOGGER.info("initializing book repository. This will create tables if they do not exist");
			app.database().registerRepo(Book.class, new BookRepository(mapperRepository));
			
			LOGGER.info("initialization of book repository complete");

			
			LOGGER.info("Executing create, read, update and delete examples.....");
			final Book createdBook = DatabaseExamples.create(app.database());
			final Book retrievedBook = DatabaseExamples.read(app.database(), createdBook.getLibraryId(), createdBook.getBookId());
			final Book updatedBook = DatabaseExamples.update(app.database(), retrievedBook);
			
			/** Delete the updated book **/
			DatabaseExamples.delete(app.database(), updatedBook);
			LOGGER.info("Execution of examples complete, shuting down now");
		}
		
		LOGGER.info("Shutdown complete for all resources...exiting now");
	}
	



	/**
	 * Create a new book
	 * @param database
	 * @return book
	 */
	private static Book create(final DatabaseFactory database) {

		final String libraryId = UUID.randomUUID().toString();
		final String bookId = UUID.randomUUID().toString();
		final BookRepository bookRepository = database.repos(Book.class);

		final Book book = new Book();
		book.setLibraryId(libraryId);
		book.setBookId(bookId);
		book.setTitle("The Old Man and the Sea");
		book.setDescription("Here Hemingway recasts, in strikingly contemporary style, the classic theme of courage in the face of defeat, of personal triumph won from loss. Written in 1952, this hugely successful novella confirmed his power and presence in the literary world and played a large part in his winning the 1954 Nobel Prize for Literature.");
		book.setIsbn("978-0684801223");
		book.setAuthor("Ernest Hemingway");
		
		LOGGER.info("saving book title {} for libraryId: {} with bookId: {}", book.getTitle(), book.getLibraryId(), book.getBookId());
		
		bookRepository.save(book);
		
		LOGGER.info("successfully saved book title {} for libraryId: {} with bookId: {}", book.getTitle(), book.getLibraryId(), book.getBookId());
		
		return book;
	}
	


	/***
	 * Read a book from the database
	 * 
	 * @param database
	 * @param libraryId
	 * @param bookId
	 * @return book
	 */
	private static Book read(final DatabaseFactory database, String libraryId, String bookId) {
		
		LOGGER.info("attempting to read book with libraryId: {} and bookId: {}", libraryId, bookId);
		
		final BookRepository bookRepo = database.repos(Book.class);
		final Book book = bookRepo.get(libraryId, bookId);
		
		if(book!=null){
			LOGGER.info("successfully read book with libraryId: {} and bookId: {}", libraryId, bookId);	
		}else{
			LOGGER.info("book was not found with libraryId: {} and bookId: {}", libraryId, bookId);
		}
		
		return book;
	}





	/***
	 * Update an existing book
	 * 
	 * @param database
	 * @param retrievedBook
	 * @return updatedBook
	 */
	private static Book update(final DatabaseFactory database, Book book) {

		LOGGER.info("attempting to update book for libraryId: {} bookId: {} with title: {}", book.getLibraryId(), book.getBookId(), book.getTitle());	
		
		final String updatedTitle = book.getTitle() + " -  Updated";
		book.setTitle(updatedTitle);
		final BookRepository bookRepository = database.repos(Book.class);
		bookRepository.save(book);
		
		LOGGER.info("successfully updated book title to: '{}' for libraryId: {} bookId: {}", book.getTitle(), book.getLibraryId(), book.getBookId());
		
		return book;
	}



	
	
	
	/***
	 * Delete an existing book
	 * @param database
	 * @param updatedBook
	 */
	private static void delete(final DatabaseFactory database, final Book updatedBook) {
		final BookRepository bookRepository = database.repos(Book.class);
		
		LOGGER.info("attempting to delete book for libraryId: {} bookId: {}", updatedBook.getLibraryId(), updatedBook.getBookId());
		
		bookRepository.delete(updatedBook);
		
		LOGGER.info("successfully deleted book for libraryId: {} bookId: {}", updatedBook.getLibraryId(), updatedBook.getBookId());
	}




	/***
	 * A model which represents a dynamoDb table. If the table does not exist 
	 * it will be created at startup
	 * 
	 * @author shawn
	 */
	@DynamoDBTable(tableName="example-book-table")
	public static class Book {
		
		private String libraryId;
		private String bookId;
		private String isbn;
		private String title;
		private String description;
		private String author;
		
		@DynamoDBHashKey(attributeName="libraryId")
		public String getLibraryId() {
			return libraryId;
		}
		public void setLibraryId(String libraryId) {
			this.libraryId = libraryId;
		}

		@DynamoDBRangeKey(attributeName="bookId")
		public String getBookId() {
			return bookId;
		}
		public void setBookId(String bookId) {
			this.bookId = bookId;
		}
		
		@DynamoDBIndexHashKey(attributeName="isbn", globalSecondaryIndexName="isbn-index")
		public String getIsbn() {
			return isbn;
		}
		public void setIsbn(String isbn) {
			this.isbn = isbn;
		}
		@DynamoDBAttribute(attributeName="title")
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		@DynamoDBAttribute(attributeName="description")
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		@DynamoDBAttribute(attributeName="author")
		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
		}
	}
	
	
	
	
	/***
	 * DynamoDb book Repository
	 * @author shawn
	 */
	public static class BookRepository extends EntityRepository<Book, String, String>{
		private final DynamoMapperRepository mapper;
		public BookRepository(DynamoMapperRepository mapper){
			this.mapper = mapper;
		}
		@Override
		protected Book get(String id) {
			return this.mapper.get(Book.class, id);
		}
		@Override
		protected Book get(String id, String rangeKey) {
			return this.mapper.get(Book.class, id, rangeKey);
		}
		@Override
		protected List<Book> getList(String hashKey) {
			final List<Book> books = new ArrayList<Book>();
			final Book book =new Book();
			book.setBookId(hashKey);
			books.add(book);
			return this.mapper.getList(books);
		}
		@Override
		public void delete(final Book book) {
			this.mapper.delete(book);
		}
		@Override
		public void save(final Book book) {
			this.mapper.save(book);
		}
		@Override
		public void save(final List<Book> books) {
			this.mapper.save(books);
		}
	}
}
