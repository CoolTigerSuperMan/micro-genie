package io.microgenie.example.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/***
 * A model which represents a DynamoDb table. If the table does not exist 
 * it will be created at startup, along with any indexes
 * 
 * @author shawn
 */
@DynamoDBTable(tableName="dw-example-book")
public class Book {

	public static final String GLOBAL_INDEX_LIBRARY_ID = "library-index";
	public static final String GLOBAL_INDEX_LIBRARY_ISBN = "library-isbn-index";
	public static final String GLOBAL_INDEX_ISBN = "library-isbn-index";
	public static final String GLOBAL_INDEX_ISBN_STATUS = "isbn-status-index";
	
	
	private String id;
	private String libraryId;
	private String isbn;
	private String title;
	private String description;
	private String author;
	private String status;

	public Book(){}
	public Book(final String bookId){
		this.id = bookId;
	}
	@DynamoDBIndexHashKey(attributeName="libraryId", globalSecondaryIndexNames={GLOBAL_INDEX_LIBRARY_ID, GLOBAL_INDEX_LIBRARY_ISBN})
	public String getLibraryId() {
		return libraryId;
	}
	public void setLibraryId(String libraryId) {
		this.libraryId = libraryId;
	}
	
	@DynamoDBHashKey(attributeName="bookId")
	public String getBookId() {
		return id;
	}
	public void setBookId(String bookId) {
		this.id = bookId;
	}
	
	@DynamoDBIndexHashKey(attributeName="isbn", globalSecondaryIndexNames={GLOBAL_INDEX_ISBN, GLOBAL_INDEX_ISBN_STATUS})
	@DynamoDBIndexRangeKey(attributeName="isbn", globalSecondaryIndexNames={GLOBAL_INDEX_LIBRARY_ISBN})
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	@DynamoDBIndexRangeKey(attributeName="status", globalSecondaryIndexName=GLOBAL_INDEX_ISBN_STATUS)
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
