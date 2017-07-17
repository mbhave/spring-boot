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

package org.springframework.boot.actuate.autoconfigure;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.AuditEventsEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.WebEndpointHandlerMappingCustomizer;
import org.springframework.boot.actuate.endpoint.web.AuditEventsWebEndpointExtension;
import org.springframework.boot.actuate.endpoint.web.HealthWebEndpointExtension;
import org.springframework.boot.actuate.endpoint.web.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Configuration for web-specific endpoint functionality
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
@ManagementContextConfiguration
@EnableConfigurationProperties({ HealthWebEndpointExtensionProperties.class,
		EndpointCorsProperties.class })
public class WebEndpointManagementContextConfiguration {

	private final HealthWebEndpointExtensionProperties healthMvcEndpointProperties;

	public WebEndpointManagementContextConfiguration(
			HealthWebEndpointExtensionProperties healthMvcEndpointProperties,
			ManagementServerProperties managementServerProperties,
			EndpointCorsProperties corsProperties,
			ObjectProvider<List<WebEndpointHandlerMappingCustomizer>> mappingCustomizers) {
		this.healthMvcEndpointProperties = healthMvcEndpointProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint("heapdump")
	public HeapDumpWebEndpoint heapDumpWebEndpoint() {
		return new HeapDumpWebEndpoint();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(HealthEndpoint.class)
	@ConditionalOnEnabledEndpoint("health")
	public HealthWebEndpointExtension healthWebEndpointExtension(HealthEndpoint delegate,
			ManagementServerProperties managementServerProperties) {
		HealthWebEndpointExtension healthMvcEndpoint = new HealthWebEndpointExtension(
				delegate);
		if (this.healthMvcEndpointProperties.getMapping() != null) {
			healthMvcEndpoint
					.addStatusMapping(this.healthMvcEndpointProperties.getMapping());
		}
		return healthMvcEndpoint;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(AuditEventsEndpoint.class)
	public AuditEventsWebEndpointExtension auditEventsWebEndpointExtension(
			AuditEventsEndpoint delegate) {
		return new AuditEventsWebEndpointExtension(delegate);
	}

	@Bean
	@ConditionalOnMissingBean
	@Conditional(LogFileCondition.class)
	public LogFileWebEndpoint logfileWebEndpoint(Environment environment) {
		return new LogFileWebEndpoint(environment);
	}

	private static class LogFileCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			Environment environment = context.getEnvironment();
			String config = environment.resolvePlaceholders("${logging.file:}");
			ConditionMessage.Builder message = ConditionMessage.forCondition("Log File");
			if (StringUtils.hasText(config)) {
				return ConditionOutcome
						.match(message.found("logging.file").items(config));
			}
			config = environment.resolvePlaceholders("${logging.path:}");
			if (StringUtils.hasText(config)) {
				return ConditionOutcome
						.match(message.found("logging.path").items(config));
			}
			config = environment.getProperty("endpoints.logfile.external-file");
			if (StringUtils.hasText(config)) {
				return ConditionOutcome.match(
						message.found("endpoints.logfile.external-file").items(config));
			}
			return ConditionOutcome.noMatch(message.didNotFind("logging file").atAll());
		}

	}

}
