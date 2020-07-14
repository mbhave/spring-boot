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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.ImportPhase;
import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.Kind;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigDataEnvironmentContributor}.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributorTests {

	@Test
	void createWithOnlyImportsCreatesInstance() {
	}

	@Test
	void createCreatesInstance() {
	}

	@Test
	void isActiveWhenAlwaysActiveReturnsTrue() {
	}

	@Test
	void getPropertySourceReturnsPropertySource() {
	}

	@Test
	void getConfigurationPropertySourceReturnsConfigurationPropertySource() {
	}

	@Test
	void getConfigurationPropertySourceWhenPropertySourceIsNullReturnsNull() {
	}

	@Test
	void hasUnprocessedImportsWhenNoImportsReturnsFalse() {
	}

	@Test
	void hasUnprocessedImportsWhenNoChildrenInImportPhaseReturnsTrue() {
	}

	@Test
	void hasUnprocessedImportsWhenEmptyChildrenInImportPhaseReturnsTrue() {

	}

	@Test
	void hasUnprocessedImportsWhenChildrenInImportPhaseReturnsTrue() {

	}

	@Test
	void getChildrenWhenNoChildrenInImportPhaseReturnsEmptyList() {

	}

	@Test
	void getChildrenWhenHasChildrenReturnsList() {

	}

	@Test
	void iteratorWhenSingleContributorReturnsSingletonIterator() {
		ConfigDataEnvironmentContributor contributor = createConfigDataEnvironmentContributor("a");
		assertThat(asLocationsList(contributor.iterator())).containsExactly("a");
	}

	@Test
	void iteratorWhenTypicalStructureReturnsCorrectlyOrderedIterator() {
		ConfigDataEnvironmentContributor fileProfile = createConfigDataEnvironmentContributor(
				"file:application-profile.properties");
		ConfigDataEnvironmentContributor fileApplication = createConfigDataEnvironmentContributor(
				"file:application.properties");
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> fileChildren = new LinkedHashMap<>();
		fileChildren.put(ImportPhase.AFTER_PROFILE_ACTIVATION, Arrays.asList(fileProfile));
		fileChildren.put(ImportPhase.BEFORE_PROFILE_ACTIVATION, Arrays.asList(fileApplication));
		ConfigDataEnvironmentContributor fileImports = createConfigDataEnvironmentContributor("file:./", fileChildren);
		ConfigDataEnvironmentContributor classpathProfile = createConfigDataEnvironmentContributor(
				"classpath:application-profile.properties");
		ConfigDataEnvironmentContributor classpathApplication = createConfigDataEnvironmentContributor(
				"classpath:application.properties");
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> classPathChildren = new LinkedHashMap<>();
		classPathChildren.put(ImportPhase.AFTER_PROFILE_ACTIVATION, Arrays.asList(classpathProfile));
		classPathChildren.put(ImportPhase.BEFORE_PROFILE_ACTIVATION, Arrays.asList(classpathApplication));
		ConfigDataEnvironmentContributor classpathImports = createConfigDataEnvironmentContributor("classpath:/",
				classPathChildren);
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> rootChildren = new LinkedHashMap<>();
		rootChildren.put(ImportPhase.BEFORE_PROFILE_ACTIVATION, Arrays.asList(fileImports, classpathImports));
		ConfigDataEnvironmentContributor root = createConfigDataEnvironmentContributor("root", rootChildren);
		assertThat(asLocationsList(root.iterator())).containsExactly("file:application-profile.properties",
				"file:application.properties", "file:./", "classpath:application-profile.properties",
				"classpath:application.properties", "classpath:/", "root");
	}

	@Test
	void iteratorWithExtractorReturnsExtractingIterator() {
		ConfigDataEnvironmentContributor contributor = createConfigDataEnvironmentContributor("a");
		Iterable<String> iterable = () -> contributor.iterator(this::getLocationName);
		assertThat(iterable).containsExactly("a");
	}

	@Test
	void withChildrenReturnsNewInstanceWithChildren() {

	}

	private List<String> asLocationsList(Iterator<ConfigDataEnvironmentContributor> iterator) {
		List<String> list = new ArrayList<>();
		iterator.forEachRemaining((contributor) -> list.add(getLocationName(contributor)));
		return list;
	}

	private String getLocationName(ConfigDataEnvironmentContributor contributor) {
		return contributor.getLocation().toString();
	}

	private ConfigDataEnvironmentContributor createConfigDataEnvironmentContributor(String location) {
		return createConfigDataEnvironmentContributor(location, Collections.emptyMap());
	}

	private ConfigDataEnvironmentContributor createConfigDataEnvironmentContributor(String location,
			Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children) {
		return new TestConfigDataEnvironmentContributor(Kind.ROOT, new TestLocation(location), null, null, null,
				children);
	}

	static class TestConfigDataEnvironmentContributor extends ConfigDataEnvironmentContributor {

		protected TestConfigDataEnvironmentContributor(Kind kind, ConfigDataLocation location,
				PropertySource<?> propertySource, ConfigurationPropertySource configurationPropertySource,
				ConfigDataProperties properties, Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children) {
			super(kind, location, propertySource, configurationPropertySource, properties, children);
		}

	}

	static class TestLocation extends ConfigDataLocation {

		private final String location;

		TestLocation(String location) {
			this.location = location;
		}

		@Override
		public String toString() {
			return this.location;
		}

	}

}
