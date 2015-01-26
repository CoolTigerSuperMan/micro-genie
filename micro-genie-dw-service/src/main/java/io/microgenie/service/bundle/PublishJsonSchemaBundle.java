package io.microgenie.service.bundle;

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
import com.google.common.base.Preconditions;
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
	private AmazonS3Client s3Client;
	
	private boolean shutdownS3Client = false;
	
	
	
	/***
	 * Using this constructor, the {@link #run(AppConfiguration, Environment)} method relies on the value of the scanPackage
	 * property found in {@link SchemaContracts#getScanPackage()}.
	 * <p>
	 * Any classes annotated with the annotation {@link Attributes} are used to generate JsonSchema from the class definition. Results are then
	 * published to the location found in {@link SchemaContracts#getDrive()} and {@link SchemaContracts#getPath()}. Where drive is the s3 bucket
	 * to publish to and path is the s3 key prefix the generated keys will be saved under when the generated schemas are published to S3
	 *<p>
	 * The configuration item should be placed in your dropwizard configuration yaml file.
	 * 
	 *<pre>
	 * 
	 *		schemaContracts:
	 *			drive: mycompany-schemas   
	 *			path: services/myservice/resources
	 *			scanPackage: com.mycompany.api.contracts
	 *
	 *</pre>
	 * 
	 */
	public PublishJsonSchemaBundle(){
		this(new AmazonS3Client());
		this.shutdownS3Client = true;
	}
	
	
	
	/***
	 * Using this constructor, the {@link #run(AppConfiguration, Environment)} method relies on the value of the scanPackage
	 * property found in {@link SchemaContracts#getScanPackage()}.
	 * <p>
	 * Any classes annotated with the annotation {@link Attributes} are used to generate JsonSchema from the class definition. Results are then
	 * published to the location found in {@link SchemaContracts#getDrive()} and {@link SchemaContracts#getPath()}. Where drive is the s3 bucket
	 * to publish to and path is the s3 key prefix the generated keys will be saved under when the generated schemas are published to S3
	 *<p>
	 * The configuration item should be placed in your dropwizard configuration yaml file.
	 * 
	 *<pre>
	 * 
	 *		schemaContracts:
	 *			drive: mycompany-schemas   
	 *			path: services/myservice/resources
	 *			scanPackage: com.mycompany.api.contracts
	 *
	 *</pre>
	 * 
	 * @param s3Client - {@link AmazonS3Client}
	 */
	public PublishJsonSchemaBundle(final AmazonS3Client s3Client){
		this(Preconditions.checkNotNull(s3Client, "s3Client is required but was found to be null"), null, null);
	}
	
	
	
	
	/***
	 * Generate JsonSchema for the supplied classes. Generated Schema is published to the location found in the
	 * path parameter, {@link FilePath}.
	 *
	 *@param s3Client - An initialized instance of {@link AmazonS3Client}
	 *@param path - {@link FilePath} - path contains the S3 Bucket and the directory 
	 *			location (S3 Bucket Key Prefix) the schemas should be deployed to
	 *@param models - Set<Class<?>> - The Classes that JsonSchema should be generated for.
	 */
	public PublishJsonSchemaBundle(final AmazonS3Client s3Client, final FilePath path, final Set<Class<?>> models){
		this.s3Client = Preconditions.checkNotNull(s3Client, "s3Client is required but was found to be null");
		this.path = path;
		this.models = models;
	}
	

	

	
	/***
	 * Generates json schema for each of the models registered with this bundle
	 */
	@Override
	public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
		
		if(this.models==null && this.path == null && configuration.getSchemaContracts() == null){
			LOGGER.debug("exiting Json Schema Publisher, json schema contract publishing has not been configured");
			return;
		}
		
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
		if(this.models==null && schemaContracts != null && !Strings.isNullOrEmpty(schemaContracts.getScanPackage())){
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
							
							this.s3Client.putObject(putRequest);						
						}catch(Exception ex){
							throw new RuntimeException(ex.getMessage(), ex);
						}						
					}
				}
			}finally{
				/** If we created the S3 client in this instance, shut it down **/
				if(shutdownS3Client){
					this.s3Client.shutdown();	
				}
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
