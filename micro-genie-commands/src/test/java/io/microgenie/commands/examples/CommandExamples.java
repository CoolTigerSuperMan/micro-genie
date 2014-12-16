package io.microgenie.commands.examples;

import static io.microgenie.commands.core.Genie.Genie;
import static io.microgenie.commands.core.Genie.commands;
import io.microgenie.commands.core.CommandResult;
import io.microgenie.commands.core.Inputs.Input;
import io.microgenie.commands.http.HttpCommandFactory;
import io.microgenie.commands.mocks.Functions;
import io.microgenie.commands.mocks.Functions.TRANSFORM_FUNCTION;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpUriRequest;



/**
 * Command Examples
 * @author shawn
 */
public class CommandExamples {


	private static URL GOOGLE_URL;
	private static URL CNN_URL;
	private static URL LINKED_IN_URL;
	private static URL GIT_HUB_URL;

	
	
	private static URL BAD_GOOGLE_URL;
	private static URL BAD_LINKED_IN_URL;
	
	
	

	/**
	 * Run HTTP Commands Asynchronously with <code>command.queue()</code>
	 * <b>Asynchronous Example:</b>
	 * 
	 * <code>
	 * final String url = "http://www.google.com";
	 * final String fallbackValue = "Google is Unavailable";
	 * 
	 * final CommandResult<String> google = commands().http().get(url, fallbackValue).queue();
	 * 
	 * //print the google web page to the console
	 * System.out.println(google.get());
	 * </code>
	 * 
	 * 
	 * <p>
	 * 
	 * 
	 * Run HTTP Commands Synchronously with <code>command.execute()</code>
	 * <b>Synchronous Example:</b> 
	 * 
	 * <code>
	 * 
	 * final String url = "http://www.google.com";
	 * final String fallbackValue = "Google is Unavailable";
	 * 
	 * final String google = commands().http().get(url, fallbackValue).execute()
	 * 
	 * //print the google web page to the console
	 * System.out.println(google);
	 * 
	 * </code>
	 * 
	 * 
	 * @param args
	 * @throws TimeoutException
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws TimeoutException, ExecutionException, IOException {
		
		CommandExamples.initUrls();

		int resultCount = commands()
					.withFunction(Functions.ADDITION_FUNCTION, Input.with(10, 10))
					.asInputTo(Functions.INTEGER_TO_STRING_FUNCTION)
					.inParallel(commands().http().get(CNN_URL, "CNN Not Available"))
					.inParallel(commands().http().get(LINKED_IN_URL, "LinkedIn Not Available"))
					.inParallel(commands().http().get(GOOGLE_URL, "Google Not Available"))	
				.queue()
				.reduce(Functions.COUNT_RESULTS_FUNCTION);
		
		System.out.println("Results: " + resultCount);
		

		try{
			
			CommandExamples.runSyncronousCommands(commands().http());
			CommandExamples.runAsyncronousCommands(commands().http());
			CommandExamples.runList(commands().http());
			CommandExamples.runDependentList(commands().http());
			CommandExamples.runInputCommands();
		
		}catch(Exception ex){
			System.err.println(ex.getMessage());
		}finally{
			Genie.close();
		}
		
	}
	
	
	
	
	

	
	/**
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 */
	private static void runInputCommands() throws TimeoutException, ExecutionException{
		
		String value = commands()
				.withFunction(Functions.INTEGER_TO_STRING_FUNCTION, Input.with1(244))
				.asInputTo(commands().appCommands().http().get(CNN_URL))
				.execute();

		System.out.println(value);	
	}
	
	
	


	
		
	
	/***
	 * Execute and print HTTP Commands Synchronously
	 * 
	 * @param http
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static void runSyncronousCommands(HttpCommandFactory<HttpUriRequest, String> http) throws TimeoutException, ExecutionException {

		
		
		// This results in 'Default Fallback Google Value' being printed
		final String google = http.get(BAD_GOOGLE_URL, "Default Fallback Google Value").execute();
				
		// This results in the cnn home page being printed, unless any exception is thrown
		final String  cnn = http.get(CNN_URL).execute();
				
		// This results in 'Default Fallback LinkedInValue' being printed
		final String  linkedin = http.get(LINKED_IN_URL, "Default Fallback LinkedInValue").execute();
				
		CommandExamples.printPages(true, google, cnn, linkedin);
	}



	
	/***
	 * Execute a batch list of commands
	 * @param http - The http command factory
	 * 
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static void runList(HttpCommandFactory<HttpUriRequest, String> http) throws TimeoutException, ExecutionException{
		
		CommandResult<String> results = http.get(GOOGLE_URL)
					.inParallel(http.get(CNN_URL))
					.inParallel(http.get(LINKED_IN_URL))
					.inParallel(http.get(GIT_HUB_URL))
				.queue();
		
		/** for each result -> Print the contents **/
		results.forEach(Functions.PRINT_STRING_FUNCTION);
		
		
		/** Run a custom reduce function against the results **/
		int resultCount = results.reduce(Functions.COUNT_RESULTS_FUNCTION);
		
		
		System.out.println("run List Result Count: " + resultCount);
	}
	
	
	
	/***
	 * Execute a batch list of commands, where certain commands must complete first, before 
	 * being followed up by other commands
	 * 
	 * @param http - The http command factory
	 * 
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static void runDependentList(HttpCommandFactory<HttpUriRequest, String> http) throws TimeoutException, ExecutionException{
		
		
		
		final String resultsPage = http.get(GOOGLE_URL)
				.before(http.get(CNN_URL) )
				.asInputTo(http.get(LINKED_IN_URL) )
				.queue()				
				.transform(new TRANSFORM_FUNCTION());
		

		CommandExamples.printPages(false, resultsPage);
	}
	
	
	
	/***
	 * Execute and print HTTP Commands Asynchronously
	 * 
	 * @param http
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static  void runAsyncronousCommands(final HttpCommandFactory<HttpUriRequest, String> http) throws TimeoutException, ExecutionException {

		// This results in 'Default Fallback Google Value' being printed
		final CommandResult<String> google = http.get(BAD_GOOGLE_URL, "Default Fallback Google Value").queue();
		
		// This results in the cnn home page being printed, unless any exception is thrown
		final CommandResult<String> cnn = http.get(CNN_URL).queue();
		
		// This results in 'Default Fallback LinkedInValue' being printed
		final CommandResult<String> linkedin = http.get(BAD_LINKED_IN_URL, "Default Fallback LinkedInValue").queue();
		
		
		CommandExamples.printPages(true, google.get(), cnn.get(), linkedin.get());
	}
	
	
	
	
	
	/***
	 * Print the pages out
	 * @param pages
	 */
	public static void printPages(boolean truncate, String...pages){
		if(pages!=null){
			for(String page : pages){
				if(page!=null){
					System.out.println();
					if(truncate){
						System.out.println(Functions.TRUNCATE_STRING.apply(page));	
					}else{
						System.out.println(page);
					}
					System.out.println();
				}else{
					System.out.println("======================");
					System.out.println("Page was null");
					System.out.println("======================");
				}
			}
		}
	}
	

	
	public static void initUrls() throws MalformedURLException{
		
		GOOGLE_URL = new URL("http://www.google.com");
		CNN_URL =  new URL("http://www.cnn.com");
		LINKED_IN_URL =  new URL("http://www.linkedin.com");
		GIT_HUB_URL =  new URL("http://www.github.com");

		BAD_GOOGLE_URL = new URL("http://www.g2sdfdoogle.com");
		BAD_LINKED_IN_URL = new URL("http://www.linsdfkedin.com");
	}		
}
