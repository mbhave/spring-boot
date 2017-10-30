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

import java.util.Arrays;

import org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.TokenValidator;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.endpoint.web.EndpointMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Madhura Bhave
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class CloudFoundryReactiveConfiguration {

	@Bean
	public CloudFoundryWebFluxEndpointHandlerMapping webEndpointReactiveHandlerMapping(
			EndpointProvider<WebEndpointOperation> provider,
			EndpointMediaTypes endpointMediaTypes,
			WebClient.Builder webClientBuilder,
			Environment environment,
			WebEndpointProperties webEndpointProperties) {
		return new CloudFoundryWebFluxEndpointHandlerMapping(
				new EndpointMapping(webEndpointProperties.getBasePath()),
				provider.getEndpoints(), endpointMediaTypes, getCorsConfiguration(), getSecurityInterceptor(webClientBuilder, environment));
	}

	private ReactiveCloudFoundrySecurityInterceptor getSecurityInterceptor(
			WebClient.Builder restTemplateBuilder, Environment environment) {
		ReactiveCloudFoundrySecurityService cloudfoundrySecurityService = getCloudFoundrySecurityService(
				restTemplateBuilder, environment);
		ReactiveTokenValidator tokenValidator = new ReactiveTokenValidator(
				cloudfoundrySecurityService);
		return new ReactiveCloudFoundrySecurityInterceptor(tokenValidator,
				cloudfoundrySecurityService,
				environment.getProperty("vcap.application.application_id"));
	}

	private ReactiveCloudFoundrySecurityService getCloudFoundrySecurityService(
			WebClient.Builder webClientBuilder, Environment environment) {
		String cloudControllerUrl = environment
				.getProperty("vcap.application.cf_api");
		boolean skipSslValidation = environment.getProperty(
				"management.cloudfoundry.skip-ssl-validation", Boolean.class, false);
		return (cloudControllerUrl == null ? null
				: new ReactiveCloudFoundrySecurityService(webClientBuilder,
				cloudControllerUrl, skipSslValidation));
	}

	private CorsConfiguration getCorsConfiguration() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin(CorsConfiguration.ALL);
		corsConfiguration.setAllowedMethods(
				Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name()));
		corsConfiguration.setAllowedHeaders(
				Arrays.asList("Authorization", "X-Cf-App-Instance", "Content-Type"));
		return corsConfiguration;
	}

}

