package io.microgenie.service.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.microgenie.aws.admin.S3Admin;
import io.microgenie.aws.config.S3Config;
import io.microgenie.service.AppConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;


/***
 * A command to initialize S3 buckets
 * @author shawn
 * <p>
 * TODO Need to make the bucket creation process more configuration driven. 
 * Currently This only Creates buckets in US Standard Region 
 * and only creates buckets with the {@link CannedAccessControlList} setting 
 * of CannedAccessControlList.BucketOwnerFullControl
 */
public class InitializeS3Bundle implements ConfiguredBundle<AppConfiguration>{

	private static Logger LOGGER = LoggerFactory.getLogger(InitializeS3Bundle.class);

	
	/***
	 * Initialize S3 buckets
	 */
	@Override
	public void run(AppConfiguration configuration, Environment environment)throws Exception {
		
		if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getS3()!=null){
			final AmazonS3Client client = new AmazonS3Client();
			try{
				LOGGER.info("Executing configured s3 bundle");
				final S3Config s3Config = configuration.getAws().getS3();
				final S3Admin admin = new S3Admin(client);
				admin.createBucket(s3Config.getDefaultDrive(), CannedAccessControlList.BucketOwnerFullControl);
				for(String bucket : s3Config.getBuckets()){
					admin.createBucket(bucket, CannedAccessControlList.BucketOwnerFullControl);	
				}				
				LOGGER.info("Completed s3 bundle execution");
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
