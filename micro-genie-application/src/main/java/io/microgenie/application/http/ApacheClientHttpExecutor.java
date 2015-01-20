package io.microgenie.application.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Charsets;


/***
 * 
 * TODO pull HTTP Apache client implementation out into it's on module
 * 
 * HTTP Executor, executes HTTP requests
 * @author shawn
 */
public class ApacheClientHttpExecutor implements HttpExecutor<HttpUriRequest, String>{
	/** The client, which should be closed **/
	private final CloseableHttpClient client = HttpClientBuilder.create().build();
	
	
	
	/**
	 * Apply Execution
	 */
	@Override
	public String apply(HttpUriRequest input) {

		HttpEntity entity = null; 
		if(input!=null){
			try(CloseableHttpResponse response = client.execute(input)){
				entity = response.getEntity();
				return EntityUtils.toString(entity,Charsets.UTF_8);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(),e);
			}finally{

				if(entity!=null){
					EntityUtils.consumeQuietly(entity);
				}
			}				
		}
		return null;
	}	
	
	
	
	/**
	 * Close the HTTP client
	 */
	@Override
	public void close() throws IOException {
		client.close();
	}
}