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

import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests for {@link HeapdumpEndpoint} OPTIONS call with security.
 *
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HeapdumpMvcEndpointSecureOptionsTests {

	// TODO Replace with equivalent tests for new infrastructure?

	// @Autowired
	// private WebApplicationContext context;
	//
	// private MockMvc mvc;
	//
	// @Autowired
	// private TestHeapdumpMvcEndpoint endpoint;
	//
	// @Before
	// public void setup() {
	// this.context.getBean(HeapdumpEndpoint.class).setEnabled(true);
	// this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	// }
	//
	// @After
	// public void reset() {
	// this.endpoint.reset();
	// }
	//
	// @Test
	// public void invokeOptionsShouldReturnSize() throws Exception {
	// this.mvc.perform(options("/application/heapdump")).andExpect(status().isOk());
	// }
	//
	// @Import({ JacksonAutoConfiguration.class,
	// HttpMessageConvertersAutoConfiguration.class,
	// EndpointServletWebAutoConfiguration.class, WebMvcAutoConfiguration.class })
	// @Configuration
	// public static class TestConfiguration {
	//
	// @Bean
	// public HeapdumpEndpoint endpoint() {
	// return new TestHeapdumpMvcEndpoint();
	// }
	//
	// }
	//
	// private static class TestHeapdumpMvcEndpoint extends HeapdumpEndpoint {
	//
	// private boolean available;
	//
	// private boolean locked;
	//
	// private String heapDump;
	//
	// TestHeapdumpMvcEndpoint() {
	// super(TimeUnit.SECONDS.toMillis(1));
	// reset();
	// }
	//
	// public void reset() {
	// this.available = true;
	// this.locked = false;
	// this.heapDump = "HEAPDUMP";
	// }
	//
	// @Override
	// protected HeapDumper createHeapDumper() {
	// return new HeapDumper() {
	//
	// @Override
	// public void dumpHeap(File file, boolean live)
	// throws IOException, InterruptedException {
	// if (!TestHeapdumpMvcEndpoint.this.available) {
	// throw new HeapDumperUnavailableException("Not available", null);
	// }
	// if (TestHeapdumpMvcEndpoint.this.locked) {
	// throw new InterruptedException();
	// }
	// if (file.exists()) {
	// throw new IOException("File exists");
	// }
	// FileCopyUtils.copy(TestHeapdumpMvcEndpoint.this.heapDump.getBytes(),
	// file);
	// }
	//
	// };
	// }
	//
	// }

}
