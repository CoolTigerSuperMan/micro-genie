package io.microgenie.dw;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.microgenie.application.ApplicationFactory;
import io.microgenie.dw.healthchecks.MicroGenieHealthCheck;



/***
 * The primary entry point for a dropwizard application
 * @author shawn
 *
 */
public class MicroGenieApplication extends Application<MicroGenieConfiguration> {
	    
	
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
        return "micro-genie-dw-application";
    }

    
    
    @Override
    public void initialize(final Bootstrap<MicroGenieConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle());
    }
	    
	    
	    

    /***
     * Application Configuration and initialization
     */
    @Override
    public void run(final MicroGenieConfiguration configuration, final Environment environment) throws Exception {
    	final ApplicationFactory app = configuration.getAppFactory();
        environment.healthChecks().register("library-health-check", new MicroGenieHealthCheck());
    }
}