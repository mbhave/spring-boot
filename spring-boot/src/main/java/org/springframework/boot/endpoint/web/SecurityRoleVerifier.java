package org.springframework.boot.endpoint.web;

/**
 * @author Madhura Bhave
 */
public interface SecurityRoleVerifier {

	String ROLE_VERIFIER_KEY = "ROLE_VERIFIER";

	boolean isUserInRole(String role);

}
