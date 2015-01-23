package io.microgenie.service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.wordnik.swagger.annotations.ApiModel;


/**
 * Base Resource containing Common helpers for Resources
 * @author shawn
 */
public abstract class BaseResource {

	
	/***
	 * Throws Resource not found with custom message
	 * @param obj
	 * @param message
	 * @throws WebApplicationException
	 */
	public void throwNotFoundIfNull(final Object obj, final String message) throws WebApplicationException{
		if(obj==null){
			final ApiError error = new ApiError("Resource Not Found", message);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(error).type(MediaType.APPLICATION_JSON).build());
		}
	}
	
	
	
	@ApiModel(value="Api Error", description="An Api Error Response")
	public static class ApiError{
		private String message;
		private String description;
		public ApiError(){}
		public ApiError(final String message, final String description){
			this.message = message;
			this.description = description;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}
}
