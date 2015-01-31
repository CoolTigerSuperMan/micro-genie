package io.microgenie.aws.admin;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Region;

/**
 * Amazon S3 Admin Utilities. Used to create buckets
 * @author shawn
 */
public class S3Admin {
	
	private final AmazonS3Client client;

	public S3Admin(final AmazonS3Client client){
		this.client = client;
	}
	

	/**
	 * Create an amazon bucket. This defaults to US_STANDARD regions
	 * @param bucket - The s3 bucket name
	 */
	public void createBucket(final String bucket){
		this.createBucket(bucket, Region.US_Standard, null);
	}	
	
	
	/**
	 * Create an amazon bucket. This defaults to US_STANDARD region
	 *  with the specified canned access control list.
	 * 
	 * @param bucket - The s3 bucket name
	 * @param cannedACL - Canned Access Control List
	 */
	public void createBucket(final String bucket, final CannedAccessControlList cannedACL){
		this.createBucket(bucket, Region.US_Standard, cannedACL, null);
	}
	
	/**
	 * Create an amazon bucket within the given region with the specified canned access control list
	 * 
	 * @param bucket - The s3 bucket name
	 * @param region
	 * @param cannedACL - Canned Access Control List
	 */
	public void createBucket(final String bucket, final Region region, final CannedAccessControlList cannedACL){
		this.createBucket(bucket, region, cannedACL, null);
	}	
	
	/**
	 * Create an amazon bucket. This defaults to US_STANDARD regions
	 * 
	 * @param bucket - The s3 bucket name
	 * @param accessControlList
	 */
	public void createBucket(final String bucket, final AccessControlList accessControlList){
		this.createBucket(bucket, Region.US_Standard, null, accessControlList);
	}	
	
	
	/**
	 * Create an amazon bucket in the specified region
	 * @param bucket - The s3 bucket name
	 * @param region - The S3 region the bucket should be created in
	 * @param accessList - The access control list settings for the bucket
	 */
	public void createBucket(final String bucket, 
				final Region region, 
				final CannedAccessControlList cannedACL, 
				final AccessControlList accessList){

		
		final CreateBucketRequest request = new CreateBucketRequest(bucket, region);
		if(cannedACL!=null){
			request.withCannedAcl(cannedACL);
		}
		if(accessList!=null){
			request.withAccessControlList(accessList);	
		}
		this.client.createBucket(request);
	}	
}
