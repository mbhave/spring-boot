/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.env;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor.OriginAndPrefixAwareSystemEnvironmentPropertySource;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SystemEnvironmentPropertySourceEnvironmentPostProcessor}.
 *
 * @author Madhura Bhave
 */
class SystemEnvironmentPropertySourceEnvironmentPostProcessorTests {

	private final ConfigurableEnvironment environment = new StandardEnvironment();

	@Test
	void postProcessShouldReplaceSystemEnvironmentPropertySource() {
		SystemEnvironmentPropertySourceEnvironmentPostProcessor postProcessor = new SystemEnvironmentPropertySourceEnvironmentPostProcessor();
		postProcessor.postProcessEnvironment(this.environment, null);
		PropertySource<?> replaced = this.environment.getPropertySources().get("systemEnvironment");
		assertThat(replaced).isInstanceOf(OriginAndPrefixAwareSystemEnvironmentPropertySource.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void replacedPropertySourceShouldBeOriginAware() {
		SystemEnvironmentPropertySourceEnvironmentPostProcessor postProcessor = new SystemEnvironmentPropertySourceEnvironmentPostProcessor();
		PropertySource<?> original = this.environment.getPropertySources().get("systemEnvironment");
		postProcessor.postProcessEnvironment(this.environment, null);
		OriginAndPrefixAwareSystemEnvironmentPropertySource replaced = (OriginAndPrefixAwareSystemEnvironmentPropertySource) this.environment
				.getPropertySources().get("systemEnvironment");
		Map<String, Object> originalMap = (Map<String, Object>) original.getSource();
		Map<String, Object> replacedMap = replaced.getSource();
		originalMap.forEach((key, value) -> {
			Object actual = replacedMap.get(key);
			assertThat(actual).isEqualTo(value);
			assertThat(replaced.getOrigin(key)).isInstanceOf(SystemEnvironmentOrigin.class);
		});
	}

	@Test
	void replacedPropertySourceWhenPropertyAbsentShouldReturnNullOrigin() {
		SystemEnvironmentPropertySourceEnvironmentPostProcessor postProcessor = new SystemEnvironmentPropertySourceEnvironmentPostProcessor();
		postProcessor.postProcessEnvironment(this.environment, null);
		OriginAndPrefixAwareSystemEnvironmentPropertySource replaced = (OriginAndPrefixAwareSystemEnvironmentPropertySource) this.environment
				.getPropertySources().get("systemEnvironment");
		assertThat(replaced.getOrigin("NON_EXISTENT")).isNull();
	}

	@Test
	void replacedPropertySourceShouldResolveProperty() {
		SystemEnvironmentPropertySourceEnvironmentPostProcessor postProcessor = new SystemEnvironmentPropertySourceEnvironmentPostProcessor();
		Map<String, Object> source = Collections.singletonMap("FOO_BAR_BAZ", "hello");
		this.environment.getPropertySources().replace("systemEnvironment",
				new SystemEnvironmentPropertySource("systemEnvironment", source));
		postProcessor.postProcessEnvironment(this.environment, null);
		OriginAndPrefixAwareSystemEnvironmentPropertySource replaced = (OriginAndPrefixAwareSystemEnvironmentPropertySource) this.environment
				.getPropertySources().get("systemEnvironment");
		SystemEnvironmentOrigin origin = (SystemEnvironmentOrigin) replaced.getOrigin("foo.bar.baz");
		assertThat(origin.getProperty()).isEqualTo("FOO_BAR_BAZ");
		assertThat(replaced.getProperty("foo.bar.baz")).isEqualTo("hello");
	}

	@Test
	void replacedPropertySourceShouldBePrefixAware() {
		testReplacedSource("FOO_BAR_BAZ", "foo");
	}

	@Test
	void replacedPropertySourceWhenPrefixUppercase() {
		testReplacedSource("FOO_BAR_BAZ", "FOO");
	}

	@Test
	void replacedPropertySourceWhenPrefixContainsUnderscore() {
		testReplacedSource("FOO_BING_BAR_BAZ", "FOO_BING");
	}

	@Test
	void replacedPropertySourceWhenPrefixEndsWithUnderscore() {
		testReplacedSource("FOO_BING_BAR_BAZ", "FOO_BING_");
	}

	@Test
	void replacedPropertySourceWhenPrefixEndsWithDot() {
		testReplacedSource("FOO_BING_BAR_BAZ", "foo.bing.");
	}

	@Test
	void replacedPropertySourceWhenPrefixEndsWithHypen() {
		testReplacedSource("FOO_BING_BAR_BAZ", "foo.bing.");
	}

	private void testReplacedSource(String foo_bar_baz, String foo) {
		SystemEnvironmentPropertySourceEnvironmentPostProcessor postProcessor = new SystemEnvironmentPropertySourceEnvironmentPostProcessor();
		Map<String, Object> source = Collections.singletonMap(foo_bar_baz, "hello");
		this.environment.getPropertySources().replace("systemEnvironment",
				new SystemEnvironmentPropertySource("systemEnvironment", source));
		SpringApplication application = new SpringApplication();
		application.setEnvironmentPrefix(foo);
		postProcessor.postProcessEnvironment(this.environment, application);
		OriginAndPrefixAwareSystemEnvironmentPropertySource replaced = (OriginAndPrefixAwareSystemEnvironmentPropertySource) this.environment
				.getPropertySources().get("systemEnvironment");
		SystemEnvironmentOrigin origin = (SystemEnvironmentOrigin) replaced.getOrigin("bar.baz");
		assertThat(origin.getProperty()).isEqualTo(foo_bar_baz);
		assertThat(replaced.getProperty("bar.baz")).isEqualTo("hello");
	}

}
