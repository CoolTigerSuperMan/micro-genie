package io.microgenie.service.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.AmazonKinesisClient;

import net.sourceforge.argparse4j.inf.Namespace;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.aws.KinesisConfig;
import io.microgenie.aws.kinesis.KinesisAdmin;
import io.microgenie.service.AppConfiguration;


/***
 * Initialize Kinesis Topics
 * @author shawn
 *
 */
public class InitializeKinesisCommand  extends ConfiguredCommand<AppConfiguration>{
	
	private static Logger LOGGER = LoggerFactory.getLogger(InitializeKinesisCommand.class);
	
	public InitializeKinesisCommand() {
		super("kinesis", "Initialize tinesis topics command");
	}
	protected InitializeKinesisCommand(final String name, final String description) {
		super(name, description);
	}
	@Override
	protected void run(final Bootstrap<AppConfiguration> bootstrap,final Namespace namespace, final AppConfiguration configuration) throws Exception {	
		
		final AmazonKinesisClient client = new AmazonKinesisClient();
		
		try{
			
			if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getKinesis()!=null){
				
				LOGGER.info("Executing command: {} - {}", this.getName(), this.getDescription());
				final List<KinesisConfig> kinesisConfigs = configuration.getAws().getKinesis();
				final KinesisAdmin admin = new KinesisAdmin(client);
				for(KinesisConfig kinesisConfig : kinesisConfigs){
					admin.createTopic(kinesisConfig.getTopic(), kinesisConfig.getShards());
				}			
				LOGGER.info("Completed command execution for command: {}", this.getName());
			}
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
		}finally{
			client.shutdown();
		}
	}
}
