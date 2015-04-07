package io.microgenie.service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;



/**
 * Base Rest resource providing helper methods common to rest API endpoints
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
	
	
	/***
	 * 
	 * @param obj
	 * @param message
	 * @throws WebApplicationException
	 */
	public void throwBadRequest(final String message, final String description) throws WebApplicationException{
		final ApiError error = new ApiError(message, description);
		throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(error).type(MediaType.APPLICATION_JSON).build());
	}
	
	

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
