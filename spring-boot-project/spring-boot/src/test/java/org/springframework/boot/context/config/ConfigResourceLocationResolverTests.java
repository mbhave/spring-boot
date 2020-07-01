/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package org.springframework.boot.context.config;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ConfigResourceLocationResolver}.
 *
 * @author Madhura Bhave
 */
public class ConfigResourceLocationResolverTests {

	private ConfigResourceLocationResolver resolver;

	private Binder binder = mock(Binder.class);

	private MockEnvironment environment;

	@BeforeEach
	void setup() {
		this.environment = new MockEnvironment();
		this.binder = Binder.get(this.environment);
		this.resolver = new ConfigResourceLocationResolver(null, this.binder);
	}

	@Test
	void isResolvableShouldAlwaysReturnTrue() {
		assertThat(this.resolver.isResolvable(this.binder, null, "test")).isTrue();
	}

	@Test
	void resolveWhenLocationIsDirectoryShouldResolveAllMatchingFilesInDirectory() {
		String location = "classpath:/configdata/properties/";
		List<ConfigResourceLocation> locations = this.resolver.resolve(this.binder, null, location);
		assertThat(locations.size()).isEqualTo(1);
		assertThat(locations.get(0).getLocation()).isEqualTo("classpath:/configdata/properties/application.properties");
	}

	@Test
	void resolveWhenLocationIsFileShouldResolveFile() {
		String location = "file:src/test/resources/configdata/properties/application.properties";
		List<ConfigResourceLocation> locations = this.resolver.resolve(this.binder, null, location);
		assertThat(locations.size()).isEqualTo(1);
		assertThat(locations.get(0).getLocation())
				.isEqualTo("file:src/test/resources/configdata/properties/application.properties");
	}

	@Test
	void directoryLocationsWithWildcardShouldHaveWildcardAsLastCharacterBeforeSlash() {
		String location = "file:src/test/resources/*/config/";
		assertThatIllegalStateException().isThrownBy(() -> this.resolver.resolve(this.binder, null, location))
				.withMessageStartingWith("Search location '").withMessageEndingWith("' must end with '*/'");
	}

	@Test
	void configNameCannotContainWildcard() {
		this.environment.setProperty("spring.config.name", "*/application");
		assertThatIllegalStateException().isThrownBy(() -> new ConfigResourceLocationResolver(null, this.binder))
				.withMessageStartingWith("Config name '").withMessageEndingWith("' cannot contain '*'");
	}

	@Test
	void directoryLocationsWithMultipleWildcardsShouldThrowException() {
		String location = "file:src/test/resources/config/**/";
		assertThatIllegalStateException().isThrownBy(() -> this.resolver.resolve(this.binder, null, location))
				.withMessageStartingWith("Search location '")
				.withMessageEndingWith("' cannot contain multiple wildcards");
	}

	@Test
	void locationsWithWildcardDirectoriesShouldRestrictToOneLevelDeep() {
		String location = "file:src/test/resources/config/*/";
		this.environment.setProperty("spring.config.name", "testproperties");
		this.resolver = new ConfigResourceLocationResolver(null, this.binder);
		List<ConfigResourceLocation> locations = this.resolver.resolve(this.binder, null, location);
		assertThat(locations.size()).isEqualTo(3);
		assertThat(locations).extracting("location")
				.contains("src/test/resources/config/1-first/testproperties.properties");
		assertThat(locations).extracting("location")
				.contains("src/test/resources/config/2-second/testproperties.properties");
		assertThat(locations).extracting("location")
				.doesNotContain("src/test/resources/config/nested/3-third/testproperties.properties");
	}

	@Test
	void locationsWithWildcardDirectoriesShouldSortAlphabeticallyBasedOnAbsolutePath() {
		String location = "file:src/test/resources/config/*/";
	}

	@Test
	void locationsWithWildcardFilesShouldLoadAllFilesThatMatch() {
		String location = "file:src/test/resources/config/*/testproperties.properties";
		List<ConfigResourceLocation> locations = this.resolver.resolve(this.binder, null, location);
		assertThat(locations.size()).isEqualTo(3);
		assertThat(locations).extracting("location")
				.contains("src/test/resources/config/1-first/testproperties.properties");
		assertThat(locations).extracting("location")
				.contains("src/test/resources/config/2-second/testproperties.properties");
		assertThat(locations).extracting("location")
				.doesNotContain("src/test/resources/config/nested/3-third/testproperties.properties");
	}

	@Test
	void resolveWhenLocationHasMatchingUrlPrefixShouldResolve() {

	}

	@Test
	void resolveWhenLocationIsRelativeShouldResolve() {

	}

	@Test
	void resolveWhenLocationIsRelativeAndParentNullShouldReturnNull() {

	}

}