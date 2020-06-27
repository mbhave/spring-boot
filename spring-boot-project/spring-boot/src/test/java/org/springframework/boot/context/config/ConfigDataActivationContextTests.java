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
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigDataActivationContext}.
 *
 * @author Phillip Webb
 */
class ConfigDataActivationContextTests {

	@Test
	void getCloudPlatformWhenCloudPropertyNotPresentDeducesCloudPlatform() {
		Environment environment = new MockEnvironment();
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = new ConfigDataActivationContext(environment, binder);
		assertThat(context.getCloudPlatform()).isNull();
	}

	@Test
	void getCloudPlatformWhenClouldPropertyInEnvironmentDeducesCloudPlatform() {
		MockEnvironment environment = createKuberntesEnvironment();
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = new ConfigDataActivationContext(environment, binder);
		assertThat(context.getCloudPlatform()).isEqualTo(CloudPlatform.KUBERNETES);
	}

	@Test
	void getCloudPlatformWhenCloudPropertyHasBeenContributedDuringInitialLoadDeducesCloudPlatform() {
		Environment environment = createKuberntesEnvironment();
		Binder binder = new Binder(
				new MapConfigurationPropertySource(Collections.singletonMap("spring.main.cloud-platform", "HEROKU")));
		ConfigDataActivationContext context = new ConfigDataActivationContext(environment, binder);
		assertThat(context.getCloudPlatform()).isEqualTo(CloudPlatform.HEROKU);
	}

	@Test
	void getActiveProfilesWhenNotActivatedReturnsNull() {
		Environment environment = new MockEnvironment();
		ConfigDataActivationContext context = createContext(environment);
		assertThat(context.hasActivatedProfiles()).isFalse();
		assertThat(context.getActiveProfiles()).isNull();
	}

	@Test
	void getActiveProfilesWhenNoEnvironmentProfilesAndNoPropertyReturnsEmptyArray() {
		Environment environment = new MockEnvironment();
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).isEmpty();
	}

	@Test
	void getActiveProfilesWhenNoEnvironmentProfilesAndBinderProperty() {
		Environment environment = new MockEnvironment();
		Binder binder = new Binder(
				new MapConfigurationPropertySource(Collections.singletonMap("spring.profiles.active", "a,b,c")));
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveProfilesWhenNoEnvironmentProfilesAndEnvironmentProperty() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.profiles.active", "a,b,c");
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveProfilesWhenEnvironmentProfilesAndBinderProperty() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("a", "b", "c");
		Binder binder = new Binder(
				new MapConfigurationPropertySource(Collections.singletonMap("spring.profiles.active", "d,e,f")));
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveProfilesWhenEnvironmentProfilesAndEnvironmentProperty() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("a", "b", "c");
		environment.setProperty("spring.profiles.active", "d,e,f");
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveProfilesWhenNoEnvironmentProfilesAndEnvironmentPropertyInBindNotation() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.profiles.active[0]", "a");
		environment.setProperty("spring.profiles.active[1]", "b");
		environment.setProperty("spring.profiles.active[2]", "c");
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveProfilesWhenEnvironmentProfilesInBindNotationAndEnvironmentPropertyReturnsEnvironmentProfiles() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("a", "b", "c");
		environment.setProperty("spring.profiles.active[0]", "d");
		environment.setProperty("spring.profiles.active[1]", "e");
		environment.setProperty("spring.profiles.active[2]", "f");
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext context = createContext(environment).withActivedProfiles(environment, binder);
		assertThat(context.hasActivatedProfiles()).isTrue();
		assertThat(context.getActiveProfiles()).containsExactly("a", "b", "c");
	}

	private ConfigDataActivationContext createContext(Environment environment) {
		Binder binder = Binder.get(environment);
		return new ConfigDataActivationContext(environment, binder);
	}

	private MockEnvironment createKuberntesEnvironment() {
		MockEnvironment environment = new MockEnvironment();
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("KUBERNETES_SERVICE_HOST", "host");
		map.put("KUBERNETES_SERVICE_PORT", "port");
		PropertySource<?> propertySource = new MapPropertySource(
				StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, map);
		environment.getPropertySources().addLast(propertySource);
		return environment;
	}

}
