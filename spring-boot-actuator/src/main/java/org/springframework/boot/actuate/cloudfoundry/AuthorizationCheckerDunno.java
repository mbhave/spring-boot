package org.springframework.boot.actuate.cloudfoundry;

/**
 * @author Madhura Bhave
 */
public interface AuthorizationCheckerDunno {
	boolean shouldCheck();

	boolean isAuthorized();

}
