package io.microgenie.commands.core;

import io.microgenie.commands.application.ApplicationCommandFactory;
import io.microgenie.commands.defaults.DefaultCommandFactory;

import java.io.IOException;


/**
 * enum singleton for accessibility to command the command factory  
 * 
 * @author shawn
 */
public enum Genie  {
	
	Genie;
	private ApplicationCommandFactory commandFactory;
	

	/**
	 * enum Singleton construction 
	 */
	Genie(){
		this.commandFactory = new DefaultCommandFactory();
	}


	
	/**
	 * Command instance
	 * @return thisInstance
	 */
	public static Genie getInstance(){
		return Genie;
	}
	

	
	public static ApplicationCommandFactory commands(){
		return Genie.commandFactory;
	}



	public void close() throws IOException {
		this.commandFactory.close();
	}
}