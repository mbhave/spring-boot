package org.springframework.boot.endpoint.web;

import java.util.Set;

/**
 * @author Madhura Bhave
 */
public class SecurityConfiguration {

	public static final String ROLE_ANONYMOUS = "ANONYMOUS";

	public static final String ROLE_ACTUATOR = "ACTUATOR";

	private final Set<String> roles;

	public SecurityConfiguration(Set<String> roles) {
		this.roles = roles;
	}

	public Set<String> getRoles() {
		return roles;
	}
}
