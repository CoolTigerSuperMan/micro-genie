package io.microgenie.service.commands;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.application.blob.FilePath;
import io.microgenie.service.AppConfiguration;
import io.microgenie.service.AppConfiguration.SchemaContracts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.entity.ContentType;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;



/***
 * A dropwizard bundle that is capable of generating Json Schema and publishing the generated 
 * Json schema to S3
 * 
 * @author shawn
 *
 */
public class PublishJsonSchemaBundle implements ConfiguredBundle<AppConfiguration>{

	
	private static final Logger LOGGER = LoggerFactory.getLogger(PublishJsonSchemaBundle.class);
	
	
	private Set<Class<?>> models;
	private FilePath path;
	
	
	/***
	 * If the parameterless constructor is used then the dropwizard configuration file must contain the 
	 * required element in order to publish.
	 *
	 * The following would be placed in your dropwizard configuration yaml file. Where "drive" is the S3 bucket,
	 * path is the S3 path prefix and scanPackage is a java package or package prefix that will be scanned
	 * for classes annotated appropriately. Any classes returned from the scan will be used to generate JsonSchema 
	 * and the result will be published to the specified s3 location
	 *<pre>
	 * 
 	 *		schemaContracts:
     *			drive: mycompany-schemas   
     *			path: services/myservice/resources
     *			scanPackage: com.mycompany.api.contracts
     *
	 *</pre>
	 */
	public PublishJsonSchemaBundle(){}
	
	
	/***
	 * 
	 * @param path - {@link FilePath} - where drive is the S3 bucket and path is the 
	 * path prefix the schemas will be saved to
	 * 
	 * @param models - The models that should be used to generate JSON schema
	 */
	public PublishJsonSchemaBundle(final FilePath path, final Set<Class<?>> models){
		this.path = path;
		this.models = models;
	}

	

	
	/***
	 * Generates json schema for each of the models registered with this bundle
	 */
	@Override
	public void run(final AppConfiguration configuration, final Environment environment)throws Exception {
		this.initialize(configuration);
		if(this.models!=null && this.path!=null){
			final JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
			v4generator.setAutoPutVersion(true);
			final Set<SchemaModelPair> schemasModelPairs = this.generateSchema(v4generator, this.models);
			this.publish(schemasModelPairs);			
		}else{
			LOGGER.info("Json Schema will not be published, either models or schema publish path were not supplied");
		}
	}
	


	/***
	 * Initialize Json schema publisher
	 * @param configuration
	 */
	private void initialize(final AppConfiguration configuration) {

		final SchemaContracts schemaContracts = configuration.getSchemaContracts();
		if(this.path==null && schemaContracts != null && schemaContracts.getPath() != null){
			this.path = FilePath.as(configuration.getSchemaContracts().getDrive(), configuration.getSchemaContracts().getPath());
		}
		if(this.models==null && !Strings.isNullOrEmpty(schemaContracts.getScanPackage())){
			final Reflections reflections = new Reflections(schemaContracts.getScanPackage());
			final Set<Class<?>> models = reflections.getTypesAnnotatedWith(Attributes.class);
			this.models = models;
		}
	}

	
	/***
	 * Generate Json Schema and create an output set of {@link SchemaModelPair} 
	 * @param v4generator
	 * @param classes
	 * @return schemaModelPairs
	 */
	private Set<SchemaModelPair> generateSchema(final JsonSchemaGenerator v4generator, final Set<Class<?>> classes) {
		final Set<SchemaModelPair> schemaModelPairs = Sets.newHashSet();
		for(Class<?> clazz : classes){
			final JsonNode schema = v4generator.generateSchema(clazz);	
			schemaModelPairs.add(SchemaModelPair.create(clazz, schema));
		}
		return schemaModelPairs;
	}
	
	
	/***
	 * Publish the json schema generated from Class models
	 * @param schemaPairs
	 */
	private void publish(final Set<SchemaModelPair> schemaPairs){
		
		if(schemaPairs!=null && schemaPairs.size()>0){
			final AmazonS3Client s3 = new AmazonS3Client();			
			try{
				
				for(SchemaModelPair pair : schemaPairs){
					final ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentType(ContentType.APPLICATION_JSON.getMimeType());
					final String json = pair.getSchema().toString();
					
					if(!Strings.isNullOrEmpty(json)){
						byte[] bytes = json.getBytes(Charsets.UTF_8);
						try(final InputStream inputStream = new ByteArrayInputStream(bytes)){
							
							final PutObjectRequest putRequest = new PutObjectRequest(
									this.path.getDrive(), 
									this.fixPath(this.path.getPath(), pair.getModel()), inputStream, metadata);
							
							s3.putObject(putRequest);						
						}catch(Exception ex){
							throw new RuntimeException(ex.getMessage(), ex);
						}						
					}
				}
			}finally{
				s3.shutdown();
			}
		}
	}
	
	
	
	
	private String fixPath(final String path, final Class<?> model) {
		if(model !=null && !Strings.isNullOrEmpty(path)){
			final String schemaPath = FilenameUtils.concat(path, model.getSimpleName().concat(".json"));
			return schemaPath;
		}
		throw new RuntimeException("Unable to create path for json schema model since model and/or path are null");
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {}
	
	/**
	 * SchemaModelPair
	 * @author shawn
	 */
	static class SchemaModelPair{
		private final Class<?> model;
		private final JsonNode schema;
		public SchemaModelPair(final Class<?> model, final JsonNode schema){
			this.model = model;
			this.schema = schema;
		}
		public Class<?> getModel() {
			return model;
		}
		public JsonNode getSchema() {
			return schema;
		}
		public static SchemaModelPair create(final Class<?> model, final JsonNode node){
			return new SchemaModelPair(model, node);
		}
	}
}
