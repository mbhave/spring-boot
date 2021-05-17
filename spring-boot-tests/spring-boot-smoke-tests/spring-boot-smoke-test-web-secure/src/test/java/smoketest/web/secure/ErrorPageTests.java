/*
 * Copyright 2012-2021 the original author or authors.
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

package smoketest.web.secure;

/**
 * @author Madhura Bhave
 */

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The @SpringBootTest annotation will load the fully ApplicationContext. This will not
 * use slicing and scan for all the stereotype annotations
 * (@Component, @Service, @Respository and @Controller / @RestController) and loads the
 * full application context.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
		classes = { ErrorPageTests.TestConfiguration.class, SampleWebSecureApplication.class },
		properties = { "server.error.include-message=always", "spring.security.user.name=username",
				"spring.security.user.password=password" })
public class ErrorPageTests {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void testBadCredentials() {
		final ResponseEntity<JsonNode> response = this.testRestTemplate.withBasicAuth("username", "wrongpassword")
				.exchange("/test", HttpMethod.GET, null, JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		JsonNode jsonResponse = response.getBody();
		assertThat(jsonResponse).isNull();
	}

	@Test
	public void testNoCredentials() {
		final ResponseEntity<JsonNode> response = this.testRestTemplate.exchange("/test", HttpMethod.GET, null,
				JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		JsonNode jsonResponse = response.getBody();
		assertThat(jsonResponse).isNull();
	}

	@Test
	public void testPublicNotFoundPage() {
		final ResponseEntity<JsonNode> response = this.testRestTemplate.exchange("/public/notfound", HttpMethod.GET,
				null, JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		JsonNode jsonResponse = response.getBody();
		assertThat(jsonResponse).isNull();
	}

	@Test
	public void testCorrectCredentials() {
		final ResponseEntity<String> response = this.testRestTemplate.withBasicAuth("username", "password")
				.exchange("/test", HttpMethod.GET, null, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		response.getBody();
		assertThat(response.getBody()).isEqualTo("test");
	}

	@Configuration(proxyBeanMethods = false)
	protected static class TestConfiguration {

		@RestController
		public static class TestController {

			@GetMapping("/test")
			public String test() {
				return "test";
			}

		}

	}

}