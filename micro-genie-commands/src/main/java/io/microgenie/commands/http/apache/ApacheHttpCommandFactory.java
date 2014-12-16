package io.microgenie.commands.http.apache;

import io.microgenie.commands.concurrency.ThreadCommandFactory;
import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.FunctionCommands.FunctionalCommand1;
import io.microgenie.commands.core.Inputs.Input1;
import io.microgenie.commands.http.HttpCommandFactory;
import io.microgenie.commands.http.HttpExecutor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import com.google.common.util.concurrent.ListeningExecutorService;



/***
 * HttpCommandFactory Implementation - Using the Apache Http Client
 * 
 * @author shawn
 *
 */
public class ApacheHttpCommandFactory extends HttpCommandFactory<HttpUriRequest, String> {

	public ApacheHttpCommandFactory() {
		super(new ThreadCommandFactory());
	}
	
	
	public ApacheHttpCommandFactory(ThreadCommandFactory tgFactory) {
		super(tgFactory);
	}


	/** The Http Executor wraps an instance of the Apache HTTP Client to be shared accross all requests **/
	private HttpExecutor<HttpUriRequest, String> HTTP_EXECUTOR = new ApacheClientHttpExecutor();
	

	/***
	 * @param url
	 * @return getCommand
	 */
	public HttpGetCommand get(final URL url){
		return new ApacheGetCommand(HTTP_EXECUTOR, url);
	}
	/***
	 * @param url
	 * @return getCommand
	 */
	public HttpGetCommand get(final URL url, final String defaultValue){
		return new ApacheGetCommand(HTTP_EXECUTOR, url, defaultValue);
	}
	
	/***
	 * @param url
	 * @return putCommand
	 */
	public HttpPutCommand put(final URL url){
		return new ApacheHttpPutCommand(HTTP_EXECUTOR, url);
	}
	
	/***
	 * @param url
	 * @return postCommand
	 */
	public HttpPostCommand post(final URL url){
		return new ApacheHttpPostCommand(HTTP_EXECUTOR, url);
	}
	
	/***
	 * @param url
	 * @return deleteCommand
	 */
	public HttpDeleteCommand delete(final URL url){
		return new ApacheHttpDeleteCommand(HTTP_EXECUTOR, url);
	}


	
	/***
	 * Create the Apache GET command
	 * @author shawn
	 */
	public class ApacheGetCommand extends HttpGetCommand{
		public ApacheGetCommand(HttpExecutor<HttpUriRequest, String> executor, URL url) {
			super(executor, url);
		}
		public ApacheGetCommand(HttpExecutor<HttpUriRequest, String> executor, URL url, String defaultValue) {
			super(executor, url, defaultValue);
		}
		@Override
		public HttpUriRequest createRequest(URL url) {
			try {
				return new HttpGet(url.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		@Override
		protected <I> String run(I input) {
			return this.run();
		}
	}
	
	
	public abstract class HttpGetCommandWithInput<R,A> extends FunctionalCommand1<A,R>{
		protected HttpGetCommandWithInput(Func1<A, R> function, final Input1<A> input,  String key,ListeningExecutorService executor) {
			super(function,input, key, executor);
		}
	}
	
	
	/***
	 * Apache HTTP PUT Command
	 */
	public class ApacheHttpPutCommand extends HttpPutCommand{
		public ApacheHttpPutCommand(HttpExecutor<HttpUriRequest, String> executor, URL url) {
			super(executor, url);
		}
		@Override
		public HttpUriRequest createRequest(URL url) {
			try {
				return new HttpPut(url.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		@Override
		protected <I> String run(I input) {
			return this.run();
		}
	}
	
	/***
	 * Apache HTTP Post Command
	 */
	public class ApacheHttpPostCommand extends HttpPostCommand{
		public ApacheHttpPostCommand(HttpExecutor<HttpUriRequest, String> executor, URL url) {
			super(executor, url);
		}
		@Override
		public HttpUriRequest createRequest(URL url) {
			try {
				return new HttpPost(url.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		@Override
		protected <I> String run(I input) {
			return this.run();
		}
	}
	
	
	/***
	 * Apache HTTP Delete Command
	 */
	public class ApacheHttpDeleteCommand extends HttpDeleteCommand{
		public ApacheHttpDeleteCommand(HttpExecutor<HttpUriRequest, String> executor, URL url) {
			super(executor, url);
		}
		@Override
		public HttpUriRequest createRequest(URL url) {
			try {
				return new HttpPost(url.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		@Override
		protected <I> String run(I input) {
			return this.run();
		}
	}


	@Override
	public void close() throws IOException {
		HTTP_EXECUTOR.close();
	}
}
