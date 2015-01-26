package io.microgenie.application.commands;

import io.microgenie.application.http.HttpFactory;
import io.microgenie.commands.concurrency.ExecutorRegistry;
import io.microgenie.commands.core.CommandFactory;
import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.GenieInputCommand;
import io.microgenie.commands.core.GenieRunnableCommand;
import io.microgenie.commands.core.Inputs.Input;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/***
 * 
 * @author shawn
 *
 */
public class HttpCommandFactory extends CommandFactory{

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpFactory.class);
	
	
	private final HttpFactory<String> http;
	
	/**
	 * Constructor
	 * @param http - The underlying http factory to use
	 */
	public HttpCommandFactory(final HttpFactory<String> http){
		super();
		this.http = http;
	}
	
	
	
	
	/***
	 * Runnable HTTP Get Command
	 * @param url
	 * @return httpGetCommand
	 */
	public HttpGetCommand get(final URL url){
		return new HttpGetCommand(http, url);
	}
	
	
	/**
	 * Runnable HTTP PUT Command
	 * @param url
	 * @param entity
	 * @return httpPutCommand
	 */
	public HttpPutCommand put(final URL url, final Object entity){
		return new HttpPutCommand(http, new EntityRequest().withUrl(url).withEntity(entity));
	}
	
	/**
	 * Runnable HTTP POST Command
	 * @param url
	 * @param entity
	 * @return httpPostCommand
	 */
	public HttpPostCommand post(final URL url, final Object entity){
		return new HttpPostCommand(http, new EntityRequest().withUrl(url).withEntity(entity));
	}
	
	/***
	 * Runnable HTTP DELETE Command
	 * @param url
	 * @return httpDeleteCommand
	 */
	public HttpDeleteCommand delete(final URL url){
		return new HttpDeleteCommand(http, url);
	}
	
	
	
	public <I> HttpGetInputCommand<I> getInput(final ToEntityRequestFunction<I> toRequest){
		return new HttpGetInputCommand<I>(http, toRequest);
	}
	public <I> HttpPutInputCommand<I> putInput(final ToEntityRequestFunction<I> toRequest){
		return new HttpPutInputCommand<I>(http, toRequest);
	}
	public <I> HttpPostInputCommand<I> postInput(final ToEntityRequestFunction<I> toRequest){
		return new HttpPostInputCommand<I>(http, toRequest);
	}
	public <I> HttpDeleteInputCommand<I> deleteInput(final ToEntityRequestFunction<I> toRequest){
		return new HttpDeleteInputCommand<I>(http, toRequest);
	}

	
	/**
	 * Create HttpCommand Interface
	 * @author shawn
	 *
	 * @param <I>
	 */
	public interface HttpCommand<I>{}
	
	

	
	
	/***
	 * HTTP GET Input Command
	 */
	public class HttpGetInputCommand<I> extends HttpInputCommand<I>{

		public HttpGetInputCommand(final HttpFactory<String> http, final ToEntityRequestFunction<I> toRequest) {
			this(http, toRequest, null);
		}
		public HttpGetInputCommand(final HttpFactory<String> http,  final ToEntityRequestFunction<I> toRequest, final String defaultValue) {
			super(http, toRequest, defaultValue);
		}
		@Override
		protected String run(I input) throws ExecutionException {
			return super.get(input);
		}	
	}
	
	
	/***
	 * HTTP PUT Command
	 */
	public class HttpPutInputCommand<I> extends HttpInputCommand<I>{
		public HttpPutInputCommand(final  HttpFactory<String> http, final ToEntityRequestFunction<I> toRequest) {
			this(http, toRequest, null);
		}
		public HttpPutInputCommand(final  HttpFactory<String> http,  final ToEntityRequestFunction<I> toRequest, final String defaultValue) {
			super(http, toRequest, defaultValue);
		}
		@Override
		protected String run(I input) throws ExecutionException {
			return super.put(input);
		}
	}
	
	
	
	/***
	 * HTTP Post Command
	 */
	public class HttpPostInputCommand<I> extends HttpInputCommand<I>{
		public HttpPostInputCommand(final  HttpFactory<String> http, final ToEntityRequestFunction<I> toRequest) {
			this(http, toRequest,  null);
		}
		public HttpPostInputCommand(final  HttpFactory<String> http, final ToEntityRequestFunction<I> toRequest,  final String defaultValue) {
			super(http, toRequest, defaultValue);
		}
		@Override
		protected String run(I input) throws ExecutionException {
			return super.post(input);
		}	
	}
	
	
	/***
	 * HTTP Delete Command
	 */
	public class HttpDeleteInputCommand<I> extends HttpInputCommand<I>{
		
		public HttpDeleteInputCommand(final  HttpFactory<String> http, final ToEntityRequestFunction<I> toEntityRequest) {
			this(http, toEntityRequest, null);
		}
		public HttpDeleteInputCommand(final  HttpFactory<String> http, final ToEntityRequestFunction<I> toEntityRequest, final String defaultValue) {
			super(http, toEntityRequest, defaultValue);
		}
		@Override
		protected String run(I input) throws ExecutionException {
			return super.delete(input);
		}
	}
	
	
	
	
	
	public class HttpGetCommand extends HttpRunnableCommand{
		private final URL url;
		public HttpGetCommand(HttpFactory<String> http, final URL url){
			super(http);
			this.url = url;	
		}
		@Override
		public String run() throws ExecutionException {
			return super.get(url);
		}
	}
	public class HttpPutCommand extends HttpRunnableCommand{
		private final EntityRequest request;
		public HttpPutCommand(HttpFactory<String> http, final EntityRequest request){
			super(http);
			this.request = request;	
		}
		@Override
		public String run() throws ExecutionException {
			return super.put(request.getUrl(), request.getEntity());
		}
	}
	public class HttpPostCommand extends HttpRunnableCommand{
		private final EntityRequest request;
		public HttpPostCommand(final HttpFactory<String> http, final EntityRequest request){
			super(http);
			this.request = request;	
		}
		@Override
		public String run() throws ExecutionException {
			return super.post(request.getUrl(), request.getEntity());
		}
	}
	public class HttpDeleteCommand extends HttpRunnableCommand{
		private final URL url;
		public HttpDeleteCommand(HttpFactory<String> http, final URL url){
			super(http);
			this.url = url;	
		}
		@Override
		public String run() throws ExecutionException {
			return super.delete(url);
		}
	}
	
	
	public abstract class HttpRunnableCommand extends GenieRunnableCommand<String> implements HttpCommand<URL>{
		private final static String DEFAULT_GROUP = "DEFAULT_GROUP";
		private final HttpFactory<String> http;
		private String fallbackValue;
		public HttpRunnableCommand(final  HttpFactory<String> http) {
			this(DEFAULT_GROUP, http, null);
		}
		public HttpRunnableCommand(final  HttpFactory<String> http, final String fallbackValue) {
			this(DEFAULT_GROUP, http, fallbackValue);
		}
		public HttpRunnableCommand(final String threadGroupKey, final  HttpFactory<String> http, final String fallbackValue) {
			super(threadGroupKey, ExecutorRegistry.INSTANCE.get(threadGroupKey));
			this.http = http;
			this.fallbackValue = fallbackValue;
		}
		public String get(final URL url) throws ExecutionException{
			try {
				LOGGER.debug("executing HTTP GET: {}", url.toString());
				return this.http.get(url);
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		public String put(final URL url, final Object payload) throws ExecutionException{
			try {
				LOGGER.debug("executing HTTP PUT: {}", url.toString());
				return this.http.put(url, payload);
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		public String post(final URL url, final Object payload) throws ExecutionException{
			try {
				LOGGER.debug("executing HTTP POST: {}", url.toString());
				return this.http.post(url, payload);
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		public String delete(final URL url) throws ExecutionException{
			try {
				LOGGER.debug("executing HTTP DELETE: {}", url.toString());
				return this.http.delete(url);
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}

		/***
		 * Log the success
		 */
		@Override
		protected void success(String result) {
			LOGGER.trace("http succeeded: ", result.toString());
		}
	
		/**
		 * TODO - Add a failure strategy
		 * Log the failure
		 */
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected String fallback() {
			return fallbackValue;
		}
	}
	
	

	/***
	 * Base HTTP Command
	 * @author shawn
	 */
	public abstract class HttpInputCommand<I> extends GenieInputCommand<I, String> implements HttpCommand<I>{
		
		private final static String DEFAULT_GROUP = "DEFAULT_GROUP";
		private final  HttpFactory<String> http;
		private final ToEntityRequestFunction<I> toEntityRequest;
		private String fallbackValue;

		public HttpInputCommand(final HttpFactory<String> http, final ToEntityRequestFunction<I> toEntityRequest) {
			this(DEFAULT_GROUP, http, toEntityRequest, null);
		}
		public HttpInputCommand(final HttpFactory<String> http, final ToEntityRequestFunction<I> toEntityRequest, final String fallbackValue) {
			this(DEFAULT_GROUP, http, toEntityRequest, fallbackValue);
		}
		public HttpInputCommand(final String key, final HttpFactory<String> http, final ToEntityRequestFunction<I> toEntityRequest, final String fallbackValue) {
			super(key, ExecutorRegistry.INSTANCE.get(key));
			this.http = http;
			this.toEntityRequest = toEntityRequest;
			this.fallbackValue = fallbackValue;
		}
		protected String get(I input) throws ExecutionException{
			final EntityRequest request = this.toEntityRequest.run(Input.with(input));
			try {
				return this.http.get(request.getUrl());
			} catch (URISyntaxException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		protected String post(I input) throws ExecutionException{
			final EntityRequest request = this.toEntityRequest.run(Input.with(input));
			try {
				return this.http.post(request.getUrl(), request.getEntity());
			} catch (URISyntaxException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		protected String put(I input) throws ExecutionException{
			final EntityRequest request = this.toEntityRequest.run(Input.with(input));
			try {
				return this.http.put(request.getUrl(), request.getEntity());
			} catch (URISyntaxException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		protected String delete(I input) throws ExecutionException{
			final EntityRequest request = this.toEntityRequest.run(Input.with(input));
			try {
				return this.http.delete(request.getUrl());
			} catch (URISyntaxException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
		/***
		 * Log the success
		 */
		@Override
		protected void success(String result) {
			LOGGER.trace("http succeeded: ", result.toString());
		}
		/**
		 * TODO - Add a failure strategy
		 * Log the failure
		 */
		@Override
		protected void failure(Throwable t) {}
		@Override
		protected String fallback() {
			return fallbackValue;
		}
	}


	@Override
	public void close() throws IOException {}


	public interface ToEntityRequestFunction<I> extends Func1<I, EntityRequest>{}
	
	public static class EntityRequest{
		private Map<String, String> headers = Maps.newHashMap();
		private URL url;
		private Object entity;
		public Map<String, String> getHeaders() {
			return headers;
		}
		public URL getUrl() {
			return url;
		}
		public Object getEntity() {
			return entity;
		}
		public EntityRequest withHeaders(Map<String, String> headers) {
			this.headers = headers;
			return this;
		}
		public EntityRequest withUrl(URL url) {
			this.url = url;
			return this;
		}
		public EntityRequest withEntity(Object entity) {
			this.entity = entity;
			return this;
		}
	}
}
