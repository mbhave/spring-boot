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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Central class for interacting with Github's API.
 *
 * @author Madhura Bhave
 */
@Component
public class GithubService {

	private static final String BRANCHES_URI = "https://api.github.com/repos/spring-projects/spring-boot/branches";

	private static final Pattern BRANCH_PATTERN = Pattern.compile("^[0-9]\\.[0-9]\\.x$");

	private final RestTemplate restTemplate;

	public GithubService(RestTemplateBuilder builder, GithubProperties properties) {
		String username = properties.getUsername();
		String password = properties.getPassword();
		if (StringUtils.hasLength(username)) {
			builder = builder.basicAuthentication(username, password);
		}
		this.restTemplate = builder.build();
	}

	@SuppressWarnings("unchecked")
	public String getLatestBranch() {
		try {
			ResponseEntity<Branch[]> response = this.restTemplate.getForEntity(BRANCHES_URI,
					(Class<Branch[]>) Array.newInstance(Branch.class, 0).getClass());
			List<Branch> branches = Arrays.stream(response.getBody())
					.filter((branch) -> BRANCH_PATTERN.matcher(branch.getName()).matches())
					.collect(Collectors.toList());
			Branch latest = branches.get(branches.size() - 1);
			return latest.getName();
		}
		catch (RestClientException ex) {
			return null;
		}
	}

}
