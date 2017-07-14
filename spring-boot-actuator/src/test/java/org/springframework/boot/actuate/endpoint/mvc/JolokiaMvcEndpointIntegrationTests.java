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
 * Integration tests for {@link JolokiaMvcEndpoint}.
 *
 * @author Christian Dupuis
 * @author Dave Syer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "management.security.enabled=false")
public class JolokiaMvcEndpointIntegrationTests {

	// TODO Replace with equivalent tests for new infrastructure?

	// @Autowired
	// private MvcEndpoints endpoints;
	//
	// @Autowired
	// private WebApplicationContext context;
	//
	// private MockMvc mvc;
	//
	// @Before
	// public void setUp() {
	// this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	// TestPropertyValues.of("foo:bar")
	// .applyTo((ConfigurableApplicationContext) this.context);
	// }
	//
	// @Test
	// public void endpointRegistered() throws Exception {
	// Set<? extends MvcEndpoint> values = this.endpoints.getEndpoints();
	// assertThat(values).hasAtLeastOneElementOfType(JolokiaMvcEndpoint.class);
	// }
	//
	// @Test
	// public void search() throws Exception {
	// this.mvc.perform(get("/application/jolokia/search/java.lang:*"))
	// .andExpect(status().isOk())
	// .andExpect(content().string(containsString("GarbageCollector")));
	// }
	//
	// @Test
	// public void read() throws Exception {
	// this.mvc.perform(get("/application/jolokia/read/java.lang:type=Memory"))
	// .andExpect(status().isOk())
	// .andExpect(content().string(containsString("NonHeapMemoryUsage")));
	// }
	//
	// @Test
	// public void list() throws Exception {
	// this.mvc.perform(get("/application/jolokia/list/java.lang/type=Memory/attr"))
	// .andExpect(status().isOk())
	// .andExpect(content().string(containsString("NonHeapMemoryUsage")));
	// }
	//
	// @Configuration
	// @EnableConfigurationProperties
	// @EnableWebMvc
	// @Import({ JacksonAutoConfiguration.class, AuditAutoConfiguration.class,
	// HttpMessageConvertersAutoConfiguration.class,
	// EndpointServletWebAutoConfiguration.class, JolokiaAutoConfiguration.class })
	// public static class Config {
	//
	// }

}
