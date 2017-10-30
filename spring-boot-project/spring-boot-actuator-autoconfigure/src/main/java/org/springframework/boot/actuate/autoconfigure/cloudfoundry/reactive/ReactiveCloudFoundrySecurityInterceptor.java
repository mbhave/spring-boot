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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryAuthorizationException;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.SecurityResponse;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.Token;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.reactive.CorsUtils;

/**
 * Security interceptor to validate the cloud foundry token that returns reactive types.
 *
 * @author Madhura Bhave
 */
class ReactiveCloudFoundrySecurityInterceptor {

	private static final Log logger = LogFactory
			.getLog(ReactiveCloudFoundrySecurityInterceptor.class);

	private final ReactiveTokenValidator tokenValidator;

	private final ReactiveCloudFoundrySecurityService cloudFoundrySecurityService;

	private final String applicationId;

	private static Mono<SecurityResponse> SUCCESS = Mono.just(SecurityResponse.success());

	ReactiveCloudFoundrySecurityInterceptor(ReactiveTokenValidator tokenValidator,
			ReactiveCloudFoundrySecurityService cloudFoundrySecurityService,
			String applicationId) {
		this.tokenValidator = tokenValidator;
		this.cloudFoundrySecurityService = cloudFoundrySecurityService;
		this.applicationId = applicationId;
	}

	Mono<SecurityResponse> preHandle(ServerHttpRequest request, String endpointId) {
		if (CorsUtils.isPreFlightRequest(request)) {
			return SUCCESS;
		}
		if (!StringUtils.hasText(this.applicationId)) {
			return Mono.error(new CloudFoundryAuthorizationException(
					CloudFoundryAuthorizationException.Reason.SERVICE_UNAVAILABLE,
					"Application id is not available"));
		}
		if (this.cloudFoundrySecurityService == null) {
			return Mono.error(new CloudFoundryAuthorizationException(
					CloudFoundryAuthorizationException.Reason.SERVICE_UNAVAILABLE,
					"Cloud controller URL is not available"));
		}
		if (HttpMethod.OPTIONS.matches(request.getMethodValue())) {
			return SUCCESS;
		}
		return check(request, endpointId)
				.then(SUCCESS);
//		catch (Exception ex) {
//			logger.error(ex);
//			if (ex instanceof CloudFoundryAuthorizationException) {
//				CloudFoundryAuthorizationException cfException = (CloudFoundryAuthorizationException) ex;
//				return Mono.just(new SecurityResponse(cfException.getStatusCode(),
//						"{\"security_error\":\"" + cfException.getMessage() + "\"}"));
//			}
//			return Mono.just(new SecurityResponse(HttpStatus.INTERNAL_SERVER_ERROR,
//					ex.getMessage()));
//		}
	}

	private Mono<Void> check(ServerHttpRequest request, String path) {
		Token token = getToken(request);
		return this.tokenValidator.validate(token).then(this.cloudFoundrySecurityService.getAccessLevel(token.toString(), this.applicationId))
				.filter(accessLevel -> accessLevel.isAccessAllowed(path))
				.switchIfEmpty(Mono.error(new CloudFoundryAuthorizationException(CloudFoundryAuthorizationException.Reason.ACCESS_DENIED,
						"Access denied")))
				.then();
	}

	private Token getToken(ServerHttpRequest request) {
		String authorization = request.getHeaders().getFirst("Authorization");
		String bearerPrefix = "bearer ";
		if (authorization == null
				|| !authorization.toLowerCase().startsWith(bearerPrefix)) {
			throw new CloudFoundryAuthorizationException(
					CloudFoundryAuthorizationException.Reason.MISSING_AUTHORIZATION,
					"Authorization header is missing or invalid");
		}
		return new Token(authorization.substring(bearerPrefix.length()));
	}

}
