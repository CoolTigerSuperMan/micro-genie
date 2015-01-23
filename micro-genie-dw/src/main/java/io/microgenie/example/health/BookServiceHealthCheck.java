package io.microgenie.example.health;

import com.codahale.metrics.health.HealthCheck;


/**
 * Default Application Health Check
 * @author shawn
 */
public class BookServiceHealthCheck extends HealthCheck {
	@Override
	protected Result check() throws Exception {
		return Result.healthy();
	}
}
