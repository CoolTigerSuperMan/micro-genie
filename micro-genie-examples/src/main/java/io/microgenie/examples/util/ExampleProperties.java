package io.microgenie.examples.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/***
 * Get properties for examples
 * @author shawn
 */
public class ExampleProperties {
	
	
	
	/***
	 * Initialize Properties
	 * @param propertyFileName
	 * @return properties
	 * @throws IOException
	 */
	public Properties initalize(final String propertyFileName) throws IOException{
		final Properties props = this.getProperties(propertyFileName);
		return props;
	}
	
	
	/***
	 * Get example properties for file name
	 * @param propertiesFileName
	 * @return properties
	 * @throws IOException
	 */
	public Properties getProperties(final String propertiesFileName) throws IOException{
		final Properties properties = new Properties();
		try(InputStream stream = getClass().getClassLoader().getResourceAsStream(propertiesFileName)){
			if(stream!=null){
				properties.load(stream);
			}else {
				throw new FileNotFoundException(String.format("Property file %s was not found", propertiesFileName));
			}
			return properties;	
		}
	}
}
