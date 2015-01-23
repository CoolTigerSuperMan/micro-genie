package io.microgenie.service.bundles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.service.AppConfiguration;
import io.microgenie.service.commands.InitializeDynamoDbCommand;
import io.microgenie.service.commands.InitializeKinesisCommand;
import io.microgenie.service.commands.InitializeS3Command;
import io.microgenie.service.commands.InitializeSqsCommand;

public class InitializeAwsCommandBundle implements ConfiguredBundle<AppConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(InitializeAwsCommandBundle.class);
	
	@Override
	public void run(AppConfiguration configuration, Environment environment) throws Exception {
		
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
		
		LOGGER.info("Initializing the AWS Command Bundle.... Adding AWS Initialization commands to Configuration Commands");
		
		bootstrap.addCommand(new InitializeSqsCommand());
		bootstrap.addCommand(new InitializeDynamoDbCommand());
		bootstrap.addCommand(new InitializeKinesisCommand());
		bootstrap.addCommand(new InitializeS3Command());
	}
}
