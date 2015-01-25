package io.microgenie.example.app;

import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.aws.AwsApplicationFactory;
import io.microgenie.example.data.BookRepository;
import io.microgenie.example.health.BookServiceHealthCheck;
import io.microgenie.models.Book;
import io.microgenie.resources.BookResource;
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
        return "micro-genie-dw-example";
    }


	@Override
	protected ApplicationFactory createApplicationFactory(AppConfiguration configuration, Environment environment) {
		return new AwsApplicationFactory(configuration.getAws(), configuration.isUseCommands());
	}


	@Override
	protected void registerHealthChecks(ApplicationFactory appFactory, AppConfiguration configuration, HealthCheckRegistry healthCheckRegistry) {
		healthCheckRegistry.register("bookService-health-check", new BookServiceHealthCheck());
	}


	@Override
	protected void registerMetrics(ApplicationFactory appFactory,AppConfiguration configuration, MetricRegistry metricRegistry) {
		//No custom metrics to register
	}

	@Override
	protected void registerResources(ApplicationFactory appFactory, AppConfiguration configuration, JerseyEnvironment jersey) {
		final BookRepository bookRepository = appFactory.database().repos(Book.class);
		jersey.register(new BookResource(bookRepository));
	}


	@Override
	protected void bootstrap(Bootstrap<AppConfiguration> bootstrap) {
		
	}
}