package io.microgenie.service.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQSClient;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.aws.SqsConfig;
import io.microgenie.aws.sqs.SqsQueueAdmin;
import io.microgenie.service.AppConfiguration;
import net.sourceforge.argparse4j.inf.Namespace;


/***
 * Initialize SQS Queues based on configuration
 * @author shawn
 */
public class InitializeSqsCommand extends ConfiguredCommand<AppConfiguration>{

	private static Logger LOGGER = LoggerFactory.getLogger(InitializeSqsCommand.class);
	
	public InitializeSqsCommand() {
		super("sqs", "Initialize sqs command");
	}
	protected InitializeSqsCommand(String name, String description) {
		super(name, description);
	}

	@Override
	protected void run(Bootstrap<AppConfiguration> bootstrap, Namespace namespace, AppConfiguration configuration)throws Exception {
		
		final AmazonSQSClient client = new AmazonSQSClient();
		try{
			if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getSqs()!=null){
				LOGGER.info("Executing configured command: {} - {}", this.getName(), this.getDescription());
				final SqsQueueAdmin admin = new SqsQueueAdmin(client);
				final SqsConfig sqsConfig = configuration.getAws().getSqs();
				admin.initializeQueues(sqsConfig.getQueues(), sqsConfig.isBlockUntilReady());	
				LOGGER.info("Completed command execution for command: {}", this.getName());
			}
		}catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
		}finally{
			client.shutdown();
		}
	}
}
