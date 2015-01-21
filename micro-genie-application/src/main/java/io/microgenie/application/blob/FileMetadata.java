package io.microgenie.application.blob;

import java.util.Map;

import com.google.common.collect.Maps;


/***
 * Metadata about a file
 * @author shawn
 */
public class FileMetadata {
	
	private final String contentType;
	private final String encoding;
	private final Map<String,String> userAttributes;
	
	/**
	 * 
	 * @param contentType - File Content Type
	 */
	protected FileMetadata(final String contentType) {
		this(contentType, null, null);
	}

	
	/**
	 * 
	 * @param contentType - File Content Type
	 */
	protected FileMetadata(final String contentType, final String encoding, final Map<String, String> userAttributes) {
		this.contentType = contentType;
		this.encoding = encoding;
		if(userAttributes==null){
			this.userAttributes = Maps.newHashMap();	
		}else{
			this.userAttributes = userAttributes;
		}
		
	}


	/**
	 * The Content Type of a {@link FileContent} instance
	 * @return contentType
	 */
	public String getContentType() {
		return this.contentType;
	}

	
	

	/***
	 * Content Encoding
	 * @return encoding
	 */
	public String getEncoding() {
		return encoding;
	}


	

	
	

	/**
	 * Get User defined Attributes for this file
	 * @return userAttributes
	 */
	public Map<String,String> getUserAttributes() {
		return userAttributes;
	}

	
	
	
	/**
	 * 
	 * @param contentType
	 * 	<p>
 	 * The Internet Media Type For more information see
	 * 
	 * <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">
	 * http://www.iana.org/assignments/media-types/media-types.xhtml</a>
	 * 
	 * @return metadata
	 */
	public static FileMetadata create(final String contentType){
		return new FileMetadata(contentType);
	}
	
	
	/**
	 * Creational method
	 * 
	 * @param contentType 
	 * 
 	 *	<p>
 	 * The Internet Media Type For more information see
	 * 
	 * <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">
	 * http://www.iana.org/assignments/media-types/media-types.xhtml</a>
	 * 
	 * 
	 * @param encoding 
	 * 
 	 * <p>
 	 * The HTTP Content-Encoding header, as defined in RFC 2616.
 	 * </p>
 	 * <p>
     * Sets the optional Content-Encoding HTTP header specifying what
     * content encodings have been applied to the object and what decoding
     * mechanisms must be applied in order to obtain the media-type referenced
     * by the Content-Type field.
     * </p>
     * 
     * <p>
     * For more information on how the Content-Encoding HTTP header works, see
     * 
     * <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11">
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11</a>
     * </p>
     *
     *
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11"
     *      >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11</a>
     *
	 * @param userAttributes - User defined key / value pairs associated with a file 
	 * @return metadata
	 */
	public static FileMetadata create(final String contentType, final String encoding, final Map<String, String> userAttributes){
		return new FileMetadata(contentType, encoding, userAttributes);
	}
}
