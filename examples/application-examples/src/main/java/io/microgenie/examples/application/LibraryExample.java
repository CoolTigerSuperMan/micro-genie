package io.microgenie.examples.application;

import io.microgenie.application.ApplicationFactory;
import io.microgenie.application.StateChangeConfiguration;
import io.microgenie.application.database.DatabaseFactory;
import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventFactory;
import io.microgenie.application.events.StateChangePublisher;
import io.microgenie.application.events.Subscriber;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.admin.AwsAdmin;
import io.microgenie.aws.config.AwsConfig;
import io.microgenie.aws.config.AwsConfig.AwsConfigBuilder;
import io.microgenie.aws.config.DynamoDbConfig;
import io.microgenie.aws.config.KinesisConfig;
import io.microgenie.aws.dynamodb.DynamoMapperRepository;
import io.microgenie.examples.ExampleConfig;
import io.microgenie.examples.application.EventHandlers.CheckoutBookRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;



/***
 * An Example That uses micro-genie to demonstrate a library book checkout system
 * @author shawn
 */
public class LibraryExample {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryExample.class);
	
	private static final TypeReference<Map<String,Object>> TYPE_REFERENCE_MAP = new TypeReference<Map<String,Object>>() {};
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final AwsAdmin AWS_ADMIN = new AwsAdmin();
	
	
	/** Libraries **/
	private static final String NORTH_GOTHAM_LIBRARY = "North Gotham";
	private static final String SOUTH_GOTHAM_LIBRARY = "South Gotham";
	private static final String EAST_GOTHAM_LIBRARY = "East Gotham";
	private static final String WEST_GOTHAM_LIBRARY = "West Gotham";
	
	
	/** Published Book ISBNs **/
	private static final String OLD_MAN_IN_SEA_ISBN = "978-0684801223";
	private static final String TOM_SAWYER_ISBN = "978-1402712166";
	private static final String OF_MICE_AND_MEN_ISBN = "978-0749717100";

	
	/** Number of copies for each library **/
	private static final int NUMBER_OF_BOOK_COPIES = 5;
	
	
	/** User to check book out **/
	private static final String USER = "shagwood";
	
	

	
	
	
	/****
	 * Execute example database calls using the dynamodb database factory
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	public static void main(String[] args ) throws IOException, InterruptedException, ExecutionException, TimeoutException{

		final AwsConfig aws = LibraryExample.buildConfig();
		
		/** Initialize Kinesis Streams and dynamo db tables **/
		AWS_ADMIN.createKinesisAdmin(new AmazonKinesisClient()).initialize(aws.getKinesis());
		AWS_ADMIN.createDynamoAdmin(new AmazonDynamoDBClient()).scan(aws.getDynamo().getPackagePrefix(), true, 60);


		final DynamoMapperRepository mapperRepository = DynamoMapperRepository.create(new AmazonDynamoDBClient());
		
		
		try (ApplicationFactory app = new AwsApplicationFactory(aws, ExampleConfig.OBJECT_MAPPER)) {
			
			final EventFactory events = app.events();
			final DatabaseFactory database = app.database();
			
			LOGGER.info("initializing book repository with change publisher. This will create tables if they do not exist");
			
			/** Create the book repository and the book change event publisher **/
			final StateChangePublisher changePublisher = LibraryExample.createChangePublisher(events);
			database.registerRepo(Book.class, new BookRepository(mapperRepository, changePublisher));
			final BookRepository bookRepository = database.repos(Book.class);
			
			
			/** Subscribe to event event streams **/
			LibraryExample.subscribeToEvents(events, bookRepository, EventHandlers.DEFAULT_CLIENT_ID);
			
			
			LOGGER.info("Executing library examples.....");
			
			/** Create the libraries and stock each libraries with copies of our books **/
			final Set<String> libraries = LibraryExample.generateLibraries();
			final Set<Book> publishedBooks = LibraryExample.stockLibraryShelves(libraries, bookRepository, NUMBER_OF_BOOK_COPIES);
			
			/** run queries against our library books **/
			LibraryExample.queryByIsbns(bookRepository, publishedBooks);
			LibraryExample.queryByLibraryId(bookRepository, libraries);
			LibraryExample.queryByLibraryIdAndISBN(bookRepository, libraries, publishedBooks);	
			
			
			/** Check out our favorite book from the North Gotham Library **/
			LibraryExample.checkOutBook(bookRepository, events, NORTH_GOTHAM_LIBRARY, OLD_MAN_IN_SEA_ISBN);

			/** Determine which books a user has checked out **/
			LibraryExample.queryAllLibrariesForBooksCheckedOutByUser(bookRepository, USER);
			
			LOGGER.info("Execution of examples waiting for all events to be consumed");

			Thread.currentThread().join(10000);
		}
		LOGGER.info("Shutdown complete for all resources...exiting now");
	}




	/***
	 * Build configuration for dynamodb tables and kinesis topics
	 * @return awsConfig
	 */
	private static AwsConfig buildConfig() {
		final AwsConfigBuilder configBuilder = new AwsConfigBuilder()
		.withKinesis(
				new KinesisConfig().withTopic(EventHandlers.TOPIC_BOOK_CHANGE_EVENT).withShards(EventHandlers.TOPIC_BOOK_CHANGE_EVENT_SHARDS),
				new KinesisConfig().withTopic(EventHandlers.TOPIC_CHECKOUT_BOOK_REQUEST).withShards(EventHandlers.TOPIC_CHECKOUT_BOOK_REQUEST_SHARDS))
		.withDynamo(new DynamoDbConfig().withScanPackage(Book.class.getPackage().getName()).withBlockingUntilReady(true));
		
		return configBuilder.build();
	}




	/**
	 * createChangePublisher
	 * @param events
	 * @return
	 */
	private static StateChangePublisher createChangePublisher(final EventFactory events) {
		final StateChangeConfiguration changeConfig = LibraryExample.createChangeConfig(events);
		final StateChangePublisher changePublisher = events.createChangePublisher(EventHandlers.DEFAULT_CLIENT_ID, changeConfig);
		return changePublisher;
	}
	

	
	
	/***
	 * Subscribe to events that get notified when a request has been submitted to check a book out
	 * and when a book changes
	 * @param events
	 * @param bookRepository
	 */
	private static void subscribeToEvents(final EventFactory events, final BookRepository bookRepository, final String clientId) {
		
		final Subscriber checkoutRequestSubscription = events.createSubscriber(clientId, EventHandlers.TOPIC_CHECKOUT_BOOK_REQUEST);
		checkoutRequestSubscription.subscribe(new EventHandlers.CheckOutRequestEventHandler(bookRepository));
		
		final Subscriber bookCheckedOutSubscription = events.createSubscriber(clientId, EventHandlers.TOPIC_BOOK_CHANGE_EVENT);
		bookCheckedOutSubscription.subscribe(new EventHandlers.BookChangeEventHandler());
	}


	
	/***
	 * Query the given library for a particular book by ISBN, then check out the first one available
	 * @param repository
	 * @param library
	 * @param isbn
	 * @throws JsonProcessingException 
	 * @throws InterruptedException 
	 */
	private static void checkOutBook(final BookRepository repository, final EventFactory events, final String library, final String isbn) throws JsonProcessingException, InterruptedException {
		final List<Book> books  = repository.getBooksFromLibrary(library, isbn);
		for(Book book : books){
			if(Book.CHECKED_OUT_BY_NOBODY.equals(book.getCheckedOutBy())){
				CheckoutBookRequest checkoutRequest = new EventHandlers.CheckoutBookRequest(book.getBookId(), USER);
				final Map<String, Object> data = MAPPER.convertValue(checkoutRequest, TYPE_REFERENCE_MAP);				
				final Event event = Event.create(EventHandlers.TOPIC_CHECKOUT_BOOK_REQUEST, isbn, data);
				events.publish(EventHandlers.DEFAULT_CLIENT_ID, event);
				return;
			}
		}
	}

	
	
	
	/***
	 * Create the State Change event Configuration
	 * @param events
	 * @return stateChangeConfig
	 */
	private static StateChangeConfiguration createChangeConfig(final EventFactory events){
		
		final Map<String, Map<String, String>> stateChangeActions = new HashMap<String, Map<String, String>>();

		/** Book Changed Actions all get published to the BookChanged topic **/
		final Map<String, String> bookChangedActions = Maps.newHashMap();
		bookChangedActions .put("Created", EventHandlers.TOPIC_BOOK_CHANGE_EVENT);
		bookChangedActions .put("Updated", EventHandlers.TOPIC_BOOK_CHANGE_EVENT);
		bookChangedActions .put("Deleted", EventHandlers.TOPIC_BOOK_CHANGE_EVENT);
		stateChangeActions.put(Book.class.getName(), bookChangedActions);
		
		final StateChangeConfiguration changeConfig = new StateChangeConfiguration();
		changeConfig.setEvents(stateChangeActions);
		return changeConfig;
	}


	
	

	private static Set<String> generateLibraries() {
		final Set<String> libraries = Sets.newHashSet();
		libraries.add(NORTH_GOTHAM_LIBRARY);
		libraries.add(SOUTH_GOTHAM_LIBRARY);
		libraries.add(EAST_GOTHAM_LIBRARY);
		libraries.add(WEST_GOTHAM_LIBRARY);
		return libraries;
	}



	
	
	
	/**
	 * Query All Libraries and find out how many copies of each book they have
	 * @param bookRepository
	 * @param libraries
	 * @param books
	 */
	private static void queryByLibraryIdAndISBN(final BookRepository bookRepository, final Set<String> libraries, final Set<Book> books) {
		
		LOGGER.info("******** Querying all libraries for number of copies of  each book ******** ");
		for(final String library : libraries){
			for(Book book : books){
				LibraryExample.queryLibraryForBookCopies(bookRepository, library, book.getIsbn());
			}
		}
	}




	/***
	 * Query A library for a book and determine number of copies
	 * @param bookRepository
	 * @param library
	 * @param isbn
	 */
	private static void queryLibraryForBookCopies(final BookRepository bookRepository, final String library, final String isbn) {
		final List<Book> books = bookRepository.getBooksFromLibrary(library, isbn);
		LOGGER.info("Library: {} - ISBN: {} contains {} copies", library, isbn, books.size());
		for(Book book : books){
			LOGGER.info("Library: {} - ISBN: {} -- Book: {}", book.getLibraryId(), book.getIsbn(), book.toString());
		}
		
	}




	private static void queryByLibraryId(BookRepository bookRepository, final Set<String> libraries) {
		LOGGER.info("******** Querying each Library for all books ********");
		for(String library : libraries){
			LibraryExample.queryLibrary(bookRepository,library);	
		}
	}



	
	
	/***
	 * Dump All books in the given library
	 * @param repository
	 * @param library
	 */
	private static void queryLibrary(BookRepository repository, String library) {
		LOGGER.info("Querying Books in LibraryId: {}", library);
		final List<Book> books = repository.getBooksFromLibrary(library, null);
		if(books!=null){
			LOGGER.info("======  found {} records: {}=====", books.size());
			for(Book book : books){
				LOGGER.info("LibraryId: {} has book: {}", library, book.toString());
			}			
		}
	}



	

	/****
	 * Query all ISBNs
	 * @param repository
	 * @param books
	 */
	private static void queryByIsbns(final BookRepository repository, final Set<Book> books){
		
		/** Query books by ISBN **/
		LOGGER.info("Querying books by isbn");
		for(Book book : books){
			queryByIsbn(repository, book.getIsbn());	
		}
		LOGGER.info("ISBN queries complete");
	}
	
	
	
	
	/***
	 * Query the libraries for a specific book by a single ISBN
	 * @param repository
	 * @param isbn
	 */
	private static void queryByIsbn(final BookRepository repository, final String isbn){
		LOGGER.info("Querying Books by ISBN: {}", isbn);
		final List<Book> books = repository.getBooksByIsbn(isbn);
		if(books!=null){
			LOGGER.info("======  found {} records: {}=====", books.size());
			for(Book book : books){
				LOGGER.info("found book: {}", book.toString());
			}			
		}
	}
	
	
	
	/***
	 * Query Libraries to determine which books a user has checked out
	 * @param bookRepository
	 * @param user
	 */
	private static void queryAllLibrariesForBooksCheckedOutByUser(final BookRepository bookRepository, final String user){
		final List<Book> books = bookRepository.getBooksCheckedOutByUser(user);
		for(Book book : books){
			LOGGER.info("Book ISBN: {} is checked out by user: {} - BookId: {} - LibraryId: {}", book.getIsbn(), book.getCheckedOutBy(), book.getBookId(), book.getLibraryId());
		}
	}



	/***
	 * Create Books and publish book copies to each libraries
	 * 
	 * @param database - The database
	 * @param bookCountPerLibrary - How many copies we should make of each book
	 */
	private static Set<Book> stockLibraryShelves(final Set<String> libraries, final BookRepository bookRepository, int bookCountPerLibrary) {
		
		final Set<Book> books = Sets.newHashSet();
		
		/** Generate 3 Books for the example **/
		
		/** The old man and the sea **/
		final Book oldManInSea = new Book();
		oldManInSea.setTitle("The Old Man and the Sea");
		oldManInSea.setDescription("Here Hemingway recasts, in strikingly contemporary style, the classic theme of courage in the face of defeat, of personal triumph won from loss. Written in 1952, this hugely successful novella confirmed his power and presence in the literary world and played a large part in his winning the 1954 Nobel Prize for Literature.");
		oldManInSea.setIsbn(OLD_MAN_IN_SEA_ISBN);
		oldManInSea.setAuthor("Ernest Hemingway");
		books.add(oldManInSea);
		
		/** Of Mice and Men **/
		final Book ofMiceAndMen = new Book();
		ofMiceAndMen.setTitle("Of Mice and Men");
		ofMiceAndMen.setDescription("They are an unlikely pair: George is 'small and quick and dark of face'; Lennie, a man of tremendous size, has the mind of a young child. Yet they have formed a 'family,' clinging together in the face of loneliness and alienation.");
		ofMiceAndMen.setIsbn(OF_MICE_AND_MEN_ISBN);
		ofMiceAndMen.setAuthor("John Steinbeck");
		books.add(ofMiceAndMen);
		
		/** Tom Sawyer **/
		final Book tomSawyer = new Book();
		tomSawyer.setTitle("The Adventures of Tom Sawyer");
		tomSawyer.setDescription("The book is part memoir and part social critique against prevailing attitudes and hypocrisies, but mostly it is a witty and charming story jumping blithely from one adventure to the next, and told in deceptively simple but deftly crafted language. Therefore it is thoroughly enjoyable, even when the subject matter is inconsequential. I loved his description of a rather one-sided contest between a poodle and a 'pinch bug' in the middle of a chruch service - it is a minor event in the book, but absolutely hilarious and a joy to read and re-read. It is quintessential Mark Twain writing at the pinnacle of his wit and style.");
		tomSawyer.setIsbn(TOM_SAWYER_ISBN);
		tomSawyer.setAuthor("Mark Twain");
		books.add(tomSawyer);
		
		
		/** Make copies of the book and publish several copies of each book to all libraries **/
		final List<Book> allBooks = Lists.newArrayList();
		for(Book book : books){
			for(String library : libraries){
				for(int i=0; i<bookCountPerLibrary;i++){
					final Book copy = book.copy();
					copy.setLibraryId(library);
					copy.setBookId(UUID.randomUUID().toString());
					allBooks.add(copy);
				}
			}
		}
		
		LOGGER.info("Saving Book list now with book count of {}", allBooks.size());
		bookRepository.save(allBooks);
		LOGGER.info("Completed saving books");
		return books;
	}
}
