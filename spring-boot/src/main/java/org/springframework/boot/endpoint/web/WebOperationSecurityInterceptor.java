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

import org.springframework.util.Assert;

/**
 * Security interceptor for Web endpoints.
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class WebOperationSecurityInterceptor {

	private final Set<String> roles;

	public Set<String> getRoles() {
		return this.roles;
	}

	public WebOperationSecurityInterceptor(Set<String> roles) {
		Assert.notNull(roles, "Roles must not be null");
		this.roles = roles;
	}

	/**
	 * Returns a {@link SecurityResponse} depending on whether there is an
	 * authenticated user and the roles for that user.
	 * @param verifier the {@link RoleVerifier} to delegate to
	 * @return the {@link SecurityResponse}
	 */
	public SecurityResponse handle(RoleVerifier verifier) {
		if (this.roles.isEmpty() || this.roles.contains(SecurityConfiguration.ROLE_ANONYMOUS)) {
			return SecurityResponse.SUCCESS;
		}
		if (!verifier.isAuthenticated()) {
			return SecurityResponse.UNAUTHORIZED;
		}
		boolean hasRole = false;
		for (String role : this.roles) {
			if (verifier.isUserInRole(role)) {
				hasRole = true;
				break;
			}
		}
		return (hasRole ? SecurityResponse.SUCCESS : SecurityResponse.FORBIDDEN);
	}

	/**
	 * An enumeration of the different types of responses from the
	 * security interceptor.
	 */
	public enum SecurityResponse {

		/**
		 * Returned when access is granted.
		 */
		SUCCESS("", 200),

		/**
		 * Returned when there is no authenticated user.
		 */
		UNAUTHORIZED("Full authentication is required to access this resource", 401),

		/**
		 * Returned when the user does not have the right roles.
		 */
		FORBIDDEN("Access is denied", 403);

		private final String failureMessage;

		private final int statusCode;

		SecurityResponse(String failureMessage, int statusCode) {
			this.failureMessage = failureMessage;
			this.statusCode = statusCode;
		}

		public String getFailureMessage() {
			return this.failureMessage;
		}

		public int getStatusCode() {
			return this.statusCode;
		}
	}
}
