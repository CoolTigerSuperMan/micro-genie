package io.microgenie.commands.application;

import io.microgenie.commands.concurrency.ThreadCommandFactory;
import io.microgenie.commands.core.CommandFactory;
import io.microgenie.commands.http.HttpCommandFactory;

import java.io.IOException;

import org.apache.http.client.methods.HttpUriRequest;

import com.google.common.util.concurrent.ListeningExecutorService;



/***
 * Application Command Factory, containing application specific commands
 * @author shawn
 *
 */
public abstract class ApplicationCommandFactory extends CommandFactory<ApplicationCommandFactory>{

	private ThreadCommandFactory threadGroupFactory;


	public ApplicationCommandFactory(){
		this(new ThreadCommandFactory());
	}

	public ApplicationCommandFactory(ThreadCommandFactory tgFactory){
		this.threadGroupFactory = tgFactory;
	}
	
	
	public abstract  HttpCommandFactory<HttpUriRequest, String> http();
	

	public  ListeningExecutorService getExecutor(final String commandKey){
		return this.threadGroupFactory.getExecutor(commandKey);
	}
	
	
	/**
	 * Shutdown thread pools
	 */
	@Override
	public void close() throws IOException {
		threadGroupFactory.close();
	}
}
