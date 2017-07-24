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

package org.springframework.boot.endpoint.web.reactive;

import java.util.Arrays;

import org.junit.Test;

import org.springframework.boot.endpoint.web.AbstractWebEndpointIntegrationTests;
import org.springframework.boot.endpoint.web.EndpointSecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.WebAnnotationEndpointDiscoverer;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.reactive.context.ReactiveWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * Integration tests for web endpoints exposed using WebFlux.
 *
 * @author Andy Wilkinson
 */
public class ReactiveWebEndpointIntegrationTests
		extends AbstractWebEndpointIntegrationTests<ReactiveWebServerApplicationContext> {

	public ReactiveWebEndpointIntegrationTests() {
		super(ReactiveConfiguration.class);
	}

	@Test
	public void operationWithSecurityInterceptor() throws Exception {
		load(TestEndpointConfiguration.class, client -> {
			client.get().uri("/test/foo.bar").accept(MediaType.APPLICATION_JSON)
					.exchange().expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		}, SecureReactiveConfiguration.class);
	}

	@Test
	public void responseToOptionsRequestIncludesCorsHeaders() {
		load(TestEndpointConfiguration.class, client -> {
			client.options().uri("/test").accept(MediaType.APPLICATION_JSON)
					.header("Access-Control-Request-Method", "POST")
					.header("Origin", "http://example.com").exchange().expectStatus()
					.isOk().expectHeader()
					.valueEquals("Access-Control-Allow-Origin", "http://example.com")
					.expectHeader()
					.valueEquals("Access-Control-Allow-Methods", "GET,POST");
		});
	}

	@Override
	protected ReactiveWebServerApplicationContext createApplicationContext(
			Class<?>... config) {
		return new ReactiveWebServerApplicationContext(config);
	}

	@Override
	protected int getPort(ReactiveWebServerApplicationContext context) {
		return context.getBean(BaseReactiveConfiguration.class).port;
	}

	@Configuration
	@EnableWebFlux
	@Import(BaseReactiveConfiguration.class)
	static class ReactiveConfiguration {

		@Bean
		public WebEndpointReactiveHandlerMapping webEndpointHandlerMapping(
				WebAnnotationEndpointDiscoverer endpointDiscoverer) {
			CorsConfiguration corsConfiguration = new CorsConfiguration();
			corsConfiguration.setAllowedOrigins(Arrays.asList("http://example.com"));
			corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST"));
			return new WebEndpointReactiveHandlerMapping(
					endpointDiscoverer.discoverEndpoints(), corsConfiguration, null);
		}

	}

	@Configuration
	@EnableWebFlux
	@Import(BaseReactiveConfiguration.class)
	static class SecureReactiveConfiguration {

		@Bean
		public Environment environment() {
			MockEnvironment environment = new MockEnvironment();
			environment.setProperty("endpoints.test.web.roles", "ADMIN");
			return environment;
		}

		@Bean
		public WebEndpointReactiveHandlerMapping webEndpointHandlerMapping(
				WebAnnotationEndpointDiscoverer endpointDiscoverer, Environment environment) {
			CorsConfiguration corsConfiguration = new CorsConfiguration();
			corsConfiguration.setAllowedOrigins(Arrays.asList("http://example.com"));
			corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST"));
			return new WebEndpointReactiveHandlerMapping(
					endpointDiscoverer.discoverEndpoints(), corsConfiguration, new EndpointSecurityConfigurationFactory(environment));
		}

	}

	static class BaseReactiveConfiguration {

		private int port;

		@Bean
		public NettyReactiveWebServerFactory netty() {
			return new NettyReactiveWebServerFactory(0);
		}

		@Bean
		public HttpHandler httpHandler(ApplicationContext applicationContext) {
			return WebHttpHandlerBuilder.applicationContext(applicationContext).build();
		}

		@Bean
		public ApplicationListener<ReactiveWebServerInitializedEvent> serverInitializedListener() {
			return event -> {
				this.port = event.getWebServer().getPort();
			};
		}

	}

}
