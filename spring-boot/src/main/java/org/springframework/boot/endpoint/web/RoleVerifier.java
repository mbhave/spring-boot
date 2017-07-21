package org.springframework.boot.endpoint.web;

/**
 * @author Madhura Bhave
 */
public interface RoleVerifier {

	boolean isAuthenticated();

	boolean isUserInRole(String role);

}
