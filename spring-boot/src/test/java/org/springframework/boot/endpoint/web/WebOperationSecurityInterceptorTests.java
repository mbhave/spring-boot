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

import java.util.Collections;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor.SecurityResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebOperationSecurityInterceptor}.
 *
 * @author Madhura Bhave
 */
public class WebOperationSecurityInterceptorTests {

	private WebOperationSecurityInterceptor interceptor;

	private static final Set<String> ROLES = Collections.singleton("ADMIN");

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void interceptorWithNullRolesShouldThrow() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Roles must not be null");
		this.interceptor = new WebOperationSecurityInterceptor(null);
	}

	@Test
	public void interceptorWhenRoleAnonymousShouldReturnSuccess() throws Exception {
		this.interceptor = new WebOperationSecurityInterceptor(Collections.singleton(SecurityConfiguration.ROLE_ANONYMOUS));
		TestRoleVerifier roleVerifier = new TestRoleVerifier(true, true);
		SecurityResponse response = this.interceptor.handle(roleVerifier);
		assertThat(response).isEqualTo(SecurityResponse.SUCCESS);
	}

	@Test
	public void interceptorWhenRolePresentShouldReturnSuccess() throws Exception {
		this.interceptor = new WebOperationSecurityInterceptor(ROLES);
		TestRoleVerifier roleVerifier = new TestRoleVerifier(true, true);
		SecurityResponse response = this.interceptor.handle(roleVerifier);
		assertThat(response).isEqualTo(SecurityResponse.SUCCESS);
	}

	@Test
	public void interceptorWhenRoleMissingShouldReturnForbidden() throws Exception {
		this.interceptor = new WebOperationSecurityInterceptor(ROLES);
		TestRoleVerifier roleVerifier = new TestRoleVerifier(true, false);
		SecurityResponse response = this.interceptor.handle(roleVerifier);
		assertThat(response).isEqualTo(SecurityResponse.FORBIDDEN);
	}

	@Test
	public void interceptorWhenNoAuthenticationShouldReturnUnauthorized() throws Exception {
		this.interceptor = new WebOperationSecurityInterceptor(ROLES);
		TestRoleVerifier roleVerifier = new TestRoleVerifier(false, false);
		SecurityResponse response = this.interceptor.handle(roleVerifier);
		assertThat(response).isEqualTo(SecurityResponse.UNAUTHORIZED);
	}

	@Test
	public void interceptorWhenRolesEmptyShouldReturnSuccess() throws Exception {
		this.interceptor = new WebOperationSecurityInterceptor(Collections.emptySet());
		TestRoleVerifier roleVerifier = new TestRoleVerifier(false, false);
		SecurityResponse response = this.interceptor.handle(roleVerifier);
		assertThat(response).isEqualTo(SecurityResponse.SUCCESS);
	}

	private final class TestRoleVerifier implements RoleVerifier {

		private final boolean authenticated;

		private final boolean isUserInRole;

		private TestRoleVerifier(boolean authenticated, boolean isUserInRole) {
			this.authenticated = authenticated;
			this.isUserInRole = isUserInRole;
		}

		@Override
		public boolean isAuthenticated() {
			return this.authenticated;
		}

		@Override
		public boolean isUserInRole(String role) {
			return this.isUserInRole;
		}
	}

}
