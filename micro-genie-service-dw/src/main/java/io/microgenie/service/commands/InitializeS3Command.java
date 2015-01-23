package io.microgenie.service.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import net.sourceforge.argparse4j.inf.Namespace;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.microgenie.aws.S3Config;
import io.microgenie.aws.s3.S3Admin;
import io.microgenie.service.AppConfiguration;


/***
 * A command to initialize S3 buckets
 * @author shawn
 * <p>
 * TODO Need to make the bucket creation process more configuration driven. 
 * Currently This only Creates buckets in US Standard Region 
 * and only creates buckets with the {@link CannedAccessControlList} setting 
 * of CannedAccessControlList.BucketOwnerFullControl
 */
public class InitializeS3Command  extends ConfiguredCommand<AppConfiguration>{

	private static Logger LOGGER = LoggerFactory.getLogger(InitializeS3Command.class);
	
	public InitializeS3Command() {
		super("s3", "Initalize S3 Buckets Command");
	}
	protected InitializeS3Command(String name, String description) {
		super(name, description);
	}

	@Override
	protected void run(Bootstrap<AppConfiguration> bootstrap, Namespace namespace, AppConfiguration configuration)throws Exception {
		
		if(configuration!=null && configuration.getAws()!=null && configuration.getAws().getS3()!=null){
			
			final AmazonS3Client client = new AmazonS3Client();
			try{
				LOGGER.info("Executing configured command: {} - {}", this.getName(), this.getDescription());
				final S3Config s3Config = configuration.getAws().getS3();
				final S3Admin admin = new S3Admin(client);
				admin.createBucket(s3Config.getDefaultDrive(), CannedAccessControlList.BucketOwnerFullControl);
				for(String bucket : s3Config.getBuckets()){
					admin.createBucket(bucket, CannedAccessControlList.BucketOwnerFullControl);	
				}				
				LOGGER.info("Completed command execution for command: {}", this.getName());
			}catch(Exception ex){
				LOGGER.error(ex.getMessage(), ex);
			}finally{
				client.shutdown();
			}
		}
	}
}
