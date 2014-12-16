package io.microgenie.commands.http;

import io.microgenie.commands.concurrency.ThreadCommandFactory;
import io.microgenie.commands.core.AbstractCommand;

import java.io.Closeable;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;



/***
 * Create HTTP Commands
 * @author shawn
 *
 */
public abstract class HttpCommandFactory<I, O> implements Closeable{

	private ThreadCommandFactory tgFactory;
	public HttpCommandFactory(){
		this(new ThreadCommandFactory());
	}	
	public HttpCommandFactory(ThreadCommandFactory tgFactory){
		this.tgFactory = tgFactory;
	}
	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	public abstract HttpGetCommand get(final URL url);
	public abstract HttpGetCommand get(final URL url, final O defaultValue);
	public abstract HttpPutCommand put(final URL url);
	public abstract HttpPostCommand post(final URL url);
	public abstract HttpDeleteCommand delete(final URL url);
	
	
	
	
	
	/***
	 * HTTP GET Command
	 */
	public abstract class HttpGetCommand extends HttpCommandBase{
		public HttpGetCommand(final HttpExecutor<I, O> executor, final URL url) {
			this(executor, url, null);
		}
		public HttpGetCommand(final HttpExecutor<I, O> executor, final URL url, final O defaultValue) {
			super(executor, url, defaultValue);
		}
		public abstract I createRequest(URL url);
	}
	
	
	
	
	
	
	/***
	 * HTTP PUT Command
	 */
	public abstract class HttpPutCommand extends HttpCommandBase{
		public HttpPutCommand(final HttpExecutor<I,O> executor, final URL url) {
			this(executor, url, null);
		}
		public HttpPutCommand(final HttpExecutor<I,O> executor, final URL url, final O defaultValue) {
			super(executor, url, defaultValue);
		}
		public abstract I createRequest(URL url);
	}
	
	
	
	/***
	 * HTTP Post Command
	 */
	public abstract class HttpPostCommand extends HttpCommandBase{
		public HttpPostCommand(final HttpExecutor<I,O> executor, final URL url) {
			this(executor, url, null);
		}
		public HttpPostCommand(final HttpExecutor<I,O> executor, final URL url, final O defaultValue) {
			super(executor, url, defaultValue);
		}
		public abstract I createRequest(URL url);
	}
	
	
	
	
	/***
	 * HTTP Delete Command
	 */
	public abstract class HttpDeleteCommand extends HttpCommandBase{
	
		public HttpDeleteCommand(final HttpExecutor<I,O> executor, final URL url) {
			this(executor, url, null);
		}
		public HttpDeleteCommand(final HttpExecutor<I,O> executor, final URL url, final O defaultValue) {
			super(executor, url, defaultValue);
		}	
	}
	
	
	
	/**
	 * Create HttpCommand Interface
	 * @author shawn
	 *
	 * @param <I>
	 */
	public interface HttpCommand<I>{
		public I createRequest(final URL input);
		public Header[] createHeaders();
	}
	
	
	/***
	 * Base HTTP Command
	 * @author shawn
	 */
	public abstract class HttpCommandBase extends AbstractCommand<O> implements HttpCommand<I>{
		
		private final static String DEFAULT_GROUP = "DEFAULT_GROUP";
		private final HttpExecutor<I, O> httpExecutor;
		private final URL url;
		
		private final String contentType;
		private O fallbackValue;
		
		
		/**
		 * An HTTP Command
		 * 
		 * @param httpExecutor
		 * @param url
		 */
		public HttpCommandBase(final HttpExecutor<I, O> executor, final URL url) {
			this(DEFAULT_GROUP, executor, url,  DEFAULT_CONTENT_TYPE, null);
		}

		public HttpCommandBase(final HttpExecutor<I , O> executor, final URL url, final O fallbackValue) {
			this(DEFAULT_GROUP, executor, url,  DEFAULT_CONTENT_TYPE, fallbackValue);
		}
		public HttpCommandBase(final String groupKey, final HttpExecutor<I,O> executor, final URL url, final O fallbackValue) {
			this(groupKey, executor, url,  DEFAULT_CONTENT_TYPE, fallbackValue);
		}
		public HttpCommandBase(final String key, final HttpExecutor<I, O> executor, final URL url, final String contentType, final O fallbackValue) {
			super(key,tgFactory.getExecutor(key));
			this.httpExecutor = executor;
			this.url = url;
			this.contentType = contentType;
			this.fallbackValue = fallbackValue;
		}
		
	
		public abstract I createRequest(final URL input);
		
		
		/**
		 * Execute the HTTP Requests
		 */
		@Override
		protected O run() {
			final I request = this.createRequest(url);
			return this.httpExecutor.apply(request);
		}
		/***
		 * Log the success
		 */
		@Override
		protected void success(O result) {}
		/**
		 * TODO - Add a failure strategy
		 * Log the failure
		 */
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected O fallback() {
			return fallbackValue;
		}
		/**
		 * Create Headers
		 * @return headers
		 */
		public Header[] createHeaders(){
			return new Header[]{new BasicHeader(HttpHeaders.CONTENT_TYPE, this.contentType)};
		}
	}
}
