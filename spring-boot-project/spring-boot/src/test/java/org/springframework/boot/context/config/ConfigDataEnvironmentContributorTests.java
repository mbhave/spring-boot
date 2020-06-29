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

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.ImportPhase;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ConfigDataEnvironmentContributor}.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributorTests {

	private ConfigDataActivationContext activationContext = mock(ConfigDataActivationContext.class);

	@Test
	void createWithImportsReturnsInstance() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor("a", "b");
		assertThat(contributor.getLocation()).isNull();
		assertThat(contributor.hasUnconsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isTrue();
		assertThat(contributor.getImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).containsExactly("a", "b");
		assertThat(contributor.hasUnconsumedImports(ImportPhase.AFTER_PROFILE_ACTIVATION)).isTrue();
		assertThat(contributor.getImports(ImportPhase.AFTER_PROFILE_ACTIVATION)).containsExactly("a", "b");
		assertThat(contributor.isActive(this.activationContext)).isTrue();
	}

	@Test
	void getImportsReturnsImports() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor("a", "b");
		assertThat(contributor.getImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).containsExactly("a", "b");
	}

	@Test
	void getImportsWhenConsumedReturnsEmptyList() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor("a", "b")
				.withConsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION);
		assertThat(contributor.getImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isEmpty();
	}

	@Test
	void hasUnconsumedImportsWhenNotConsumedReturnsTrue() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor("a", "b");
		assertThat(contributor.hasUnconsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isTrue();
	}

	@Test
	void hasUnconsumedImportsWhenConsumedReturnsFalse() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor("a", "b")
				.withConsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION);
		assertThat(contributor.hasUnconsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isFalse();
	}

	@Test
	void hasUnconsumedImportsWhenImportsAreEmptyReturnsFalse() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor();
		assertThat(contributor.hasUnconsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isFalse();
	}

	@Test
	void withConsumedImportsReturnsNewInstance() {
		ConfigDataEnvironmentContributor contributor = new ConfigDataEnvironmentContributor("a", "b");
		ConfigDataEnvironmentContributor consumedContributor = contributor
				.withConsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION);
		assertThat(consumedContributor).isNotSameAs(contributor);
		assertThat(contributor.hasUnconsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isTrue();
		assertThat(consumedContributor.hasUnconsumedImports(ImportPhase.BEFORE_PROFILE_ACTIVATION)).isFalse();
	}

	@Test
	void getImportPhaseWhenNoActiveProfilesReturnsBeforeProfileActivation() {
		ConfigDataActivationContext activationContext = createActivationContext();
		ImportPhase phase = ImportPhase.get(activationContext);
		assertThat(phase).isEqualTo(ImportPhase.BEFORE_PROFILE_ACTIVATION);
	}

	@Test
	void getImportPhaseWhenHasActiveProfilesReturnsAfterProfileActivation() {
		Profiles profiles = mock(Profiles.class);
		ConfigDataActivationContext activationContext = createActivationContext().withProfiles(profiles);
		ImportPhase phase = ImportPhase.get(activationContext);
		assertThat(phase).isEqualTo(ImportPhase.AFTER_PROFILE_ACTIVATION);
	}

	private ConfigDataActivationContext createActivationContext() {
		MockEnvironment environment = new MockEnvironment();
		Binder binder = Binder.get(environment);
		ConfigDataActivationContext activationContext = new ConfigDataActivationContext(environment, binder);
		return activationContext;
	}

}
