package io.microgenie.application.http;

import java.io.Closeable;
import java.net.URISyntaxException;
import java.net.URL;



/***
 * Create HTTP Commands
 * @author shawn
 */
public abstract class HttpFactory<O> implements Closeable{
	
	public HttpFactory(){}

	public abstract void initialize();
	
	public abstract O get(URL url) throws URISyntaxException;
	public abstract O put(URL url, Object payload) throws URISyntaxException;
	public abstract O post(URL url, Object payload) throws URISyntaxException;
	public abstract O delete(URL url) throws URISyntaxException;
}
