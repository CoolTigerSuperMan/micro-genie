package io.microgenie.service.bundle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Environment;
import io.microgenie.application.blob.FilePath;
import io.microgenie.application.util.CloseableUtil;
import io.microgenie.service.AppConfiguration;
import io.microgenie.service.AppConfiguration.SchemaContracts;
import io.microgenie.service.bundle.SchemaTestUtil.Cat;
import io.microgenie.service.bundle.SchemaTestUtil.Dog;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/***
 * Tests the Dropwizard {@link ConfiguredBundle} implementation for the Json Schema Contract Publisher
 * @author shawn
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PublishJsonSchemaBundleTest {
	
	private final Environment mockEnvironment = mock(Environment.class);
	private final AmazonS3Client mockS3Client = mock(AmazonS3Client.class);
	
	private static final String MOCK_PUBLISH_DRIVE = "my-s3-bucket";
	private static final String MOCK_PUBLISH_PATH = "api/contracts/animals";
	
	private static final String SCAN_PACKAGE = PublishJsonSchemaBundleTest.class.getPackage().getName();
	
	
	/** reset each time during setup **/
	private AppConfiguration appConfig;
	
	
	@Before
    public void setUp() {
        this.appConfig = new AppConfiguration();
    }


    @After
    public void tearDown() {
        reset(mockS3Client);
        reset(mockEnvironment);
    }

    

    /***
     * The bundle for json schema is optional, it should exit cleanly when it is not configured
     * @throws Exception 
     */
    @Test
    public void bundleShouldExitWithoutThrowingExceptionWhenNoConfigurationIsDefined() throws Exception {
    
    	final PublishJsonSchemaBundle bundle = new PublishJsonSchemaBundle();
    	bundle.run(this.appConfig, this.mockEnvironment);
    }

    
    
    /***
     * 
     * When scanning a package, all annotated models should be picked up and have json schema generated
     * and published for them. 
     * 
     * Should Generate Json Schema for supplied models
     * @throws Exception
     */
    @Test
    public void shouldGenerateMultipleSchemasWhenScanningPackageWithTwoModelCandidates() throws Exception {
    	
    	final List<String> expectedSchemas = Lists.newArrayList(SchemaTestUtil.createJsonSchemaAsString(Dog.class), SchemaTestUtil.createJsonSchemaAsString(Cat.class));

		/** mock the s3 putObject call **/
		when(mockS3Client.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());
		
    	/** execute and run with values set in config settings**/
    	final PublishJsonSchemaBundle bundle = new PublishJsonSchemaBundle(mockS3Client);
    	final SchemaContracts contractConfig = new SchemaContracts(MOCK_PUBLISH_DRIVE, MOCK_PUBLISH_PATH, SCAN_PACKAGE);
    	this.appConfig.setSchemaContracts(contractConfig);
    	bundle.run(this.appConfig, this.mockEnvironment);

    	
    	/** Verify with argument captor **/
    	final ArgumentCaptor<PutObjectRequest> putRequestArgumentCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    	verify(mockS3Client, times(expectedSchemas.size())).putObject(putRequestArgumentCaptor.capture());
    	
    	
    	/** Assert that expected and actual json schemas are the same **/
    	final List<PutObjectRequest> allCaputredPutRequests = putRequestArgumentCaptor.getAllValues();
    
    	/** assert that our responses match are expected schema count **/ 
    	assertThat(allCaputredPutRequests).hasSize(expectedSchemas.size());
    	
    	/** convert each InputStream to a String for easy assertions **/ 
    	final List<String> actualSchemas = SchemaTestUtil.converToStringList(
    					allCaputredPutRequests.get(0).getInputStream(), 
    					allCaputredPutRequests.get(1).getInputStream());

    	/** assert our actual schemas match are expected schemas exactly **/
    	assertThat(actualSchemas).hasSameElementsAs(expectedSchemas);	
    	
    }
    
    
    
    /***
     * Should Generate Json Schema for supplied models
     * @throws Exception
     */
    @Test
    public void shouldGenerateJsonSchemaFromSuppliedPathAndModel() throws Exception {

    	final InputStream expectedInputStream = SchemaTestUtil.createJsonSchemaAsInputStream(Dog.class);;
    	InputStream actualInputStream = null;
    	
    	try{
    		
    		/** mock the s3 putObject call **/
    		when(mockS3Client.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());

        	/** Models to generate JsonSchema for **/
        	final Set<Class<?>> models = Sets.newHashSet();
        	models.add(Dog.class);

        	/** execute **/
        	final PublishJsonSchemaBundle bundle = new PublishJsonSchemaBundle(mockS3Client, FilePath.as("my-s3-bucket", "api/contracts/animals"), models);    	
        	bundle.run(this.appConfig, this.mockEnvironment);
        	
        	/** Verify with argument captor **/
        	final ArgumentCaptor<PutObjectRequest> putRequestArgumentCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        	verify(mockS3Client, times(1)).putObject(putRequestArgumentCaptor.capture());

        	/** Assert that expected and actual json schemas are the same **/
        	actualInputStream = putRequestArgumentCaptor.getValue().getInputStream();
        	assertThat(actualInputStream).hasContentEqualTo(expectedInputStream);
	
    	}finally{
    		CloseableUtil.closeQuietly(actualInputStream);
    		CloseableUtil.closeQuietly(actualInputStream);
    	}	
    }
}
