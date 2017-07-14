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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link HalBrowserMvcEndpoint}
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = { "endpoints.hypermedia.enabled=true",
		"management.security.enabled=false" })
public class HalBrowserMvcEndpointVanillaIntegrationTests {

	// TODO Replace with equivalent tests for new infrastructure?

	// @Autowired
	// private WebApplicationContext context;
	//
	// @Autowired
	// private MvcEndpoints mvcEndpoints;
	//
	// private MockMvc mockMvc;
	//
	// @Before
	// public void setUp() {
	// this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	// }
	//
	// @Test
	// public void links() throws Exception {
	// this.mockMvc.perform(get("/application").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links").exists())
	// .andExpect(header().doesNotExist("cache-control"));
	// }
	//
	// @Test
	// public void linksWithTrailingSlash() throws Exception {
	// this.mockMvc.perform(get("/application/").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links").exists())
	// .andExpect(header().doesNotExist("cache-control"));
	// }
	//
	// @Test
	// public void browser() throws Exception {
	// this.mockMvc.perform(get("/application/").accept(MediaType.TEXT_HTML))
	// .andExpect(status().isFound())
	// .andExpect(header().string(HttpHeaders.LOCATION,
	// "http://localhost/application/browser.html"));
	// }
	//
	// @Test
	// public void trace() throws Exception {
	// this.mockMvc.perform(get("/application/trace").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links").doesNotExist())
	// .andExpect(jsonPath("$").isArray());
	// }
	//
	// @Test
	// public void envValue() throws Exception {
	// this.mockMvc
	// .perform(get("/application/env/user.home")
	// .accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk())
	// .andExpect(jsonPath("$._links").doesNotExist());
	// }
	//
	// @Test
	// public void endpointsAllListed() throws Exception {
	// for (MvcEndpoint endpoint : this.mvcEndpoints.getEndpoints()) {
	// String path = endpoint.getPath();
	// if ("/application".equals(path)) {
	// continue;
	// }
	// path = path.startsWith("/") ? path.substring(1) : path;
	// this.mockMvc.perform(get("/application").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk())
	// .andExpect(jsonPath("$._links.%s.href", path).exists());
	// }
	// }
	//
	// @Test
	// public void endpointsEachHaveSelf() throws Exception {
	// Set<String> collections = new HashSet<>(Arrays.asList("/trace", "/beans", "/dump",
	// "/heapdump", "/loggers", "/auditevents"));
	// for (MvcEndpoint endpoint : this.mvcEndpoints.getEndpoints()) {
	// String path = endpoint.getPath();
	// if (collections.contains(path)) {
	// continue;
	// }
	// path = "/application" + (path.length() > 0 ? path : "/");
	// this.mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links.self.href")
	// .value("http://localhost/application" + endpoint.getPath()));
	// }
	// }
	//
	// @MinimalActuatorHypermediaApplication
	// @Configuration
	// public static class SpringBootHypermediaApplication {
	//
	// }

}
