package io.microgenie.example.data;

import io.microgenie.application.database.EntityDatabusRepository;
import io.microgenie.application.events.StateChangePublisher;
import io.microgenie.example.models.Library;

import java.util.List;


/***
 * Library Repository, backed by data changes
 * @author shawn
 */
public class LibraryRepository extends EntityDatabusRepository<Library> {

	public LibraryRepository(final StateChangePublisher changePublisher) {
		super(Library.class, changePublisher);
	}

	@Override
	public PartitionedDataKeyWithItem<Library> createPartitionedDataKey(final Library item) {
		return null;
	}

	@Override
	protected void delete(final Library item) {
		
	}

	@Override
	protected List<Library> getList(final List<Library> items) {
		return null;
	}

	@Override
	public Library get(final Key key) {
		return null;
	}

	@Override
	public void delete(final Key key) {
		
	}

	@Override
	public void save(final Library item) {
		
	}

	@Override
	public void save(final List<Library> items) {
		
	}
}
