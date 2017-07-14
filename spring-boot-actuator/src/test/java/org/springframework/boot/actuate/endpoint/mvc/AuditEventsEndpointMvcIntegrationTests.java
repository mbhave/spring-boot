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

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointInfrastructureAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointServletWebAutoConfiguration;
import org.springframework.boot.actuate.endpoint.AuditEventsEndpoint;
import org.springframework.boot.actuate.endpoint.web.AuditEventsWebEndpointExtension;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link AuditEventsEndpoint} and
 * {@link AuditEventsWebEndpointExtension} exposed by Spring MVC.
 *
 * @author Vedran Pavic
 * @author Andy Wilkinson
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class AuditEventsEndpointMvcIntegrationTests {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setUp() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	public void contentTypeDefaultsToActuatorV2Json() throws Exception {
		this.mvc.perform(
				get("/application/auditevents").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(header().string("Content-Type",
						"application/vnd.spring-boot.actuator.v2+json;charset=UTF-8"));
	}

	@Test
	public void contentTypeCanBeApplicationJson() throws Exception {
		this.mvc.perform(
				get("/application/auditevents").contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk()).andExpect(header().string("Content-Type",
						MediaType.APPLICATION_JSON_UTF8_VALUE));
	}

	@Test
	public void eventsWithDateAfter() throws Exception {
		this.mvc.perform(get("/application/auditevents").param("after",
				"2016-11-01T13:00:00+00:00")).andExpect(status().isOk())
				.andExpect(content().string("{\"events\":[]}"));
	}

	@Test
	public void eventsWithPrincipalAndDateAfter() throws Exception {
		this.mvc.perform(get("/application/auditevents")
				.contentType(MediaType.APPLICATION_JSON)
				.param("after", "2016-11-01T10:00:00+00:00").param("principal", "user"))
				.andExpect(status().isOk())
				.andExpect(content().string(
						containsString("\"principal\":\"user\",\"type\":\"login\"")))
				.andExpect(content().string(not(containsString("admin"))));
	}

	@Test
	public void eventsWithPrincipalDateAfterAndType() throws Exception {
		this.mvc.perform(get("/application/auditevents")
				.param("after", "2016-11-01T10:00:00+00:00").param("principal", "admin")
				.param("type", "logout"))
				.andExpect(status().isOk())
				.andExpect(content().string(
						containsString("\"principal\":\"admin\",\"type\":\"logout\"")))
				.andExpect(content().string(not(containsString("login"))));
	}

	@Configuration
	@Import({ JacksonAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class, WebMvcAutoConfiguration.class,
			DispatcherServletAutoConfiguration.class,
			EndpointInfrastructureAutoConfiguration.class,
			EndpointServletWebAutoConfiguration.class })
	protected static class TestConfiguration {

		@Bean
		public AuditEventRepository auditEventsRepository() {
			AuditEventRepository repository = new InMemoryAuditEventRepository(3);
			repository.add(createEvent("2016-11-01T11:00:00Z", "admin", "login"));
			repository.add(createEvent("2016-11-01T12:00:00Z", "admin", "logout"));
			repository.add(createEvent("2016-11-01T12:00:00Z", "user", "login"));
			return repository;
		}

		@Bean
		public AuditEventsEndpoint auditEventsEndpoint() {
			return new AuditEventsEndpoint(auditEventsRepository());
		}

		@Bean
		public AuditEventsWebEndpointExtension auditEventsWebEndpointExtension() {
			return new AuditEventsWebEndpointExtension(auditEventsEndpoint());
		}

		private AuditEvent createEvent(String instant, String principal, String type) {
			return new AuditEvent(Date.from(Instant.parse(instant)), principal, type,
					Collections.<String, Object>emptyMap());
		}

	}

}
