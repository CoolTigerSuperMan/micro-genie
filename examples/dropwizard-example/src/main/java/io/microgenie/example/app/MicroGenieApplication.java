package io.microgenie.example.app;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.aws.dynamodb.DynamoDbMapperFactory;
import io.microgenie.example.data.BookRepository;
import io.microgenie.example.health.BookServiceHealthCheck;
import io.microgenie.example.models.Book;
import io.microgenie.example.resources.BookResource;
import io.microgenie.service.AppConfiguration;
import io.microgenie.service.MicroService;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;



/***
 * The primary entry point for a dropwizard application
 * @author shawn
 *
 */
public class MicroGenieApplication extends MicroService<AppConfiguration> {
	    
	
	/**
	 * Entry point
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new MicroGenieApplication().run(args);
	}

	
	
	/**
	 * Application Name
	 */
    @Override
    public String getName() {
        return "micro-genie-dropwizard-example";
    }

    
    
    
    /***
     * Perform any bootstrapping
     */
	@Override
	protected void bootstrap(Bootstrap<AppConfiguration> bootstrap) {}

	
	
	
	
	/***
	 * Create the application factory
	 */
	@Override
	protected ApplicationFactory createApplicationFactory(final AppConfiguration configuration, final Environment environment) {
		
		final AwsApplicationFactory appFactory = new AwsApplicationFactory(configuration.getAws(), environment.getObjectMapper());
		final DynamoDbMapperFactory database = appFactory.database();
		
		/** Register any application repositories **/
		database.registerRepo(Book.class, new BookRepository(database.getMapperRepository()));
		return appFactory;
	}
	

	
	
	/***
	 * Register Jersey Rest API resources
	 */
	@Override
	protected void registerResources(final ApplicationFactory appFactory, final AppConfiguration configuration, final Environment environment) {
		
		/** Inject the book repository into the book resources **/
		final BookRepository bookRepository = appFactory.database().repos(Book.class);
		environment.jersey().register(new BookResource(bookRepository));
	}


	
	
	/**
	 * Register Health Checks
	 */
	@Override
	protected void registerHealthChecks(final ApplicationFactory appFactory, final AppConfiguration configuration, final HealthCheckRegistry healthCheckRegistry) {
		healthCheckRegistry.register("bookService-health-check", new BookServiceHealthCheck());
	}
	
	


	/**
	 * Register Custom Metrics
	 */
	@Override
	protected void registerMetrics(final ApplicationFactory appFactory, final AppConfiguration configuration, final MetricRegistry metricRegistry) {
		//No custom metrics to register
	}
}