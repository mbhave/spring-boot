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

package org.springframework.boot.autoconfigure.condition;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConditionalOnWarDeployment @ConditionalOnWarDeployment}.
 *
 * @author Madhura Bhave
 */
class ConditionalOnWarDeploymentTests {

	private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner(
			AnnotationConfigServletWebApplicationContext::new);

	@Test
	void nonWebApplicationShouldNotMatch() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner();
		contextRunner.withUserConfiguration(NoWebApplicationConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean("noWeb"));
	}

	@Test
	void reactiveWebApplicationShouldNotMatch() {
		ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner();
		contextRunner.withUserConfiguration(ReactiveWebApplicationConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean("reactive"));
	}

	@Test
	void embeddedServletWebApplicationShouldNotMatch() {
		this.contextRunner.withUserConfiguration(ServletWebApplicationConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean("servlet"));
	}

	@Test
	void warDeployedServletWebApplicationShouldMatch() {
		this.contextRunner.withUserConfiguration(WarWebApplicationConfiguration.class)
				.run((context) -> assertThat(context).hasBean("war"));
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication
	static class WarWebApplicationConfiguration extends SpringBootServletInitializer {

		@Bean
		String war() {
			return "war";
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWarDeployment
	static class NoWebApplicationConfiguration {

		@Bean
		String noWeb() {
			return "noWeb";
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWarDeployment
	static class ServletWebApplicationConfiguration {

		@Bean
		String servlet() {
			return "servlet";
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWarDeployment
	static class ReactiveWebApplicationConfiguration {

		@Bean
		String reactive() {
			return "reactive";
		}

	}

}
