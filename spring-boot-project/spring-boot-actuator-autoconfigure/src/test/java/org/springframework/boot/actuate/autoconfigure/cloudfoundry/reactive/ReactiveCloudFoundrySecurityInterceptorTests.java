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

package org.springframework.boot.actuate.autoconfigure.cloudfoundry.reactive;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryAuthorizationException;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.AccessLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.Base64Utils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReactiveCloudFoundrySecurityInterceptor}.
 *
 * @author Madhura Bhave
 */
public class ReactiveCloudFoundrySecurityInterceptorTests {

	@Mock
	private ReactiveTokenValidator tokenValidator;

	@Mock
	private ReactiveCloudFoundrySecurityService securityService;

	private ReactiveCloudFoundrySecurityInterceptor interceptor;

	private MockServerHttpRequest request;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.interceptor = new ReactiveCloudFoundrySecurityInterceptor(this.tokenValidator,
				this.securityService, "my-app-id");
	}

	@Test
	public void preHandleWhenRequestIsPreFlightShouldReturnTrue() throws Exception {
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.header(HttpHeaders.ORIGIN, "http://example.com")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK));
	}

	@Test
	public void preHandleWhenTokenIsMissingShouldReturnFalse() throws Exception {
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus())
						.isEqualTo(CloudFoundryAuthorizationException.Reason.MISSING_AUTHORIZATION.getStatus()));
	}

	@Test
	public void preHandleWhenTokenIsNotBearerShouldReturnFalse() throws Exception {
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.header(HttpHeaders.AUTHORIZATION, mockAccessToken())
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus())
						.isEqualTo(CloudFoundryAuthorizationException.Reason.MISSING_AUTHORIZATION.getStatus()));
	}
//
	@Test
	public void preHandleWhenApplicationIdIsNullShouldReturnFalse() throws Exception {
		this.interceptor = new ReactiveCloudFoundrySecurityInterceptor(this.tokenValidator,
				this.securityService, null);
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.header(HttpHeaders.AUTHORIZATION, mockAccessToken())
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus())
						.isEqualTo(CloudFoundryAuthorizationException.Reason.SERVICE_UNAVAILABLE.getStatus()));
	}

	@Test
	public void preHandleWhenCloudFoundrySecurityServiceIsNullShouldReturnFalse()
			throws Exception {
		this.interceptor = new ReactiveCloudFoundrySecurityInterceptor(this.tokenValidator, null,
				"my-app-id");
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.header(HttpHeaders.AUTHORIZATION, mockAccessToken())
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus())
						.isEqualTo(CloudFoundryAuthorizationException.Reason.SERVICE_UNAVAILABLE.getStatus()));
	}

	@Test
	public void preHandleWhenAccessIsNotAllowedShouldReturnFalse() throws Exception {
		BDDMockito.given(this.securityService.getAccessLevel(mockAccessToken(), "my-app-id"))
				.willReturn(Mono.just(AccessLevel.RESTRICTED));
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.header(HttpHeaders.AUTHORIZATION, mockAccessToken())
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus())
						.isEqualTo(CloudFoundryAuthorizationException.Reason.ACCESS_DENIED.getStatus()));
	}

	@Test
	public void preHandleSuccessfulWithFullAccess() throws Exception {
		String accessToken = mockAccessToken();
		BDDMockito.given(this.securityService.getAccessLevel(accessToken, "my-app-id"))
				.willReturn(Mono.just(AccessLevel.FULL));
		MockServerWebExchange request = MockServerWebExchange
				.from(MockServerHttpRequest.options("/a")
						.header(HttpHeaders.AUTHORIZATION, mockAccessToken())
						.build());
		StepVerifier.create(this.interceptor.preHandle(request.getRequest(), "/a"))
				.consumeNextWith(response -> assertThat(response.getStatus())
						.isEqualTo(HttpStatus.OK));
	}
//
//	@Test
//	public void preHandleSuccessfulWithRestrictedAccess() throws Exception {
//		String accessToken = mockAccessToken();
//		this.request.addHeader("Authorization", "Bearer " + accessToken);
//		given(this.securityService.getAccessLevel(accessToken, "my-app-id"))
//				.willReturn(AccessLevel.RESTRICTED);
//		SecurityResponse response = this.interceptor
//				.preHandle(this.request, "info");
//		ArgumentCaptor<Token> tokenArgumentCaptor = ArgumentCaptor.forClass(Token.class);
//		verify(this.tokenValidator).validate(tokenArgumentCaptor.capture());
//		Token token = tokenArgumentCaptor.getValue();
//		assertThat(token.toString()).isEqualTo(accessToken);
//		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
//		assertThat(this.request.getAttribute("cloudFoundryAccessLevel"))
//				.isEqualTo(AccessLevel.RESTRICTED);
//	}

	private String mockAccessToken() {
		return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0b3B0YWwu"
				+ "Y29tIiwiZXhwIjoxNDI2NDIwODAwLCJhd2Vzb21lIjp0cnVlfQ."
				+ Base64Utils.encodeToString("signature".getBytes());
	}

}