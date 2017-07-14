/*
 * Copyright 2012-2016 the original author or authors.
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
 * Integration tests for {@link HalBrowserMvcEndpoint} when a custom management context
 * path has been configured.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "management.contextPath:/admin",
		"management.security.enabled=false" })
@DirtiesContext
public class HalBrowserMvcEndpointManagementContextPathIntegrationTests {

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
	// public void actuatorHomeJson() throws Exception {
	// this.mockMvc.perform(get("/admin").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links").exists());
	// }
	//
	// @Test
	// public void actuatorHomeWithTrailingSlashJson() throws Exception {
	// this.mockMvc.perform(get("/admin/").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links").exists());
	// }
	//
	// @Test
	// public void actuatorHomeHtml() throws Exception {
	// this.mockMvc.perform(get("/admin/").accept(MediaType.TEXT_HTML))
	// .andExpect(status().isFound()).andExpect(header().string(
	// HttpHeaders.LOCATION, "http://localhost/admin/browser.html"));
	// }
	//
	// @Test
	// public void actuatorBrowserHtml() throws Exception {
	// this.mockMvc
	// .perform(get("/admin/browser.html").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk())
	// .andExpect(content().string(containsString("entryPoint: '/admin'")));
	// }
	//
	// @Test
	// public void trace() throws Exception {
	// this.mockMvc.perform(get("/admin/trace").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk()).andExpect(jsonPath("$._links").doesNotExist())
	// .andExpect(jsonPath("$").isArray());
	// }
	//
	// @Test
	// public void endpointsAllListed() throws Exception {
	// for (MvcEndpoint endpoint : this.mvcEndpoints.getEndpoints()) {
	// String path = endpoint.getPath();
	// if ("/actuator".equals(path)) {
	// continue;
	// }
	// path = path.startsWith("/") ? path.substring(1) : path;
	// path = path.length() > 0 ? path : "self";
	// this.mockMvc.perform(get("/admin").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isOk())
	// .andExpect(jsonPath("$._links.%s.href", path)
	// .value("http://localhost/admin" + endpoint.getPath()));
	// }
	// }
	//
	// @MinimalActuatorHypermediaApplication
	// @RestController
	// public static class SpringBootHypermediaApplication {
	//
	// @RequestMapping("")
	// public ResourceSupport home() {
	// ResourceSupport resource = new ResourceSupport();
	// resource.add(linkTo(SpringBootHypermediaApplication.class).slash("/")
	// .withSelfRel());
	// return resource;
	// }
	//
	// }

}
