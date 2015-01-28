package io.microgenie.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.application.util.CloseableUtil;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;




/**
 * ApplicationFactoryTest
 * @author shawn
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationFactoryTest {
	
	
	private final static QueueFactory queues = mock(QueueFactory.class);
	private final static FileStoreFactory files = mock(FileStoreFactory.class);
	private final static DatabaseFactory databases = mock(DatabaseFactory.class);
	private final static EventFactory events = mock(EventFactory.class);
	
	
	private static ApplicationFactory appFactory = new MockApplicationFactory();
	
	
	/***
	 * Setup 
	 */
	@Before
	public  void setup(){}
	
	
	@Test
	public void shouldCloseAllConfiguredFactories() throws IOException{
		
		appFactory.close();
		verify(databases, times(1)).close();
		verify(queues, times(1)).close();
		verify(files, times(1)).close();
		verify(events, times(1)).close();
	}
	
	
	
	/***
	 * Mock Application Factory for unit testing
	 * @author shawn
	 */
	static class MockApplicationFactory extends ApplicationFactory {
		
		@Override
		public QueueFactory queues() {
			return queues;
		}		
		@Override
		public EventFactory events() {
			return events;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public DatabaseFactory database() {
			return databases;
		}
		@Override
		public FileStoreFactory blobs() {
			return files;
		}
		@Override
		public void close() throws IOException {
			CloseableUtil.closeQuietly(this.events());
			CloseableUtil.closeQuietly(this.blobs());
			CloseableUtil.closeQuietly(this.database());
			CloseableUtil.closeQuietly(this.queues());
		}
	};
}
