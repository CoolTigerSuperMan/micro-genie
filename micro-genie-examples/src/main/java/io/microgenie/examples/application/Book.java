package io.microgenie.examples.application;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;




/***
 * A model which represents a dynamoDb table. If the table does not exist 
 * it will be created at startup
 * 
 * @author shawn
 */
@DynamoDBTable(tableName="library-books")
public class Book {
	
	public static final String GLOBAL_INDEX_LIBRARY_ISBN = "library-isbn-index";
	public static final String GLOBAL_INDEX_ISBN = "isbn-index";
	public static final String GLOBAL_INDEX_CHECKED_OUT_BY = "checkedOutBy-index";
	
	/** Used to note that the book is not checked out by anyone. This is the default **/
	public static final String CHECKED_OUT_BY_NOBODY = "NOBODY";

	
	private String bookId;
	private String libraryId;
	private String isbn;
	private String title;
	private String description;
	private String author;
	private String checkedOutBy = CHECKED_OUT_BY_NOBODY;
	
	
	@DynamoDBHashKey(attributeName="bookId")
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	
	
	@DynamoDBIndexHashKey(attributeName="libraryId", globalSecondaryIndexName=GLOBAL_INDEX_LIBRARY_ISBN)
	public String getLibraryId() {
		return libraryId;
	}
	public void setLibraryId(String libraryId) {
		this.libraryId = libraryId;
	}

	
	@DynamoDBIndexHashKey(attributeName="isbn", globalSecondaryIndexNames={GLOBAL_INDEX_ISBN})
	@DynamoDBIndexRangeKey(attributeName="isbn", globalSecondaryIndexName=GLOBAL_INDEX_LIBRARY_ISBN)
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
	
	
	@DynamoDBIndexHashKey(attributeName="checkedOutBy", globalSecondaryIndexName=GLOBAL_INDEX_CHECKED_OUT_BY)
	public String getCheckedOutBy() {
		return checkedOutBy;
	}
	
	public void setCheckedOutBy(String checkedOutBy) {
		this.checkedOutBy = checkedOutBy;
	}
	
	
	public Book copy() {
		final Book copy = new Book();
		copy.setAuthor(this.getAuthor());
		copy.setBookId(this.getBookId());
		copy.setDescription(this.getDescription());
		copy.setIsbn(this.getIsbn());
		copy.setLibraryId(this.getLibraryId());
		copy.setTitle(this.getTitle());
		copy.setCheckedOutBy(this.getCheckedOutBy());
		return copy;
	}
	
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("ISBN: ").append(this.getIsbn()).append(" - ");
		sb.append("LibraryId: ").append(this.getLibraryId()).append(" - ");
		sb.append("BookId: ").append(this.getBookId()).append(" - ");
		sb.append("CheckedOutBY: ").append(this.getCheckedOutBy()).append(" - ");
		sb.append("Title: ").append(this.getTitle()).append(" - ");
		sb.append("Author: ").append(this.getAuthor()).append(" - ");
		sb.append("Description: ").append(this.getDescription());
		return sb.toString();
	}
}
