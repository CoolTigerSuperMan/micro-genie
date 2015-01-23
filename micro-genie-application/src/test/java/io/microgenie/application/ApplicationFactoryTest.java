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
	private final static HttpFactory<String> http = (HttpFactory<String>)mock(HttpFactory.class);
	
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
	public  void setup(){
		appFactory.registerDatabase(databases);
		appFactory.registerQueues(queues);
		appFactory.registerFiles(files);
		appFactory.registerEvents(events);
		appFactory.registerCommands(commands);
		appFactory.registerHttp(http);
	}
	
	
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
		
		private volatile boolean isInitialized;
		private QueueFactory queues;
		private DatabaseFactory databases;
		private FileStoreFactory files;
		private ApplicationCommandFactory commands;
		private EventFactory events;
		private HttpFactory<String> http;
		@Override
		public void registerQueues(QueueFactory queues) {
			this.queues = queues;
		}
		@Override
		public void registerHttp(HttpFactory<String> http) {
			this.http = http;
		}
		@Override
		public void registerFiles(FileStoreFactory files) {
			this.files = files;
		}
		@Override
		public void registerEvents(EventFactory events) {
			this.events = events;
		}
		@Override
		public void registerDatabase(DatabaseFactory database) {
			this.databases = database;
		}
		@Override
		public void registerCommands(ApplicationCommandFactory commands) {
			this.commands = commands;
		}
		@Override
		public QueueFactory queues() {
			return this.queues;
		}		
		@Override
		public HttpFactory<String> http() {
			return this.http;
		}
		@Override
		public EventFactory events() {
			return this.events;
		}
		@Override
		public DatabaseFactory database() {
			return this.databases;
		}
		@Override
		public ApplicationCommandFactory commands() {
			return this.commands;
		}
		@Override
		public FileStoreFactory blobs() {
			return this.files;
		}
		@Override
		public void initialize() {
			this.isInitialized = true;
		}
		@Override
		public boolean isInitialized() {
			return isInitialized;
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
