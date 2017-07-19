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

package org.springframework.boot.actuate.endpoint.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileCopyUtils;

/**
 * Integration tests for {@link HeapDumpWebEndpoint} exposed by Jersey, Spring MVC, and
 * WebFlux.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
@RunWith(WebEndpointsRunner.class)
public class HeapDumpWebEndpointWebIntegrationTests {

	private static WebTestClient client;

	private static ConfigurableApplicationContext context;

	private TestHeapDumpWebEndpoint endpoint;

	@Before
	public void configureEndpoint() {
		this.endpoint = context.getBean(TestHeapDumpWebEndpoint.class);
		this.endpoint.setAvailable(true);
		this.endpoint.setLocked(false);
	}

	@Test
	public void invokeWhenNotAvailableShouldReturnServiceUnavailableStatus()
			throws Exception {
		this.endpoint.setAvailable(false);
		client.get().uri("/application/heapdump").exchange().expectStatus()
				.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	public void invokeWhenLockedShouldReturnTooManyRequestsStatus() throws Exception {
		this.endpoint.setLocked(true);
		client.get().uri("/application/heapdump").exchange().expectStatus()
				.isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	public void getRequestShouldReturnHeapDumpInResponseBody() throws Exception {
		client.get().uri("/application/heapdump").exchange().expectStatus().isOk()
				.expectBody(String.class).isEqualTo("HEAPDUMP");
	}

	@Configuration
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
