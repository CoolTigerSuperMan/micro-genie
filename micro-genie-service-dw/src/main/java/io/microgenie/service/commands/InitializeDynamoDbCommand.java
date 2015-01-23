package io.microgenie.service.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.aws.DynamoDbConfig;
import io.microgenie.aws.dynamodb.DynamoAdmin;
import io.microgenie.service.AppConfiguration;
import net.sourceforge.argparse4j.inf.Namespace;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


/***
 * Configured Command That initializes dynamodDb
 * @author shawn
 */
public class InitializeDynamoDbCommand extends ConfiguredCommand<AppConfiguration>{ 
	
	private static Logger LOGGER = LoggerFactory.getLogger(InitializeDynamoDbCommand.class);
	
	/***
	 * Constructor
	 */
	public InitializeDynamoDbCommand() {
		this("dynamodb", "Initialize DynamoDb Command");
	}
	protected InitializeDynamoDbCommand(String name, String description) {
		super(name, description);
	}


	/***
	 * Execute DynamoDb configuration according to configuration settings
	 * 
	 * TODO add the 'blockUntil ready setting'
	 */
	@Override
	protected void run(final Bootstrap<AppConfiguration> bootstrap, final Namespace namespace,final AppConfiguration configuration) throws Exception {
		
		final AmazonDynamoDBClient client = new AmazonDynamoDBClient();
		try{
			
			if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getDynamo() !=null){
				
				LOGGER.info("Executing command: {} - {}", this.getName(), this.getDescription());
				final DynamoAdmin admin = new DynamoAdmin(client);
				final DynamoDbConfig dynamodbConfig = configuration.getAws().getDynamo();
				admin.scan(dynamodbConfig.getPackagePrefix());	
				LOGGER.info("Completed command execution for command: {}", this.getName());
			}
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
		}finally{
			client.shutdown();
		}
	}
}