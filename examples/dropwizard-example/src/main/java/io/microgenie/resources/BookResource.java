package io.microgenie.resources;

import io.microgenie.example.data.BookRepository;
import io.microgenie.models.Book;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;



/***
 * @author shawn
 */
@Path(value="books")
@Consumes(value="application/json")
@Produces(value="application/json")
public class BookResource extends BaseResource{
	
	
	private BookRepository bookRepository;
	
	
	/***
	 * Create the book resource
	 * @param bookRepository - Book data access repository
	 */
	public BookResource(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}
	
	
	
	/**
	* Get the item by it's id
	* @param id
	* @return book
	*/
	@Timed
	@GET
	public List<Book> query(@QueryParam("isbn") String isbn){
		final List<Book> books = bookRepository.getBooksByIsbn(isbn);
		return books;
	}
	
	
	

	/**
	* Get the item by it's id
	* @param id
	* @return book
	*/
	@Timed
	@GET
	@Path(value = "/{id}")
	public Book get(@PathParam("id") String id){
		final Book book = bookRepository.get(id);
		super.throwNotFoundIfNull(book, String.format("UserId: %s not found", id));
		return book;
	}
	
	
	

	/***
	* Update the given item with the specified id
	* @param id
	* @param book
	* @return Book - The updated book
	*/
	@Timed
	@PUT
	@Path(value = "/{id}")
	public Book update(@PathParam("id") String id, Book book){
		book.setBookId(id);
		bookRepository.save(book);
		return book;
	}
	
	
	
	
	/**
	* Create a new Item
	* @param book
	* @return book - The newly created book
	*/
	@Timed
	@POST
	public Book create(Book book){
		book.setBookId(UUID.randomUUID().toString());
		bookRepository.save(book);
		return book;
	}
	
	
	
	
	
	/**
	* Delete the resource with the given Id
	* @param id
	* @return 200 OK
	*/
	@Timed
	@DELETE
	@Path(value = "/{id}")
	public Response delete(@PathParam("id") String id){
		bookRepository.delete(new Book());
		return Response.status(Status.OK).build();
	}
}
