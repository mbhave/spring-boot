package org.springframework.boot.endpoint.web;

import java.util.Set;

/**
 * @author Madhura Bhave
 */
public class WebOperationSecurityInterceptor {

	private final Set<String> roles;

	public Set<String> getRoles() {
		return this.roles;
	}

	public WebOperationSecurityInterceptor(Set<String> roles) {
		this.roles = roles;
	}

	public SecurityResponse handle(RoleVerifier verifier) {
		if (this.roles.contains(SecurityConfiguration.ROLE_ANONYMOUS)) {
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

	public enum SecurityResponse {

		SUCCESS("", 200),

		UNAUTHORIZED("Full authentication is required to access this resource", 401),

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
