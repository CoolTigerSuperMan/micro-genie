package io.microgenie.examples.commands;



import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.blob.FileContent;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.commands.ApplicationCommandFactory;
import io.microgenie.application.commands.BlobSpecs.DefaultFileInputSpec;
import io.microgenie.application.commands.FileStoreCommnandFactory.SaveFileInputCommand;
import io.microgenie.application.commands.FileStoreCommnandFactory.ToFileFunction;
import io.microgenie.application.commands.QueueCommandFactory.ToMessageFunction;
import io.microgenie.application.queue.Message;
import io.microgenie.application.queue.MessageHandler;
import io.microgenie.application.queue.ProduceInputCommand;
import io.microgenie.application.queue.QueueSpecs.DefaultQueueInputSpec;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.AwsConfig;
import io.microgenie.commands.core.CommandResult;
import io.microgenie.commands.core.GenieInputCommand;
import io.microgenie.commands.core.Inputs.Input2;
import io.microgenie.commands.core.Inputs.Input3;
import io.microgenie.examples.ExampleConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Examples
 * 
 * @author shawn
 */
public class CommandExamples {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandExamples.class);
	

	private static URL CNN;
	private static URL GOOGLE;
	private static URL LINKED_IN;
	
	


	/**
	 * @param args
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws TimeoutException,ExecutionException, IOException, InterruptedException {

		final Properties properties = ExampleConfig.getProperties(ExampleConfig.PROPERTY_FILE_NAME);
		final AwsConfig config  = ExampleConfig.createConfig(ExampleConfig.PROPERTY_FILE_NAME);
		
		CommandExamples.initUrls();
		try (ApplicationFactory app = new AwsApplicationFactory(config, true)) {
			CommandExamples.executeGenieContainer(app, properties);
		}
	}

	
	
	
	/***
	 * Execute In parallel
	 * 
	 * @param commands
	 * @throws ExecutionException 
	 * @throws TimeoutException 
	 * @throws InterruptedException 
	 */
	public static void executeGenieContainer(final ApplicationFactory app, final Properties properties) throws TimeoutException, ExecutionException, InterruptedException {
		
		final ApplicationCommandFactory commands = app.commands();
		final String defaultBucket = properties.getProperty(ExampleConfig.BUCKET_PROPERTY);
		final String queue = properties.getProperty(ExampleConfig.FILE_SAVED_QUEUE_PROPERTY);
		
		final DefaultFileInputSpec<String> blobSpec = new DefaultFileInputSpec<String>(defaultBucket, new PageToFile());
		final DefaultQueueInputSpec<FilePath> claimCheckSpec = new DefaultQueueInputSpec<FilePath>(queue, new FilePathToMessageFunction());

		/***
		 * Make 3 Http Requests in parallel, asynchronously. 
		 * For each http Response, save the results to the File Store implementation
		 * For each Result File Saved, Submit a message to the queue implementation with a pointer to the file on disk
		 * 
		 * web().get("google.com")
		 * .into(blob().save(fileSpec.saveInputSpec("pages/google.html"))
		 * 			.into(queue().publish(claimCheckSpec)))
		 * 
		 */
		app.queues().consume(queue, 1, new OutputMessageHandler());
		
		final CommandResult<String> result = 
				commands.get(GOOGLE)
					.into(commands.saveFile(blobSpec.path("pages/google.html?" + UUID.randomUUID().toString()))
							.into(commands.produce(claimCheckSpec))
			)
			.queue();
			
//		final CommandResult<String> result = 
//			commands.get(GOOGLE).into(commands.saveFile(blobSpec.path("pages/google.html")).into(commands.produce(claimCheckSpec)))
//			.inParallel(commands.get(CNN).into(commands.saveFile(blobSpec.path("pages/cnn.html")).into(commands.produce(claimCheckSpec))))
//			.inParallel(commands.get(LINKED_IN).into(commands.saveFile(blobSpec.path("pages/linkedin.html")).into(commands.produce(claimCheckSpec))))
//			.queue();
		
		Thread.sleep(5000);
		
		final List<Object> results = result.allResults();
		LOGGER.info("Commands Executed: {}", results.size());
	}

	
	/***
	 * Execute In parallel
	 * @param commands
	 */
	public static void executeGenieContainer1(final ApplicationFactory app, final Properties properties) {

		
		final ApplicationCommandFactory commands = app.commands();
		
		/** GOOGLE **/
		GenieInputCommand<String, FilePath> saveGoogleInput = commands.saveFile(FilePath.as(properties.getProperty(ExampleConfig.BUCKET_PROPERTY), "pages/google.html"), new PageToFile());
		GenieInputCommand<FilePath, String> queueGoogleInput = commands.produce("FileSavedQueue",new FilePathToMessageFunction());
		saveGoogleInput.into(queueGoogleInput);

		
		/** CNN **/
		SaveFileInputCommand<String> saveCnnInput = commands.saveFile(FilePath.as(properties.getProperty(ExampleConfig.BUCKET_PROPERTY), "pages/cnn.html"),new PageToFile());
		ProduceInputCommand<FilePath> producerCnnInput = commands.produce("FileSavedQueue", new FilePathToMessageFunction());
		saveCnnInput.into(producerCnnInput);

		
		/** LinkedIn **/
		SaveFileInputCommand<String> saveLinkedInInput = commands.saveFile(FilePath.as(properties.getProperty(ExampleConfig.BUCKET_PROPERTY), "pages/linkedin.html"),new PageToFile());
		ProduceInputCommand<FilePath> producerLinkedInInput = commands.produce("FileSavedQueue", new FilePathToMessageFunction());
		saveLinkedInInput.into(producerLinkedInInput);

		
		/** execute all tasks **/
		final CommandResult<String> result = 
				commands.get(GOOGLE).into(saveGoogleInput)
				.inParallel(commands.get(CNN).into(saveCnnInput))
				.inParallel(commands.get(LINKED_IN).into(saveLinkedInInput))
				.queue();

		
		final List<Object> results = result.allResults();
		System.out.println(results.size());
	}

	
	
	
	/***
	 * A function to convert a page to a File
	 * 
	 * @author shawn
	 *
	 */
	public static class PageToFile implements ToFileFunction<String> {
		@Override
		public FileContent run(Input2<String, FilePath> input) {
			byte[] bytes = input.getA().getBytes();
			return FileContent.create(input.getB(), bytes.length,
					new ByteArrayInputStream(bytes));
		}
	}

	/**
	 * Example Function that converts 3 input parameters into a queue message.
	 * The input parameters are: a: String - QueueName b: String - MessageId c:
	 * FilePath - a FilePath - Calling filePath.toString() converts the FilePath
	 * to the format of "s3://mybucket/path/to/file.txt"
	 * 
	 * @author shawn
	 */
	public static class FilePathToMessageFunction implements
			ToMessageFunction<FilePath> {
		@Override
		public Message run(final Input3<String, String, FilePath> input) {
			return new Message() {
				@Override
				public String getQueue() {
					return input.getA();
				}
				@Override
				public String getId() {
					return input.getB();
				}
				@Override
				public String getBody() {
					return input.getC().toString();
				}
				@Override
				public Map<String, String> getHeaders() {
					return new HashMap<String, String>();
				}
			};
		}
	}

	
	
	public static class OutputMessageHandler implements MessageHandler{
		@Override
		public void handle(Message message) {
			LOGGER.info("Received messageId: {} messageBody:{}", message.getId(), message.getBody());
		}
		@Override
		public void handleBatch(List<Message> messages) {
			for(Message message : messages){
				handle(message);
			}
		}}
	
	
	
	/**
	 * Initialize URLs for examples
	 */
	private static void initUrls() {
		try {
			CNN = new URL("http://www.cnn.com");
			LINKED_IN = new URL("http://www.linkedin.com");
			GOOGLE = new URL("http://www.google.com");
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
