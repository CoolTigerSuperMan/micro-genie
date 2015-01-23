//package io.microgenie.service;
//
//import io.dropwizard.Application;
//import io.dropwizard.Bundle;
//import io.dropwizard.ConfiguredBundle;
//import io.dropwizard.cli.Command;
//import io.dropwizard.cli.ConfiguredCommand;
//import io.dropwizard.lifecycle.Managed;
//import io.dropwizard.setup.Bootstrap;
//import io.dropwizard.setup.Environment;
//import io.federecio.dropwizard.swagger.SwaggerDropwizard;
//import io.microgenie.application.ApplicationFactory;
//import io.microgenie.commands.util.CollectionUtil;
//
//import java.lang.reflect.ParameterizedType;
//import java.text.SimpleDateFormat;
//import java.util.Set;
//
//
//
//
///***
// * The application entry point
// * @author shawn
// */
//public abstract class ServiceApplication<T extends AppConfiguration> extends Application<T>{
//
//	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
//	private MicroService<T> service;
//	private final SwaggerDropwizard swaggerDropwizard = new SwaggerDropwizard();
//
//	
//	
//	public abstract MicroService<T> createMicroService();
//
//	
//    @SuppressWarnings("unchecked")	
//	public T createInstance() throws Exception {
//	    final T instance = ((Class<T>)((ParameterizedType)this.getClass()
//	       .getGenericSuperclass())
//	       .getActualTypeArguments()[0])
//	       .newInstance();
//	    return instance;
//	}
//	
//	
//	@Override
//	public void initialize(Bootstrap<T> bootstrap) {
//
//		/** Create the micro service instance **/
//		this.service = this.createMicroService();
//
//		/** Initialize documentation, bundles and commands **/
//	    swaggerDropwizard.onInitialize(bootstrap);
//	    this.addBundles(bootstrap);
//	    this.addConfiguredBundles(bootstrap);
//	    this.addCommands(bootstrap);
//	    this.addConfiguredCommands(bootstrap);
//	}
//	
//	
//
//	/**
//	 * Run the application
//	 */
//	@Override
//	public void run(T configuration, Environment environment) throws Exception {
//
//		environment.getObjectMapper()
//        .setDateFormat(new SimpleDateFormat(ISO_8601_DATE_FORMAT));
//		
//		/** Initialize the application command factory **/
//		final ApplicationFactory appFactory = service.createApplicationFactory(configuration, environment);
//		manageApplicationFactory(appFactory, environment);
//		
//		service.registerHealthChecks(appFactory, configuration, environment);
//		service.registerResources(appFactory, configuration, environment);
//		service.registerSubscribers(appFactory, configuration, environment);
//		service.registerConsumers(appFactory, configuration, environment);
//
//        /** Configure API documentation **/
//        this.swaggerDropwizard.onRun(configuration, environment, configuration.getHost(), configuration.getPort());
//	}
//	
//
//	
//	
//	/***
//	 * Add any bundles found
//	 * @param bootstrap
//	 */
//	private void addBundles(Bootstrap<T> bootstrap) {
//	    final Set<Bundle> bundles = service.createBundles(bootstrap);
//	    if(CollectionUtil.hasElements(bundles)){
//	    	for(Bundle bundle : bundles){
//	    		bootstrap.addBundle(bundle);
//	    	}
//	    }	
//	}
//	
//	/***
//	 * Add any configured bundles found
//	 * @param bootstrap
//	 */
//	private void addConfiguredBundles(Bootstrap<T> bootstrap) {
//	    final  Set<ConfiguredBundle<T>> bundles = service.createConfiguredBundles(bootstrap);
//	    if(CollectionUtil.hasElements(bundles)){
//	    	for(ConfiguredBundle<T> bundle : bundles){
//	    		bootstrap.addBundle(bundle);
//	    	}
//	    }	
//	}
//	
//	
//	/***
//	 * Add any commands found
//	 * @param bootstrap
//	 */
//	private void addCommands(Bootstrap<T> bootstrap) {
//	    final Set<Command> commands = service.createCommands(bootstrap);
//	    if(CollectionUtil.hasElements(commands)){
//	    	for(Command command : commands){
//	    		bootstrap.addCommand(command);
//	    	}
//	    }	
//	}
//	
//	/***
//	 * Add any configured commands found
//	 * @param bootstrap
//	 */
//	private void addConfiguredCommands(Bootstrap<T> bootstrap) {
//	    final  Set<ConfiguredCommand<T>> configuredCommands = service.createConfiguredCommands(bootstrap);
//	    if(CollectionUtil.hasElements(configuredCommands)){
//	    	for(ConfiguredCommand<T> configuredCommand : configuredCommands){
//	    		bootstrap.addCommand(configuredCommand);
//	    	}
//	    }	
//	}
//
//
//	
//	protected void manageApplicationFactory(final ApplicationFactory appFactory, final Environment environment){
//		final Managed managedAppFactory = new Managed() {
//			@Override
//			public void start() throws Exception {
//				appFactory.initialize();
//			}
//			@Override
//			public void stop() throws Exception {
//				appFactory.close();
//			}
//		};
//		environment.lifecycle().manage(managedAppFactory);
//	}
//}
