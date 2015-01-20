package io.microgenie.data;

import io.microgenie.core.Book;


/***
 * Book Repository Interface for data access
 * @author shawn
 */
public interface BookRepository {

	public Book get(final String id);
	public void save(final Book book);
	public void delete(String id);

}
