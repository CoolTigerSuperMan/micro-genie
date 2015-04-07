package io.microgenie.example.resources;

import io.microgenie.application.database.EntityDatabusRepository.Key;
import io.microgenie.example.data.LibraryRepository;
import io.microgenie.example.models.Library;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.codahale.metrics.annotation.Timed;


@Path(value="libraries")
@Consumes(value="application/json")
@Produces(value="application/json")
public class LibraryResource extends BaseResource{
	
	private final LibraryRepository repository;
	
	public LibraryResource(final LibraryRepository repository){
		this.repository = repository;
	}
	

	/**
	* Get the item by it's id
	* @param id
	* @return library
	*/
	@Timed
	@GET
	@Path(value = "/{id}")
	public Library get(@PathParam("id") String id){
		final Library library = repository.get(Key.create(id));
		super.throwNotFoundIfNull(library, String.format("LibraryId: %s not found", id));
		return library;
	}
}
