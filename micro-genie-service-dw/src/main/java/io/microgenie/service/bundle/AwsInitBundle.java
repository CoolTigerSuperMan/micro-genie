package io.microgenie.service.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.service.AppConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/***
 * Aws Initialization Bundle
 * @author shawn
 *
 */
public class AwsInitBundle implements ConfiguredBundle<AppConfiguration>{

	private static final Logger LOGGER = LoggerFactory.getLogger(AwsInitBundle.class);
	

	public AwsInitBundle() {}

	
	@Override
	public void run(AppConfiguration configuration, Environment environment)throws Exception {
		
		LOGGER.info("attempting to run the aws initialization bundle");
		
		LOGGER.info("attempting to run sqs initialization bundle");
		final InitializeSqsBundle sqs = new InitializeSqsBundle();
		sqs.run(configuration, environment);
		
		LOGGER.info("attempting to run dynamodb initialization bundle");
		final InitializeDynamoDbBundle dynamodb = new InitializeDynamoDbBundle();
		dynamodb.run(configuration, environment);
		
		LOGGER.info("attempting to run kinesis initialization bundle");
		final InitializeKinesisBundle kinesis = new InitializeKinesisBundle();
		kinesis.run(configuration, environment);
		
		LOGGER.info("attempting to run s3 initialization bundle");
		final InitializeS3Bundle s3 = new InitializeS3Bundle();
		s3.run(configuration, environment);
		
		LOGGER.info("aws initialization bundle complete");
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {

	}		
}
