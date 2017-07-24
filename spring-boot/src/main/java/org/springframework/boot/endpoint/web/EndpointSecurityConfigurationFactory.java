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

package org.springframework.boot.endpoint.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;

/**
 * A {@link SecurityConfiguration} factory that use the {@link Environment} to extract
 * the security settings for each web endpoint.
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class EndpointSecurityConfigurationFactory implements Function<String, SecurityConfiguration> {

	private final Environment environment;

	private static final Set<String> DEFAULT_INSECURE_ENDPOINTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("status")));

	/**
	 * Create a new instance with the {@link Environment} to use.
	 * @param environment the environment
	 */
	public EndpointSecurityConfigurationFactory(Environment environment) {
		this.environment = environment;
	}

	@Override
	public SecurityConfiguration apply(String endpointId) {
		String key = String.format("endpoints.%s.web.roles", endpointId);
		Set<String> roles = new Binder(ConfigurationPropertySources.get(this.environment)).bind(key, Bindable.setOf(String.class)).orElse(Collections.singleton(getDefaultRole(endpointId)));
		return new SecurityConfiguration(roles);
	}

	public Environment getEnvironment() {
		return this.environment;
	}

	private String getDefaultRole(String endpointId) {
		if (DEFAULT_INSECURE_ENDPOINTS.contains(endpointId)) {
			return SecurityConfiguration.ROLE_ANONYMOUS;
		}
		return SecurityConfiguration.ROLE_ACTUATOR;
	}
}
