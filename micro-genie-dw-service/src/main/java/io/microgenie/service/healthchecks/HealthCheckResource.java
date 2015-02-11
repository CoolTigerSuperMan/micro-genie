package io.microgenie.service.healthchecks;

import java.util.Map.Entry;
import java.util.SortedMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry;

@Path(value="health")
public class HealthCheckResource {

	private HealthCheckRegistry health;

	
	public HealthCheckResource(HealthCheckRegistry health){
		this.health = health;
	}
	
	
	/***
	 * Application Health Checks
	 * @return response
	 */
	@Path(value="/check")
	@GET
	@Timed
	public Response check(){
		final SortedMap<String, HealthCheck.Result> results = this.health.runHealthChecks();
		for(Entry<String, Result> entryResult : results.entrySet()){
			if(!entryResult.getValue().isHealthy()){
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(entryResult.getValue().getMessage()).build();
			}
		}
		return Response.status(Status.OK).entity("Healthy!").build();
	}
}
