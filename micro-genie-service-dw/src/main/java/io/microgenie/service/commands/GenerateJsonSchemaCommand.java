package io.microgenie.service.commands;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.service.AppConfiguration;

import java.util.Set;

import net.sourceforge.argparse4j.inf.Namespace;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
import com.google.common.collect.Sets;



public abstract class GenerateJsonSchemaCommand extends ConfiguredCommand<AppConfiguration> {

	public GenerateJsonSchemaCommand() {
		super("jsonschema", "Generate Json Schema Command");
	}
	
	protected GenerateJsonSchemaCommand(String name, String description) {
		super(name, description);
	}
	
	public abstract Set<Class<?>> getModels();
	public abstract void publish(final Set<JsonNode> schemas);
	
	

	/**
	 * 
	 * Template Method to generate and publish Json Schemas
	 * 
	 * <li>Get Models to derive JsonSchema from</li>
	 * <li>Generate Json Schema</li>
	 * <li>Execute publish method to publish generated Json Schemas</li>
	 * 
	 */
	@Override
	protected void run(Bootstrap<AppConfiguration> bootstrap, Namespace namespace, AppConfiguration configuration) throws Exception {
		final Set<Class<?>> models = this.getModels();
		final JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
		v4generator.setAutoPutVersion(true);
		final Set<JsonNode> schemas = generateSchema(v4generator, models);
		this.publish(schemas);
	}
	


	private Set<JsonNode> generateSchema(final JsonSchemaGenerator v4generator, final Set<Class<?>> classes) {
		final Set<JsonNode> schemas = Sets.newHashSet();
		for(Class<?> clazz : classes){
			final JsonNode schema = v4generator.generateSchema(clazz);	
			schemas.add(schema);
		}
		return schemas;
	}
}
