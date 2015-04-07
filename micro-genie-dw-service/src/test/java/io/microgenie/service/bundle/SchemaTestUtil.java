package io.microgenie.service.bundle;

import io.microgenie.application.util.CloseableUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;


/****
 * JSON Schema Test Utility methods
 * @author shawn
 *
 */
public class SchemaTestUtil {
	
	

	   
    /***
     * Create JsonSchema and return as an InputStream
     * 
     * @param modelClazz
     * @return jsonSchemaInputStream
     */
    public static InputStream createJsonSchemaAsInputStream(Class<?> modelClazz){
    	final byte[] jsonSchemaBytes = SchemaTestUtil.createJsonSchemaAsByteArray(modelClazz);
    	final InputStream stream = new ByteArrayInputStream(jsonSchemaBytes);
    	return stream;
    }
    
    
    /***
     * Create JsonSchema and return as a byte array
     * @param clazz
     * @return jsonSchemaBytes
     */
    public static byte[] createJsonSchemaAsByteArray(final Class<?> clazz){
    	final String jsonSchema = SchemaTestUtil.createJsonSchemaAsString(clazz);
    	final byte[] schemaBytes = jsonSchema.getBytes(Charsets.UTF_8);
    	return schemaBytes;
    }
    
    
    /***
     * Create JsonSchema and return as a String
     * @param clazz
     * @return jsonSchema
     */
    public static String createJsonSchemaAsString(final Class<?> clazz){
    	final JsonNode node = SchemaTestUtil.createJsonSchemaAsJsonNode(clazz);
    	return node.toString();
    }
    
    /**
     * Create JsonNode from model and return as JsonNode
     * @param clazz
     * @return node
     */
    public static JsonNode createJsonSchemaAsJsonNode(final Class<?> clazz){
		final JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
		final JsonNode schema = v4generator.generateSchema(clazz);
		return schema;
    }
    
    
    
    
    /***
     * Converts a list of {@link InputStream} into a list of string contents
     * NOTE: This will close the streams
     * @param streams
     * @return streamContents
     */
	public static List<String> converToStringList(final InputStream ...streams) {
		return converToStringList(Lists.newArrayList(streams));
	}
    

    
    /***
     * Converts a list of {@link InputStream} into a list of string contents;
     * NOTE: This will close the stream
     * @param streams
     * @return streamContents
     */
	public static List<String> converToStringList(final List<InputStream> streams) {
		final List<String> streamContents = Lists.newArrayList();
		for(final InputStream stream : streams){
			try {
				byte[] bytes = IOUtils.toByteArray(stream);
				streamContents.add(new String(bytes, Charsets.UTF_8));
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(),e);
			}finally{
				CloseableUtil.closeQuietly(stream);
			}
		}
		return streamContents;
	}
	
	

    @Attributes(title="Dog", description="A dog schema")
    @JsonSerializableSchema(schemaType="object")
    public static class Dog{
    	
        @Attributes(required=true, title="Name", description="The dogs name")
        @JsonProperty(defaultValue="fido", index=0, required=true, value="name")
        @JsonPropertyDescription("The dogs name")
    	private String name="fido";
        
        @Attributes(required=true, title="Age", description="How old is the dog?")
        @JsonProperty(defaultValue="30", index=1, required=true, value="age")
        @JsonPropertyDescription("How old is the dog?")
    	private int age;
        @Attributes(required=true, title="Hair Type", description="The dog's hair type", enums={"long","short"})
        @JsonPropertyDescription("The dog's hair type")
    	private List<String> hairType;
        
        @Attributes(required=true,  title="Hair Color", description="The dog's hair color", minItems=1, uniqueItems=true)
        @JsonPropertyDescription("The dog's hair color")
    	private String hairColor;
        
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public String getHairColor() {
			return hairColor;
		}
		public void setHairColor(String hairColor) {
			this.hairColor = hairColor;
		}
    }
    
    
    
    @Attributes(title="Cat", description="A Cat schema")
    public static class Cat{
        
    	@Attributes(required=true, title="name")
        @JsonPropertyDescription("The cats name")
    	private String name = "morris";
    	
    	@Attributes(required=true, title="age")
        @JsonPropertyDescription("The cats age")
    	private int age;
        
    	@Attributes(required=true, title="hairType")
        @JsonPropertyDescription("The cats hair type")
    	private String hairType;
        
    	@Attributes(required=true, title="hairColor")
        @JsonPropertyDescription("The cats hair color")
    	private String hairColor;
        
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public String getHairColor() {
			return hairColor;
		}
		public void setHairColor(String hairColor) {
			this.hairColor = hairColor;
		}
    }
}
