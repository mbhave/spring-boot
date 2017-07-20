package org.springframework.boot.endpoint.web;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.endpoint.OperationInvoker;
import org.springframework.util.StringUtils;

/**
 * @author Madhura Bhave
 */
public class SecurityOperationInvoker implements OperationInvoker {

	private final Set<String> roles;

	private final OperationInvoker target;

	public SecurityOperationInvoker(Set<String> roles, OperationInvoker target) {
		this.roles = roles;
		this.target = target;
	}

	@Override
	public Object invoke(Map<String, Object> arguments) {
		SecurityRoleVerifier checker = (SecurityRoleVerifier) arguments.get(SecurityRoleVerifier.ROLE_VERIFIER_KEY);
		boolean hasRole = false;
		for (String role : this.roles) {
			if (checker.isUserInRole(role)) {
				hasRole = true;
				break;
			}
		}
		if (!hasRole) {
			String roles = StringUtils.collectionToDelimitedString(this.roles, " ");
			return new WebEndpointResponse<>("Access is denied. User must have one of the these roles: " + roles, 401);
		}
		return this.target.invoke(arguments);

	}
}
