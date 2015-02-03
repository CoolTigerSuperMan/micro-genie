package io.microgenie.service.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.aws.admin.KinesisAdmin;
import io.microgenie.aws.config.KinesisConfig;
import io.microgenie.service.AppConfiguration;

import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


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
				
				final List<ListenableFuture<?>> futures = Lists.newArrayList();
				
				for(KinesisConfig kinesisConfig : kinesisConfigs){
					ListenableFuture<?> future = admin.createTopic(kinesisConfig.getTopic(), kinesisConfig.getShards());
					if(future!=null){
						futures.add(future);
					}
				}			
				final ListenableFuture<List<Object>> allAsList = Futures.allAsList(futures);
				allAsList.get();
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
