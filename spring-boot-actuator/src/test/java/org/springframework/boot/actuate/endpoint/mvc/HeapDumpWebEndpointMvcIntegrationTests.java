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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointInfrastructureAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointServletWebAutoConfiguration;
import org.springframework.boot.actuate.endpoint.web.HeapDumpWebEndpoint;
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
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link HeapDumpWebEndpoint}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HeapDumpWebEndpointMvcIntegrationTests {

	// TODO Replace with equivalent tests for new infrastructure?

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Autowired
	private TestHeapDumpWebEndpoint endpoint;

	@Before
	public void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@After
	public void reset() {
		this.endpoint.reset();
	}

	@Test
	public void invokeWhenNotAvailableShouldReturnServiceUnavailableStatus()
			throws Exception {
		this.endpoint.setAvailable(false);
		this.mvc.perform(get("/application/heapdump"))
				.andExpect(status().isServiceUnavailable());
	}

	@Test
	public void invokeWhenLockedShouldReturnTooManyRequestsStatus() throws Exception {
		this.endpoint.setLocked(true);
		this.mvc.perform(get("/application/heapdump"))
				.andExpect(status().isTooManyRequests());
		assertThat(Thread.interrupted()).isTrue();
	}

	@Test
	public void getRequestShouldReturnHeapDumpInResponseBody() throws Exception {
		this.mvc.perform(get("/application/heapdump")).andExpect(status().isOk())
				.andExpect(content().bytes("HEAPDUMP".getBytes()));
	}

	@Test
	public void invokeOptionsShouldReturnSize() throws Exception {
		this.mvc.perform(options("/application/heapdump")).andExpect(status().isOk());
	}

	@Configuration
	@Import({ JacksonAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class, WebMvcAutoConfiguration.class,
			DispatcherServletAutoConfiguration.class,
			EndpointInfrastructureAutoConfiguration.class,
			EndpointServletWebAutoConfiguration.class })
	public static class TestConfiguration {

		@Bean
		public HeapDumpWebEndpoint endpoint() {
			return new TestHeapDumpWebEndpoint();
		}

	}

	private static class TestHeapDumpWebEndpoint extends HeapDumpWebEndpoint {

		private boolean available;

		private boolean locked;

		private String heapDump;

		TestHeapDumpWebEndpoint() {
			super(TimeUnit.SECONDS.toMillis(1));
			reset();
		}

		public void reset() {
			this.available = true;
			this.locked = false;
			this.heapDump = "HEAPDUMP";
		}

		@Override
		protected HeapDumper createHeapDumper() {
			return (file, live) -> {
				if (!TestHeapDumpWebEndpoint.this.available) {
					throw new HeapDumperUnavailableException("Not available", null);
				}
				if (TestHeapDumpWebEndpoint.this.locked) {
					throw new InterruptedException();
				}
				if (file.exists()) {
					throw new IOException("File exists");
				}
				FileCopyUtils.copy(TestHeapDumpWebEndpoint.this.heapDump.getBytes(),
						file);
			};
		}

		public void setAvailable(boolean available) {
			this.available = available;
		}

		public void setLocked(boolean locked) {
			this.locked = locked;
		}

	}

}
