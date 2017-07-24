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

/**
 * Used to verify if there is an authenticated user or if the
 * user has a given role.
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
public interface RoleVerifier {

	/**
	 * Checks if there is an authenticate user.
	 * @return true if there is an authenticated user
	 */
	boolean isAuthenticated();

	/**
	 * Checks if the authenticated user has the given role.
	 * @param role the role to verify
	 * @return true if the user has the role
	 */
	boolean isUserInRole(String role);

}

