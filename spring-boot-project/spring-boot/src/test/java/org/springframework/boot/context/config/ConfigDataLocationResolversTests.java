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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ConfigDataLocationResolvers}.
 *
 * @author Phillip Webb
 */
class ConfigDataLocationResolversTests {

	private Binder binder = mock(Binder.class);

	private Profiles profiles = mock(Profiles.class);

	@Test
	void createWhenInjectingBinderCreatesResolver() {
		ConfigDataLocationResolvers resolvers = new ConfigDataLocationResolvers(null, this.binder,
				Collections.singletonList(TestBoundResolver.class.getName()));
		assertThat(resolvers.getResolvers()).hasSize(1);
		assertThat(resolvers.getResolvers().get(0)).isExactlyInstanceOf(TestBoundResolver.class);
		assertThat(((TestBoundResolver) resolvers.getResolvers().get(0)).getBinder()).isSameAs(this.binder);
	}

	@Test
	void createWhenNotInjectingBinderCreatesResolver() {
		ConfigDataLocationResolvers resolvers = new ConfigDataLocationResolvers(null, this.binder,
				Collections.singletonList(TestResolver.class.getName()));
		assertThat(resolvers.getResolvers()).hasSize(1);
		assertThat(resolvers.getResolvers().get(0)).isExactlyInstanceOf(TestResolver.class);
	}

	@Test
	void createWhenNameIsNotConfigDataLocationResolverThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ConfigDataLocationResolvers(null, this.binder,
						Collections.singletonList(InputStream.class.getName())))
				.withMessageContaining("Unable to instantiate").havingCause().withMessageContaining("not assignable");
	}

	@Test
	void createOrdersResolvers() {
		List<String> names = new ArrayList<>();
		names.add(TestResolver.class.getName());
		names.add(LowestTestResolver.class.getName());
		names.add(HighestTestResolver.class.getName());
		ConfigDataLocationResolvers resolvers = new ConfigDataLocationResolvers(null, this.binder, names);
		assertThat(resolvers.getResolvers().get(0)).isExactlyInstanceOf(HighestTestResolver.class);
		assertThat(resolvers.getResolvers().get(1)).isExactlyInstanceOf(TestResolver.class);
		assertThat(resolvers.getResolvers().get(2)).isExactlyInstanceOf(LowestTestResolver.class);
	}

	@Test
	void resolveAllResolvesUsingFirstSupportedResolver() {
		ConfigDataLocationResolvers resolvers = new ConfigDataLocationResolvers(null, this.binder,
				Arrays.asList(LowestTestResolver.class.getName(), HighestTestResolver.class.getName()));
		List<ConfigDataLocation> resolved = resolvers.resolveAll(this.binder, null,
				Collections.singletonList("LowestTestResolver:test"), null);
		assertThat(resolved).hasSize(1);
		TestConfigDataLocation location = (TestConfigDataLocation) resolved.get(0);
		assertThat(location.getResolver()).isInstanceOf(LowestTestResolver.class);
		assertThat(location.getLocation()).isEqualTo("LowestTestResolver:test");
		assertThat(location.isProfileSpecific()).isFalse();
	}

	@Test
	void resolveAllWhenProfileMergesResolvedLocations() {
		ConfigDataLocationResolvers resolvers = new ConfigDataLocationResolvers(null, this.binder,
				Arrays.asList(LowestTestResolver.class.getName(), HighestTestResolver.class.getName()));
		List<ConfigDataLocation> resolved = resolvers.resolveAll(this.binder, null,
				Collections.singletonList("LowestTestResolver:test"), this.profiles);
		assertThat(resolved).hasSize(2);
		TestConfigDataLocation location = (TestConfigDataLocation) resolved.get(0);
		assertThat(location.getResolver()).isInstanceOf(LowestTestResolver.class);
		assertThat(location.getLocation()).isEqualTo("LowestTestResolver:test");
		assertThat(location.isProfileSpecific()).isFalse();
		TestConfigDataLocation profileLocation = (TestConfigDataLocation) resolved.get(1);
		assertThat(profileLocation.getResolver()).isInstanceOf(LowestTestResolver.class);
		assertThat(profileLocation.getLocation()).isEqualTo("LowestTestResolver:test");
		assertThat(profileLocation.isProfileSpecific()).isTrue();
	}

	@Test
	void resolveWhenNoResolverThrowsException() {
		ConfigDataLocationResolvers resolvers = new ConfigDataLocationResolvers(null, this.binder,
				Arrays.asList(LowestTestResolver.class.getName(), HighestTestResolver.class.getName()));
		assertThatExceptionOfType(UnsupportedConfigDataLocationException.class)
				.isThrownBy(
						() -> resolvers.resolveAll(this.binder, null, Collections.singletonList("Missing:test"), null))
				.satisfies((ex) -> assertThat(ex.getLocation()).isEqualTo("Missing:test"));
	}

	static class TestResolver implements ConfigDataLocationResolver {

		@Override
		public boolean isResolvable(Binder binder, ConfigDataLocation parent, String location) {
			String name = getClass().getName();
			name = name.substring(name.lastIndexOf("$") + 1);
			return location.startsWith(name + ":");
		}

		@Override
		public List<ConfigDataLocation> resolve(Binder binder, ConfigDataLocation parent, String location) {
			return Collections.singletonList(new TestConfigDataLocation(this, location, false));
		}

		@Override
		public List<ConfigDataLocation> resolveProfileSpecific(Binder binder, ConfigDataLocation parent,
				String location, Profiles profiles) {
			return Collections.singletonList(new TestConfigDataLocation(this, location, true));
		}

	}

	static class TestBoundResolver extends TestResolver {

		private final Binder binder;

		TestBoundResolver(Binder binder) {
			this.binder = binder;
		}

		Binder getBinder() {
			return this.binder;
		}

	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	static class HighestTestResolver extends TestResolver {

	}

	@Order(Ordered.LOWEST_PRECEDENCE)
	static class LowestTestResolver extends TestResolver {

	}

	static class TestConfigDataLocation extends ConfigDataLocation {

		private final TestResolver resolver;

		private final String location;

		private final boolean profileSpecific;

		TestConfigDataLocation(TestResolver resolver, String location, boolean profileSpecific) {
			this.resolver = resolver;
			this.location = location;
			this.profileSpecific = profileSpecific;
		}

		TestResolver getResolver() {
			return this.resolver;
		}

		String getLocation() {
			return this.location;
		}

		boolean isProfileSpecific() {
			return this.profileSpecific;
		}

	}

}
