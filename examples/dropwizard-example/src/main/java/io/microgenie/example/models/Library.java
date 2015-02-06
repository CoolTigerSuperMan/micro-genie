package io.microgenie.example.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;


/***
 * 
 * @author shawn
 *
 */
@DynamoDBTable(tableName="dw-example-library")
public class Library {
	
	private String id;
	private String name;
	private String address;
	private String city;
	private String state;
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
