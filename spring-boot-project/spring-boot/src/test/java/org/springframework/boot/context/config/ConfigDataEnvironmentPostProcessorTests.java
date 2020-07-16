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

package org.springframework.boot.context.config;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link ConfigDataEnvironmentPostProcessor}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class ConfigDataEnvironmentPostProcessorTests {

	private ConfigDataEnvironment configDataEnvironment = mock(ConfigDataEnvironment.class);

	private ConfigDataEnvironmentPostProcessor postProcessor = spy(
			new ConfigDataEnvironmentPostProcessor(Supplier::get));

	private StandardEnvironment environment;

	private SpringApplication application;

	@BeforeEach
	void setup() {
		this.application = new SpringApplication();
		this.environment = new StandardEnvironment();
	}

	@Test
	@SuppressWarnings("deprecation")
	void defaultOrderMatchesDeprecatedListener() {
		assertThat(ConfigDataEnvironmentPostProcessor.ORDER).isEqualTo(ConfigFileApplicationListener.DEFAULT_ORDER);
	}

	@Test
	void configDataEnvironmentIsCreatedWithDefaultResourceLoaderIfNull() {
		doReturn(this.configDataEnvironment).when(this.postProcessor).getConfigDataEnvironment(eq(this.environment),
				any(DefaultResourceLoader.class), eq(Collections.emptySet()));
		this.postProcessor.postProcessEnvironment(this.environment, this.application);
		verify(this.configDataEnvironment).processAndApply();
	}

	@Test
	void configDataEnvironmentIsCreatedWithCustomResourceLoaderIfAvailable() {
		ResourceLoader resourceLoader = mock(ResourceLoader.class);
		this.application.setResourceLoader(resourceLoader);
		doReturn(this.configDataEnvironment).when(this.postProcessor).getConfigDataEnvironment(eq(this.environment),
				eq(resourceLoader), eq(Collections.emptySet()));
		this.postProcessor.postProcessEnvironment(this.environment, this.application);
		verify(this.configDataEnvironment).processAndApply();
	}

	@Test
	void postProcessShouldWhenAdditionalProfilesOnSpringApplication() {
		this.application.setAdditionalProfiles("dev");
		ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
		doReturn(this.configDataEnvironment).when(this.postProcessor).getConfigDataEnvironment(eq(this.environment),
				any(DefaultResourceLoader.class), captor.capture());
		this.postProcessor.postProcessEnvironment(this.environment, this.application);
		assertThat(captor.getValue()).containsExactly("dev");
		verify(this.configDataEnvironment).processAndApply();
	}

	@Test
	@SuppressWarnings("deprecation")
	void postProcessWhenUseLegacyProcessingShouldSwitch() {
		ConfigDataEnvironmentPostProcessor.LegacyConfigFileApplicationListener listener = mock(
				ConfigDataEnvironmentPostProcessor.LegacyConfigFileApplicationListener.class);
		doThrow(new UseLegacyConfigProcessingException(null)).when(this.postProcessor).getConfigDataEnvironment(
				eq(this.environment), any(DefaultResourceLoader.class), eq(Collections.emptySet()));
		doReturn(listener).when(this.postProcessor).getLegacyListener();
		this.postProcessor.postProcessEnvironment(this.environment, this.application);
		verifyNoInteractions(this.configDataEnvironment);
		verify(listener).addPropertySources(eq(this.environment), any(DefaultResourceLoader.class));
	}

}
