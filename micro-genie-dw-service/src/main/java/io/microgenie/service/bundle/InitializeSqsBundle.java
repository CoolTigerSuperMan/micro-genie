package io.microgenie.service.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.aws.admin.SqsQueueAdmin;
import io.microgenie.aws.config.SqsConfig;
import io.microgenie.service.AppConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQSClient;


/***
 * Initialize SQS Queues based on configuration
 * @author shawn
 */
public class InitializeSqsBundle implements ConfiguredBundle<AppConfiguration>{

	private static Logger LOGGER = LoggerFactory.getLogger(InitializeSqsBundle.class);

	@Override
	public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
		
		if(configuration!=null && configuration.getAws() !=null && configuration.getAws().getSqs() !=null){
			final AmazonSQSClient client = new AmazonSQSClient();
			try{
					LOGGER.info("Executing configured sqs bundle");
					final SqsQueueAdmin admin = new SqsQueueAdmin(client);
					final SqsConfig sqsConfig = configuration.getAws().getSqs();
					admin.initializeQueues(sqsConfig.getQueues(), sqsConfig.isBlockUntilReady());	
					LOGGER.info("Completed sqs bundle execution");
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
