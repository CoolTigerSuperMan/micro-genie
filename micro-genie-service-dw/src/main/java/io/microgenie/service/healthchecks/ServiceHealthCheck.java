package io.microgenie.service.healthchecks;

import com.codahale.metrics.health.HealthCheck;


/**
 * Default Application Health Check
 * @author shawn
 */
public class ServiceHealthCheck extends HealthCheck {
	@Override
	protected Result check() throws Exception {
		return Result.healthy();
	}
}
