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

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.endpoint.web.EndpointSecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.mvc.WebEndpointServletHandlerMapping;
import org.springframework.boot.endpoint.web.reactive.WebEndpointReactiveHandlerMapping;
import org.springframework.boot.test.context.WebApplicationContextTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebEndpointInfrastructureManagementContextConfiguration}.
 *
 * @author Madhura Bhave
 */
public class WebEndpointInfrastructureManagementContextConfigurationTests {

	private WebApplicationContextTester contextTester;

	@Test
	public void jerseyEndpointInfrastructureManagementContextConfiguration() throws Exception {
		this.contextTester = new WebApplicationContextTester()
				.withConfiguration(AutoConfigurations.of(
						JacksonAutoConfiguration.class,
						EndpointInfrastructureAutoConfiguration.class,
						WebEndpointInfrastructureManagementContextConfiguration.class))
				.withUserConfiguration(JerseyAppConfiguration.class);
		this.contextTester.run(context -> {
			assertThat(context).getBean(ResourceConfigCustomizer.class).isNotNull();
			//TODO assert on security interceptor
		});
	}

	@Test
	public void mvcEndpointInfrastructureManagementContextConfiguration() throws Exception {
		this.contextTester = new WebApplicationContextTester()
				.withConfiguration(AutoConfigurations.of(
						JacksonAutoConfiguration.class,
						DispatcherServletAutoConfiguration.class,
						HttpMessageConvertersAutoConfiguration.class,
						WebMvcAutoConfiguration.class, EndpointAutoConfiguration.class,
						EndpointInfrastructureAutoConfiguration.class,
						EndpointServletWebAutoConfiguration.class,
						WebEndpointInfrastructureManagementContextConfiguration.class));
		this.contextTester.run(context -> {
			assertThat(context).getBean(WebEndpointServletHandlerMapping.class).isNotNull();
			EndpointSecurityConfigurationFactory factory = (EndpointSecurityConfigurationFactory) ReflectionTestUtils
					.getField(context.getBean(WebEndpointServletHandlerMapping.class), "securityConfigurationFactory");
			assertThat(factory.getEnvironment()).isEqualTo(context.getEnvironment());
		});
	}

	@Test
	@Ignore("Reactive type needs to be loaded")
	public void reactiveEndpointInfrastructureManagementContextConfiguration() throws Exception {
		this.contextTester = new WebApplicationContextTester()
				.withConfiguration(AutoConfigurations.of(
						JacksonAutoConfiguration.class,
						HttpMessageConvertersAutoConfiguration.class,
						EndpointAutoConfiguration.class,
						EndpointInfrastructureAutoConfiguration.class,
						WebEndpointInfrastructureManagementContextConfiguration.class));
		this.contextTester.run(context -> {
			assertThat(context).getBean(WebEndpointReactiveHandlerMapping.class).isNotNull();
			EndpointSecurityConfigurationFactory factory = (EndpointSecurityConfigurationFactory) ReflectionTestUtils
					.getField(context.getBean(WebEndpointReactiveHandlerMapping.class), "securityConfigurationFactory");
			assertThat(factory.getEnvironment()).isEqualTo(context.getEnvironment());
		});
	}

	@Configuration
	static class JerseyAppConfiguration {

		@Bean
		public ResourceConfig resourceConfig() {
			return new ResourceConfig();
		}

	}
}
