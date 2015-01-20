package io.microgenie.resources;

import io.microgenie.core.Book;
import io.microgenie.data.BookRepository;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.ApiResponse;


/***
 * @author shawn
 */
@Path(value="users")
@Api(value="users", description="Create, Update, Read, Delete Books", consumes="application/json", produces="application/json")
@Consumes(value="application/json")
@Produces(value="application/json")
public class BookResource extends BaseResource{
	
	private BookRepository bookRepository;
	
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
	@Path(value = "/{id}")
	@ApiOperation(value="Get a Book by Id", response=Book.class, nickname="Book")
	@ApiResponses(value = {@ApiResponse(code=200, message = "Success", response=Book.class), @ApiResponse(code=404, message = "Book Not Found", response=ApiError.class)})
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
	@ApiOperation(value="Update a Book by Id", response=Book.class, nickname="Book")
	@ApiResponses(value = {@ApiResponse(code=200, message = "Success", response=Book.class),
			 			   @ApiResponse(code=404, message = "Book Not Found", response=ApiError.class)})
	public Book update(@PathParam("id") String id, Book book){
		book.setId(id);
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
	@ApiOperation(value="Create a new Book", response=Book.class, nickname="Book")
	@ApiResponses(value = { @ApiResponse(code=200, message = "Success", response=Book.class)})
	public Book create(Book book){
		book.setId(UUID.randomUUID().toString());
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
	@ApiOperation(value="Delete a Book")
	public Response delete(@PathParam("id") String id){
		bookRepository.delete(id);
		return Response.status(Status.OK).build();
	}
}
