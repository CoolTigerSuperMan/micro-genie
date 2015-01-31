package io.microgenie.service.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.aws.admin.DynamoAdmin;
import io.microgenie.aws.config.DynamoDbConfig;
import io.microgenie.service.AppConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


/***
 * Configured Command That initializes dynamodDb
 * @author shawn
 */
public class InitializeDynamoDbBundle implements ConfiguredBundle<AppConfiguration>{

	private static Logger LOGGER = LoggerFactory.getLogger(InitializeDynamoDbBundle.class);

	@Override
	public void run(AppConfiguration configuration, Environment environment)throws Exception {
		if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getDynamo() !=null){
			final AmazonDynamoDBClient client = new AmazonDynamoDBClient();
			try{
				LOGGER.info("Executing configured dynamodb bundle");
				final DynamoAdmin admin = new DynamoAdmin(client);
				final DynamoDbConfig dynamodbConfig = configuration.getAws().getDynamo();
				admin.scan(dynamodbConfig.getPackagePrefix());	
				LOGGER.info("Completed dynamodb bundle execution");
			}catch(Exception ex){
				LOGGER.error(ex.getMessage(), ex);
			}finally{
				client.shutdown();
			}
		}
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
		// TODO Auto-generated method stub
		
	}
}