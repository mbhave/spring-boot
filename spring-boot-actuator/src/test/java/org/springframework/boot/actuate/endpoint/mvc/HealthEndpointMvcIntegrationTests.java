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

package org.springframework.boot.actuate.endpoint.mvc;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointInfrastructureAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointServletWebAutoConfiguration;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.web.HealthWebEndpointExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link HealthEndpoint} and {@link HealthWebEndpointExtension}
 * exposed by Spring MVC.
 *
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HealthEndpointMvcIntegrationTests {

	// TODO Test Jersey and WebFlux too?

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setUp() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	public void whenHealthIsUp200ResponseIsReturned() throws Exception {
		this.mvc.perform(get("/application/health")).andExpect(status().isOk())
				.andExpect(jsonPath("status", equalTo("UP")))
				.andExpect(jsonPath("alpha.status", equalTo("UP")))
				.andExpect(jsonPath("bravo.status", equalTo("UP")));
	}

	@Test
	public void whenHealthIsDown503ResponseIsReturned() throws Exception {
		this.context.getBean("alphaHealthIndicator", TestHealthIndicator.class)
				.setHealth(Health.down().build());
		this.mvc.perform(get("/application/health"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("status", equalTo("DOWN")))
				.andExpect(jsonPath("alpha.status", equalTo("DOWN")))
				.andExpect(jsonPath("bravo.status", equalTo("UP")));
	}

	@Configuration
	@Import({ JacksonAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class, WebMvcAutoConfiguration.class,
			DispatcherServletAutoConfiguration.class,
			EndpointInfrastructureAutoConfiguration.class,
			EndpointServletWebAutoConfiguration.class })
	public static class TestConfiguration {

		@Bean
		public HealthEndpoint healthEndpoint(
				Map<String, HealthIndicator> healthIndicators) {
			return new HealthEndpoint(new OrderedHealthAggregator(), healthIndicators);
		}

		@Bean
		public HealthWebEndpointExtension healthWebEndpointExtension(
				HealthEndpoint delegate) {
			return new HealthWebEndpointExtension(delegate);
		}

		@Bean
		public TestHealthIndicator alphaHealthIndicator() {
			return new TestHealthIndicator();
		}

		@Bean
		public TestHealthIndicator bravoHealthIndicator() {
			return new TestHealthIndicator();
		}

	}

	private static class TestHealthIndicator implements HealthIndicator {

		private Health health = Health.up().build();

		@Override
		public Health health() {
			return this.health;
		}

		public void setHealth(Health health) {
			this.health = health;
		}

	}

}
