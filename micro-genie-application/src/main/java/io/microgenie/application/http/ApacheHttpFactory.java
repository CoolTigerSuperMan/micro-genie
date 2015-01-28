package io.microgenie.application.http;



import io.microgenie.application.util.CloseableUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;



/***
 * HttpFactory Implementation - Using the Apache Http Client
 * 
 * @author shawn
 *
 */
public class ApacheHttpFactory extends HttpFactory<String> {

	
	private CloseableHttpClient httpClient;
	private final ObjectMapper mapper;
	private final ResponseHandler<String> responseHandler;
	
	
	
	/***
	 * The default Response Handler used if no handler is provided
	 */
	private static final ResponseHandler<String> DEFAULT_RESPONSE_HANDLER = new ResponseHandler<String>(){
		@Override
		public String handleResponse(HttpResponse response)throws ClientProtocolException, IOException {
			if(response !=null){
				final HttpEntity entity = response.getEntity();
				if(entity!=null){
					try{
						return EntityUtils.toString(entity, Charsets.UTF_8);		
					}finally{
						EntityUtils.consume(entity);
					}
				}				
			}
			return null;
		}
	};

	
	
	
	public ApacheHttpFactory() {
		this(new ObjectMapper(), DEFAULT_RESPONSE_HANDLER);
	}

	public ApacheHttpFactory(final ObjectMapper mapper) {
		this(mapper, DEFAULT_RESPONSE_HANDLER);
	}
	
	public ApacheHttpFactory(final ObjectMapper mapper, final ResponseHandler<String> responseHandler) {
		this.mapper = mapper;
		this.responseHandler = responseHandler;
		this.httpClient = HttpClientBuilder.create().build();
	}
	

	
	
	/***
	 * Execute HTTP GET against the given URL
	 */
	@Override
	public String get(final URL url) throws URISyntaxException {
		final HttpUriRequest request = new HttpGet(url.toURI());
		return this.execute(request);
	}

	

	/***
	 * Execute HTTP PUT with the provided payload
	 */
	@Override
	public String put(final URL url, final Object payload) throws URISyntaxException {

		final HttpPut request = new HttpPut(url.toURI());
		final HttpEntity entity = this.toHttpEntity(payload);
		request.setEntity(entity);
		return this.execute(request);
	}


	
	/***
	 * Execute HTTP POST with the provided entity payload
	 */
	@Override
	public String post(final URL url, final Object payload) throws URISyntaxException {
		final HttpPost request = new HttpPost(url.toURI());
		final HttpEntity entity = this.toHttpEntity(payload);
		request.setEntity(entity);
		return this.execute(request);
	}


	/***
	 * Execute HTTP Delete
	 */
	@Override
	public String delete(final URL url) throws URISyntaxException {
		final HttpDelete request = new HttpDelete(url.toURI());
		return this.execute(request);
	}
	
	
	
	/***
	 * Convert the payload to an HttpEntity where the contents are JSON encoded and stored as a byte array
	 * @param payload
	 * @return httpEntity
	 */
	private HttpEntity toHttpEntity(final Object payload){
		final byte[] bytes = this.encodeEntity(payload);
		final HttpEntity entity = new ByteArrayEntity(bytes, ContentType.APPLICATION_JSON);
		return entity;
	}
	
	
	/***
	 * Helper function to convert the payload to a byte array encoded as JSON
	 * @param payload
	 * @return bytes - JSON represented as a byte array
	 */
	private byte[] encodeEntity(final Object payload){
		try {
			return mapper.writeValueAsBytes(payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	
	/***
	 * Execute the Http Request
	 * @param uriRequest
	 * @return entityContents - As a String
	 */
	private String execute(final HttpUriRequest uriRequest){
		final HttpUriRequest request = Preconditions.checkNotNull(uriRequest, "An instance of HttpUriRequest is required");
		try {
			final  String response  = httpClient.execute(request, this.responseHandler);
			return response;
		} catch (Exception  e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	

	@Override
	public void close() throws IOException {
		CloseableUtil.closeQuietly(this.httpClient);
	}
}
