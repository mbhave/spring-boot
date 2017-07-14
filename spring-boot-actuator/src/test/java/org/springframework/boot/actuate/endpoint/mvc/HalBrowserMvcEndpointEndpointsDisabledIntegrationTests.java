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
 * Integration tests for {@link HalBrowserMvcEndpoint} when endpoints are disabled.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = "endpoints.enabled=false")
@DirtiesContext
public class HalBrowserMvcEndpointEndpointsDisabledIntegrationTests {

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
	// this.mockMvc.perform(get("/actuator").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isNotFound());
	// }
	//
	// @Test
	// public void browser() throws Exception {
	// this.mockMvc.perform(get("/actuator/").accept(MediaType.TEXT_HTML))
	// .andExpect(status().isNotFound());
	// }
	//
	// @Test
	// public void trace() throws Exception {
	// this.mockMvc.perform(get("/trace").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isNotFound());
	// }
	//
	// @Test
	// public void envValue() throws Exception {
	// this.mockMvc.perform(get("/env/user.home").accept(MediaType.APPLICATION_JSON))
	// .andExpect(status().isNotFound());
	// }
	//
	// @Test
	// public void endpointsAllDisabled() throws Exception {
	// assertThat(this.mvcEndpoints.getEndpoints()).isEmpty();
	// }
	//
	// @MinimalActuatorHypermediaApplication
	// @Configuration
	// public static class SpringBootHypermediaApplication {
	//
	// }

}
