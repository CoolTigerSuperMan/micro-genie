package io.microgenie.application;

import org.mockito.runners.*;

import static org.mockito.Mockito.*;

import java.io.IOException;








import io.microgenie.application.blob.FileStoreFactory;
import io.microgenie.application.commands.ApplicationCommandFactory;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.http.HttpFactory;
import io.microgenie.application.queue.QueueFactory;
import io.microgenie.commands.util.CloseableUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;




/**
 * ApplicationFactoryTest
 * @author shawn
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationFactoryTest {
	
	@SuppressWarnings("unchecked")
	private final static HttpFactory<String> http = mock(HttpFactory.class);
	private final static QueueFactory queues = mock(QueueFactory.class);
	private final static FileStoreFactory files = mock(FileStoreFactory.class);
	private final static DatabaseFactory databases = mock(DatabaseFactory.class);
	private final static EventFactory events = mock(EventFactory.class);
	
	private final static ApplicationCommandFactory commands = mock(ApplicationCommandFactory.class);
	
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
		verify(commands, times(1)).close();
		verify(http, times(1)).close();;
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
		public HttpFactory<String> http() {
			return http;
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
		public ApplicationCommandFactory commands() {
			return commands;
		}
		@Override
		public FileStoreFactory blobs() {
			return files;
		}
		@Override
		public void close() throws IOException {
			CloseableUtil.closeQuietly(this.http());
			CloseableUtil.closeQuietly(this.events());
			CloseableUtil.closeQuietly(this.blobs());
			CloseableUtil.closeQuietly(this.database());
			CloseableUtil.closeQuietly(this.queues());
			CloseableUtil.closeQuietly(this.commands());
		}
	};
}
