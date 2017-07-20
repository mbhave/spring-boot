/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint;

import liquibase.integration.spring.SpringLiquibase;
import org.flywaydb.core.Flyway;
import org.junit.Test;

import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.endpoint.AuditEventsEndpoint;
import org.springframework.boot.actuate.endpoint.AutoConfigurationReportEndpoint;
import org.springframework.boot.actuate.endpoint.BeansEndpoint;
import org.springframework.boot.actuate.endpoint.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.EnvironmentEndpoint;
import org.springframework.boot.actuate.endpoint.FlywayEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.LiquibaseEndpoint;
import org.springframework.boot.actuate.endpoint.LoggersEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.RequestMappingEndpoint;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.boot.actuate.endpoint.ThreadDumpEndpoint;
import org.springframework.boot.actuate.endpoint.TraceEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.ApplicationContextTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link EndpointAutoConfiguration}.
 *
 * @author Andy Wilkinson
 */
public class EndpointAutoConfigurationTests {

	@Test
	public void environmentEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(EnvironmentEndpoint.class);
	}

	@Test
	public void environmentEndpointCanBeDisabled() {
		endpointCanBeDisabled(EnvironmentEndpoint.class, "env");
	}

	@Test
	public void healthEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(HealthEndpoint.class);
	}

	@Test
	public void healthEndpointCanBeDisabled() {
		endpointCanBeDisabled(HealthEndpoint.class, "health");
	}

	@Test
	public void beansEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(BeansEndpoint.class);
	}

	@Test
	public void beansEndpointCanBeDisabled() {
		endpointCanBeDisabled(BeansEndpoint.class, "beans");
	}

	@Test
	public void infoEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(InfoEndpoint.class);
	}

	@Test
	public void infoEndpointCanBeDisabled() {
		endpointCanBeDisabled(InfoEndpoint.class, "info");
	}

	@Test
	public void loggersEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(LoggersEndpoint.class, LoggingSystemConfiguration.class);
	}

	@Test
	public void loggersEndpointCanBeDisabled() {
		endpointCanBeDisabled(LoggersEndpoint.class, "loggers",
				LoggingSystemConfiguration.class);
	}

	@Test
	public void metricsEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(MetricsEndpoint.class);
	}

	@Test
	public void metricsEndpointCanBeDisabled() {
		endpointCanBeDisabled(MetricsEndpoint.class, "metrics");
	}

	@Test
	public void traceEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(TraceEndpoint.class);
	}

	@Test
	public void traceEndpointCanBeDisabled() {
		endpointCanBeDisabled(TraceEndpoint.class, "trace");
	}

	@Test
	public void threadDumpEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(ThreadDumpEndpoint.class);
	}

	@Test
	public void threadDumpEndpointCanBeDisabled() {
		endpointCanBeDisabled(ThreadDumpEndpoint.class, "threaddump");
	}

	@Test
	public void autoConfigurationReportEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(AutoConfigurationReportEndpoint.class);
	}

	@Test
	public void autoConfigurationReportEndpointCanBeDisabled() {
		endpointCanBeDisabled(AutoConfigurationReportEndpoint.class, "autoconfig");
	}

	@Test
	public void shutdownEndpointIsNotAutoConfiguredByDefault() {
		context().run(
				(context) -> assertThat(context).doesNotHaveBean(ShutdownEndpoint.class));
	}

	@Test
	public void shutdownEndpointCanBeEnabled() {
		context().withPropertyValue("endpoints.shutdown.enabled", "true").run(
				(context) -> assertThat(context.getBeansOfType(ShutdownEndpoint.class))
						.hasSize(1));
	}

	@Test
	public void configurationPropertiesReportEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(ConfigurationPropertiesReportEndpoint.class);
	}

	@Test
	public void configurationPropertiesReportEndpointCanBeDisabled() {
		endpointCanBeDisabled(ConfigurationPropertiesReportEndpoint.class, "configprops");
	}

	@Test
	public void auditEventsEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(AuditEventsEndpoint.class,
				AuditEventRepositoryConfiguration.class);
	}

	@Test
	public void auditEventsEndpointCanBeDisabled() {
		endpointCanBeDisabled(AuditEventsEndpoint.class, "auditevents");
	}

	@Test
	public void flywayEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(FlywayEndpoint.class, FlywayConfiguration.class);
	}

	@Test
	public void flywayEndpointCanBeDisabled() {
		endpointCanBeDisabled(FlywayEndpoint.class, "flyway", FlywayConfiguration.class);
	}

	@Test
	public void liquibaseEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(LiquibaseEndpoint.class, LiquibaseConfiguration.class);
	}

	@Test
	public void liquibaseEndpointCanBeDisabled() {
		endpointCanBeDisabled(LiquibaseEndpoint.class, "liquibase",
				LiquibaseConfiguration.class);
	}

	@Test
	public void requestMappingEndpointIsAutoConfigured() {
		endpointIsAutoConfigured(RequestMappingEndpoint.class);
	}

	@Test
	public void requestMappingEndpointCanBeDisabled() {
		endpointCanBeDisabled(RequestMappingEndpoint.class, "mappings");
	}

	private void endpointIsAutoConfigured(Class<?> endpoint, Class<?>... config) {
		context().withUserConfiguration(config)
				.run((context) -> assertThat(context).hasSingleBean(endpoint));
	}

	private void endpointCanBeDisabled(Class<?> endpoint, String id, Class<?>... config) {
		context().withPropertyValue("endpoints." + id + ".enabled", "false").run(
				(context) -> assertThat(context.getBeansOfType(endpoint)).hasSize(0));
	}

	private ApplicationContextTester context() {
		return new ApplicationContextTester().withConfiguration(
				AutoConfigurations.of(EndpointAutoConfiguration.class));
	}

	@Configuration
	static class LoggingSystemConfiguration {

		@Bean
		public LoggingSystem loggingSystem() {
			return mock(LoggingSystem.class);
		}

	}

	@Configuration
	static class AuditEventRepositoryConfiguration {

		@Bean
		public AuditEventRepository auditEventRepository() {
			return mock(AuditEventRepository.class);
		}

	}

	@Configuration
	static class FlywayConfiguration {

		@Bean
		public Flyway flyway() {
			return mock(Flyway.class);
		}

	}

	@Configuration
	static class LiquibaseConfiguration {

		@Bean
		public SpringLiquibase liquibase() {
			return mock(SpringLiquibase.class);
		}

	}

}
