package io.microgenie.commands.defaults;

import io.microgenie.commands.application.ApplicationCommandFactory;
import io.microgenie.commands.concurrency.ThreadCommandFactory;
import io.microgenie.commands.http.HttpCommandFactory;
import io.microgenie.commands.http.apache.ApacheHttpCommandFactory;

import java.io.IOException;

import org.apache.http.client.methods.HttpUriRequest;



/***
 * @author shawn
 */
public class DefaultCommandFactory extends ApplicationCommandFactory{

	
	private HttpCommandFactory<HttpUriRequest, String> http;
	
	
	public DefaultCommandFactory(){
		this(new ThreadCommandFactory());
	}
	
	
	public DefaultCommandFactory(ThreadCommandFactory tgFactory) {
		super(tgFactory);
		 this.http = new ApacheHttpCommandFactory(tgFactory);
	}

	
	
	/***
	 * Http Commands
	 */
	@Override
	public  HttpCommandFactory<HttpUriRequest, String>  http() {
		return this.http;
	}


	@Override
	public void close() throws IOException {
		super.close();
		this.http.close();
	}

	@Override
	public ApplicationCommandFactory appCommands() {
		return this;
	}
}
