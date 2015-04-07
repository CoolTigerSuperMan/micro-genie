package io.microgenie.service;

import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.service.bundle.AwsInitBundle;
import io.microgenie.service.bundle.PublishJsonSchemaBundle;
import io.microgenie.service.healthchecks.HealthCheckResource;

import java.text.SimpleDateFormat;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;




/***
 * Abstract Service Application class. The purpose is to handle initialization, service bootstrapping
 * and other common service tasks while just letting the services themselves define application factory, 
 * register resources, health checks, queue consumers and event subscribers. 
 * 
 * <p>
 * The service parent project handles the ordering by which this happens and delegates to the actual
 * service implementation for specifics via the template method pattern
 *  
 * @author shawn
 *
 */
public abstract class MicroService<T extends AppConfiguration> extends Application<T>{
	
	
	protected abstract ApplicationFactory createApplicationFactory(final T configuration, final Environment environment);
	
	
	
	/**
 	* Register any application health checks
	* 
 	* @param appFactory - {@link ApplicationFactory} - The application factory
	* @param configuration {@link AppConfiguration} - The application configuration
	* @param healthCheckRegistry - {@link HealthCheckRegistry} - The health check registry used to register HealthChecks for this service
	*/
	protected abstract void registerHealthChecks(final ApplicationFactory appFactory, final T configuration, final HealthCheckRegistry healthCheckRegistry);
	
	
	
	/***
	 * Register any custom application metrics
	 * @param appFactory
	 * @param configuration
	 * @param metricRegistry
	 */
	protected abstract void registerMetrics(final ApplicationFactory appFactory, final T configuration, final MetricRegistry metricRegistry);
	
	
	/***
	 * Register any Jersey resources
	 * 
	 * @param appFactory
	 * @param configuration
	 * @param environment
	 */
	protected abstract void registerResources(final ApplicationFactory appFactory, final T configuration, final Environment environment);

	
	/**
	 * Initialize documentation and add bootstrap commands and bundles
	 * @param bootstrap - {@link Bootstrap}
	 */
	@Override
	public synchronized void initialize(Bootstrap<T> bootstrap) {
		bootstrap.addBundle(new AwsInitBundle());
		bootstrap.addBundle(new PublishJsonSchemaBundle());
	    this.bootstrap(bootstrap);
	}
	
	/**
	 * Bootstrap the service by adding commands and bundles
	 * @param bootstrap - {@link Bootstrap}
	 */
	protected abstract void bootstrap(Bootstrap<T> bootstrap);
	
	

	/***
	 * Run the application
	 * @param configuration - {@link AppConfiguration} - application configuration
	 * @param environment - {@link Environment} - dropwizard Environment
	 */
	@Override
	public void run(T configuration, final Environment environment) throws Exception {

		final SimpleDateFormat globalDateFormat = new SimpleDateFormat(configuration.getDateFormat());
		
		/** set object mapper data format pattern **/
		environment.getObjectMapper().setDateFormat(globalDateFormat);

		/** Initialize the application factory **/
		final ApplicationFactory appFactory = this.createApplicationFactory(configuration, environment);
		this.manageApplicationFactory(appFactory, environment);
		
		
		/** register metrics, health checks and jersey resources **/
		this.registerMetrics(appFactory, configuration, environment.metrics());
		this.registerHealthChecks(appFactory, configuration, environment.healthChecks());
		this.registerResources(appFactory, configuration, environment);
		
        /** register the Health Check API endpoint **/
        environment.jersey().register(new HealthCheckResource(environment.healthChecks()));
	}
	


	/***
	 * Manage the {@link ApplicationFactory} in terms of starting up and shutting down resources.
	 * <p>
	 * @param appFactory - The application factory to manage
	 * @param environment - The environment
	 */
	protected void manageApplicationFactory(final ApplicationFactory appFactory, final Environment environment){
		final Managed managedAppFactory = new Managed() {
			@Override
			public void start() throws Exception {
				
			}
			@Override
			public void stop() throws Exception {
				appFactory.close();
			}
		};
		environment.lifecycle().manage(managedAppFactory);
	}
}