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

package org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.glassfish.jersey.server.ResourceConfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.boot.endpoint.web.EndpointSecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.WebEndpointOperation;
import org.springframework.boot.endpoint.web.jersey.JerseyEndpointResourceFactory;
import org.springframework.boot.endpoint.web.mvc.WebEndpointServletHandlerMapping;
import org.springframework.boot.endpoint.web.reactive.WebEndpointReactiveHandlerMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Management context configuration for the infrastructure for web endpoints.
 *
 * @author Andy Wilkinson
 */
@ConditionalOnWebApplication
@ManagementContextConfiguration
public class WebEndpointInfrastructureManagementContextConfiguration {

	private final ApplicationContext applicationContext;

	public WebEndpointInfrastructureManagementContextConfiguration(
			ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Bean
	public EndpointSecurityConfigurationFactory securityConfigurationFactory() {
		return new EndpointSecurityConfigurationFactory(this.applicationContext.getEnvironment());
	}

	@Configuration
	@ConditionalOnWebApplication(type = Type.SERVLET)
	@ConditionalOnClass(ResourceConfig.class)
	@ConditionalOnBean(ResourceConfig.class)
	@ConditionalOnMissingBean(type = "org.springframework.web.servlet.DispatcherServlet")
	static class JerseyWebEndpointConfiguration {

		@Bean
		public ResourceConfigCustomizer webEndpointRegistrar(
				EndpointProvider<WebEndpointOperation> provider, EndpointSecurityConfigurationFactory endpointSecurityConfigurationFactory) {
			return resourceConfig -> resourceConfig.registerResources(
					new HashSet<>(new JerseyEndpointResourceFactory(endpointSecurityConfigurationFactory)
							.createEndpointResources(provider.getEndpoints())));
		}

	}

	@Configuration
	@ConditionalOnWebApplication(type = Type.SERVLET)
	@ConditionalOnClass(DispatcherServlet.class)
	@ConditionalOnBean(DispatcherServlet.class)
	static class MvcWebEndpointConfiguration {

		private final List<WebEndpointHandlerMappingCustomizer> mappingCustomizers;

		MvcWebEndpointConfiguration(
				ObjectProvider<List<WebEndpointHandlerMappingCustomizer>> mappingCustomizers) {
			this.mappingCustomizers = mappingCustomizers
					.getIfUnique(Collections::emptyList);
		}

		@Bean
		@ConditionalOnMissingBean
		public WebEndpointServletHandlerMapping webEndpointServletHandlerMapping(
				EndpointProvider<WebEndpointOperation> provider, EndpointSecurityConfigurationFactory endpointSecurityConfigurationFactory) {
			WebEndpointServletHandlerMapping handlerMapping = new WebEndpointServletHandlerMapping(
					provider.getEndpoints(), null, endpointSecurityConfigurationFactory);
			for (WebEndpointHandlerMappingCustomizer customizer : this.mappingCustomizers) {
				customizer.customize(handlerMapping);
			}
			return handlerMapping;
		}

	}

	@ConditionalOnWebApplication(type = Type.REACTIVE)
	static class ReactiveWebEndpointConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public WebEndpointReactiveHandlerMapping webEndpointReactiveHandlerMapping(
				EndpointProvider<WebEndpointOperation> provider, EndpointSecurityConfigurationFactory endpointSecurityConfigurationFactory) {
			return new WebEndpointReactiveHandlerMapping(provider.getEndpoints(), null, endpointSecurityConfigurationFactory);
		}

	}

}
