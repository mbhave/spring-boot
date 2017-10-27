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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.boot.actuate.autoconfigure.cloudfoundry.AccessLevel;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryAuthorizationException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Madhura Bhave
 */
public class ReactiveCloudFoundrySecurityService {

	private final WebClient webClient;

	private final String cloudControllerUrl;

	private String uaaUrl;

	ReactiveCloudFoundrySecurityService(WebClient webClient,
			String cloudControllerUrl, boolean skipSslValidation) {
		Assert.notNull(webClient, "Webclient must not be null");
		Assert.notNull(cloudControllerUrl, "CloudControllerUrl must not be null");
		this.webClient = webClient;
		this.cloudControllerUrl = cloudControllerUrl;
	}

	/**
	 * Return the access level that should be granted to the given token.
	 * @param token the token
	 * @param applicationId the cloud foundry application ID
	 * @return the access level that should be granted
	 * @throws CloudFoundryAuthorizationException if the token is not authorized
	 */
	public Mono<AccessLevel> getAccessLevel(String token, String applicationId)
			throws CloudFoundryAuthorizationException {
		URI uri = getPermissionsUri(applicationId);
		return this.webClient.get().uri(uri)
				.header("Authorization", "bearer" + token)
				.retrieve().bodyToMono(Map.class)
				.flatMap(this::getAccessLevel)
				.doOnError(this::handleCloudControllerError);
	}

	private void handleCloudControllerError(Throwable cause) {
		if (cause instanceof HttpClientErrorException) {
			if (((HttpClientErrorException)cause).getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				throw new CloudFoundryAuthorizationException(CloudFoundryAuthorizationException.Reason.ACCESS_DENIED,
					"Access denied");
			}
			throw new CloudFoundryAuthorizationException(CloudFoundryAuthorizationException.Reason.INVALID_TOKEN,
				"Invalid token", cause);
		}
		if (cause instanceof HttpServerErrorException) {
			throw new CloudFoundryAuthorizationException(CloudFoundryAuthorizationException.Reason.SERVICE_UNAVAILABLE,
				"Cloud controller not reachable");
		}

	}

	private Mono<AccessLevel> getAccessLevel(Map body) {
		if (Boolean.TRUE.equals(body.get("read_sensitive_data"))) {
			return Mono.just(AccessLevel.FULL);
		}
		return Mono.just(AccessLevel.RESTRICTED);
	}

	private URI getPermissionsUri(String applicationId) {
		try {
			return new URI(this.cloudControllerUrl + "/v2/apps/" + applicationId
					+ "/permissions");
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Return all token keys known by the UAA.
	 * @return a list of token keys
	 */
	public Mono<Map<String, String>> fetchTokenKeys() {
		return this.webClient.get()
				.uri(getUaaUrl() + "/token_keys")
				.retrieve().bodyToMono(new ParameterizedTypeReference<Map<String,String>>() {})
				.flatMap((response) -> Mono.just(extractTokenKeys(response)))
				.doOnError((cause -> handleError("UAA not reachable")));
	}

	private void handleError(String message) {
		throw new CloudFoundryAuthorizationException(CloudFoundryAuthorizationException.Reason.SERVICE_UNAVAILABLE,
				message);
	}

	private Map<String, String> extractTokenKeys(Map<?, ?> response) {
		Map<String, String> tokenKeys = new HashMap<>();
		for (Object key : (List<?>) response.get("keys")) {
			Map<?, ?> tokenKey = (Map<?, ?>) key;
			tokenKeys.put((String) tokenKey.get("kid"), (String) tokenKey.get("value"));
		}
		return tokenKeys;
	}

	/**
	 * Return the URL of the UAA.
	 * @return the UAA url
	 */
	public Mono<String> getUaaUrl() {
		if (this.uaaUrl == null) {
			this.webClient
				.get().uri(this.cloudControllerUrl + "/info")
				.retrieve().bodyToMono(Map.class)
				.doOnError((cause) -> handleError("Unable to fetch token keys from UAA"))
				.doOnSuccess(this::setUaaUrl);
		}
		return Mono.just(this.uaaUrl);
	}

	private void setUaaUrl(Map response) {
		this.uaaUrl = (String)response.get("token_endpoint");
	}

}
