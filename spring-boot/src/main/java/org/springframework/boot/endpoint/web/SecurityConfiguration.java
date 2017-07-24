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

import java.util.Set;

/**
 * Provides the security configuration for an endpoint.
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class SecurityConfiguration {

	/**
	 * The default role which allows anonymous access.
	 */
	public static final String ROLE_ANONYMOUS = "ANONYMOUS";

	/**
	 * The default role for securing actuator endpoints.
	 */
	public static final String ROLE_ACTUATOR = "ACTUATOR";

	private final Set<String> roles;

	public SecurityConfiguration(Set<String> roles) {
		this.roles = roles;
	}

	public Set<String> getRoles() {
		return this.roles;
	}
}
