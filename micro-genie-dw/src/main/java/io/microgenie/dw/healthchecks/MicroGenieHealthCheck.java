package io.microgenie.dw.healthchecks;

import com.codahale.metrics.health.HealthCheck;


/**
 * Default Application Health Check
 * @author shawn
 */
public class MicroGenieHealthCheck extends HealthCheck {
	@Override
	protected Result check() throws Exception {
		return Result.healthy();
	}
}
