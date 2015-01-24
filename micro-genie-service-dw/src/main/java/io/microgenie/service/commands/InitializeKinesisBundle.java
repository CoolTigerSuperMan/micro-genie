package io.microgenie.service.commands;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.aws.KinesisConfig;
import io.microgenie.aws.kinesis.KinesisAdmin;
import io.microgenie.service.AppConfiguration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.AmazonKinesisClient;


/***
 * Initialize Kinesis Topics
 * @author shawn
 *
 */
public class InitializeKinesisBundle implements ConfiguredBundle<AppConfiguration>{

	private static Logger LOGGER = LoggerFactory.getLogger(InitializeKinesisBundle.class);

	
	/***
	 * Initialize Kinesis Topics and shards
	 */
	@Override
	public void run(AppConfiguration configuration, Environment environment)throws Exception {
		if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getKinesis()!=null){
			final AmazonKinesisClient client = new AmazonKinesisClient();
			try{
				LOGGER.info("Executing configured kinesis bundle");
				final List<KinesisConfig> kinesisConfigs = configuration.getAws().getKinesis();
				final KinesisAdmin admin = new KinesisAdmin(client);
				for(KinesisConfig kinesisConfig : kinesisConfigs){
					admin.createTopic(kinesisConfig.getTopic(), kinesisConfig.getShards());
				}			
				LOGGER.info("Completed kinesis bundle execution");
			}catch(Exception ex){
				LOGGER.error(ex.getMessage(), ex);
			}finally{
				client.shutdown();
			}
		}
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {} 
}
