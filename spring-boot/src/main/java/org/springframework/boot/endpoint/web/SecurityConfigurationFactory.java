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
 * @author Madhura Bhave
 */
public class SecurityConfigurationFactory implements Function<String, SecurityConfiguration> {

	private final Environment environment;

	private static final Set<String> DEFAULT_INSECURE_ENDPOINTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("status")));

	/**
	 * Create a new instance with the {@link Environment} to use.
	 * @param environment the environment
	 */
	public SecurityConfigurationFactory(Environment environment) {
		this.environment = environment;
	}

	@Override
	public SecurityConfiguration apply(String endpointId) {
		String key = String.format("endpoints.%s.web.roles", endpointId);
		Set<String> roles = new Binder(ConfigurationPropertySources.get(this.environment)).bind(key, Bindable.setOf(String.class)).orElse(Collections.singleton(getDefaultRole(endpointId)));
		return new SecurityConfiguration(roles);
	}

	private String getDefaultRole(String endpointId) {
		if (DEFAULT_INSECURE_ENDPOINTS.contains(endpointId)) {
			return SecurityConfiguration.ROLE_ANONYMOUS;
		}
		return SecurityConfiguration.ROLE_ACTUATOR;
	}
}
