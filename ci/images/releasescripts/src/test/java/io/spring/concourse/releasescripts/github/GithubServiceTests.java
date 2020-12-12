/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.concourse.releasescripts.github;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link GithubService}.
 *
 * @author Madhura Bhave
 */
@EnableConfigurationProperties(GithubProperties.class)
@RestClientTest(GithubService.class)
public class GithubServiceTests {

	private static final String BRANCHES_URL = "https://api.github.com/repos/spring-projects/spring-boot/branches";

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private GithubService service;

	@Test
	public void getLatestBranch() {
		ClassPathResource resource = new ClassPathResource("branches.json", getClass());
		this.server.expect(requestTo(BRANCHES_URL)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));
		String branch = this.service.getLatestBranch();
		assertThat(branch).isEqualTo("2.1.x");
	}

}