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

package org.springframework.boot.endpoint.web.jersey;

import java.util.Collection;
import java.util.HashSet;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.Test;

import org.springframework.boot.endpoint.web.AbstractWebEndpointIntegrationTests;
import org.springframework.boot.endpoint.web.EndpointSecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.WebAnnotationEndpointDiscoverer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;

/**
 * Integration tests for web endpoints exposed using Jersey.
 *
 * @author Andy Wilkinson
 */
public class JerseyWebEndpointIntegrationTests extends
		AbstractWebEndpointIntegrationTests<AnnotationConfigServletWebServerApplicationContext> {

	public JerseyWebEndpointIntegrationTests() {
		super(JerseyConfiguration.class);
	}

	@Test
	public void operationWithSecurityInterceptor() throws Exception {
		load(TestEndpointConfiguration.class, client -> {
			client.get().uri("/test/foo.bar").accept(MediaType.APPLICATION_JSON)
					.exchange().expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		}, SecureJerseyConfiguration.class);
	}

	@Override
	protected AnnotationConfigServletWebServerApplicationContext createApplicationContext(
			Class<?>... config) {
		return new AnnotationConfigServletWebServerApplicationContext(config);
	}

	@Override
	protected int getPort(AnnotationConfigServletWebServerApplicationContext context) {
		return context.getWebServer().getPort();
	}

	@Configuration
	@Import(BaseJerseyConfiguration.class)
	static class JerseyConfiguration {

		@Bean
		public ResourceConfig resourceConfig(
				WebAnnotationEndpointDiscoverer endpointDiscoverer) {
			ResourceConfig resourceConfig = new ResourceConfig();
			Collection<Resource> resources = new JerseyEndpointResourceFactory(null)
					.createEndpointResources(endpointDiscoverer.discoverEndpoints());
			resourceConfig.registerResources(new HashSet<Resource>(resources));
			resourceConfig.register(JacksonFeature.class);
			resourceConfig.register(new ObjectMapperContextResolver(new ObjectMapper()),
					ContextResolver.class);
			return resourceConfig;
		}

	}

	@Configuration
	@Import(BaseJerseyConfiguration.class)
	static class SecureJerseyConfiguration {

		@Bean
		public Environment environment() {
			MockEnvironment environment = new MockEnvironment();
			environment.setProperty("endpoints.test.web.roles", "ADMIN");
			return environment;
		}

		@Bean
		public ResourceConfig resourceConfig(
				WebAnnotationEndpointDiscoverer endpointDiscoverer, Environment environment) {
			ResourceConfig resourceConfig = new ResourceConfig();
			Collection<Resource> resources = new JerseyEndpointResourceFactory(new EndpointSecurityConfigurationFactory(environment))
					.createEndpointResources(endpointDiscoverer.discoverEndpoints());
			resourceConfig.registerResources(new HashSet<Resource>(resources));
			resourceConfig.register(JacksonFeature.class);
			resourceConfig.register(new ObjectMapperContextResolver(new ObjectMapper()),
					ContextResolver.class);
			return resourceConfig;
		}

	}

	@Configuration
	static class BaseJerseyConfiguration {

		@Bean
		public TomcatServletWebServerFactory tomcat() {
			return new TomcatServletWebServerFactory(0);
		}

		@Bean
		public ServletRegistrationBean<ServletContainer> servletContainer(
				ResourceConfig resourceConfig) {
			return new ServletRegistrationBean<ServletContainer>(
					new ServletContainer(resourceConfig), "/*");
		}

	}

	private static final class ObjectMapperContextResolver
			implements ContextResolver<ObjectMapper> {

		private final ObjectMapper objectMapper;

		private ObjectMapperContextResolver(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public ObjectMapper getContext(Class<?> type) {
			return this.objectMapper;
		}

	}

}
