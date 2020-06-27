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

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Profiles}.
 *
 * @author Phillip Webb
 */
class ProfilesTests {

	@Test
	void getActiveWhenNoEnvironmentProfilesAndNoPropertyReturnsEmptyArray() {
		Environment environment = new MockEnvironment();
		Binder binder = Binder.get(environment);
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).isEmpty();
	}

	@Test
	void getActiveWhenNoEnvironmentProfilesAndBinderProperty() {
		Environment environment = new MockEnvironment();
		Binder binder = new Binder(
				new MapConfigurationPropertySource(Collections.singletonMap("spring.profiles.active", "a,b,c")));
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveWhenNoEnvironmentProfilesAndEnvironmentProperty() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.profiles.active", "a,b,c");
		Binder binder = Binder.get(environment);
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveWhenEnvironmentProfilesAndBinderProperty() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("a", "b", "c");
		Binder binder = new Binder(
				new MapConfigurationPropertySource(Collections.singletonMap("spring.profiles.active", "d,e,f")));
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveWhenEnvironmentProfilesAndEnvironmentProperty() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("a", "b", "c");
		environment.setProperty("spring.profiles.active", "d,e,f");
		Binder binder = Binder.get(environment);
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveWhenNoEnvironmentProfilesAndEnvironmentPropertyInBindNotation() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.profiles.active[0]", "a");
		environment.setProperty("spring.profiles.active[1]", "b");
		environment.setProperty("spring.profiles.active[2]", "c");
		Binder binder = Binder.get(environment);
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).containsExactly("a", "b", "c");
	}

	@Test
	void getActiveWhenEnvironmentProfilesInBindNotationAndEnvironmentPropertyReturnsEnvironmentProfiles() {
		MockEnvironment environment = new MockEnvironment();
		environment.setActiveProfiles("a", "b", "c");
		environment.setProperty("spring.profiles.active[0]", "d");
		environment.setProperty("spring.profiles.active[1]", "e");
		environment.setProperty("spring.profiles.active[2]", "f");
		Binder binder = Binder.get(environment);
		Profiles profiles = new Profiles(environment, binder);
		assertThat(profiles.getActive()).containsExactly("a", "b", "c");
	}

}
