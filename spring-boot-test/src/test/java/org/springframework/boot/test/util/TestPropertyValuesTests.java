package org.springframework.boot.test.util;

import org.junit.Test;

import org.springframework.boot.test.util.TestPropertyValues.Type;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestPropertyValues}.
 *
 * @author Madhura Bhave
 */
public class TestPropertyValuesTests {

	private final ConfigurableEnvironment environment = new StandardEnvironment();

	@Test
	public void addEnvironmentShouldAttachConfigurationPropertySource() throws Exception {
		TestPropertyValues.of("foo.bar=baz").applyTo(this.environment);
		PropertySource<?> source = this.environment.getPropertySources().get("configurationProperties");
		assertThat(source).isNotNull();
	}

	@Test
	public void addPairsToDefaultPropertySource() throws Exception {
		TestPropertyValues.of("foo.bar=baz", "hello.world=hi").applyTo(this.environment);
		assertThat(this.environment.getProperty("foo.bar")).isEqualTo("baz");
		assertThat(this.environment.getProperty("hello.world")).isEqualTo("hi");
	}

	@Test
	public void addPairsToSystemSystemPropertySource() throws Exception {
		TestPropertyValues.of("FOO_BAR=BAZ").applyTo(this.environment, Type.SYSTEM);
		assertThat(this.environment.getProperty("foo.bar")).isEqualTo("BAZ");
	}

	@Test
	public void addWithSpecificName() throws Exception {
		TestPropertyValues.of("foo.bar=baz").applyTo(this.environment, Type.MAP, "other");
		assertThat(this.environment.getPropertySources().get("other")).isNotNull();
		assertThat(this.environment.getProperty("foo.bar")).isEqualTo("baz");
	}

	@Test
	public void addWithExistingNameAndDifferentTypeShouldOverrideExistingOne() throws Exception {
		TestPropertyValues.of("foo.bar=baz").applyTo(this.environment, Type.MAP, "other");
		TestPropertyValues.of("FOO_BAR=BAZ").applyTo(this.environment, Type.SYSTEM, "other");
		assertThat(this.environment.getPropertySources().get("other")).isNotNull();
		assertThat(this.environment.getProperty("foo.bar")).isEqualTo("BAZ");
	}

	@Test
	public void addWithExistingNameAndSameTypeShouldOverrideExisting() throws Exception {
		TestPropertyValues.of("foo.bar=baz").applyTo(this.environment, Type.MAP);
		TestPropertyValues.of("foo.bar=new").applyTo(this.environment, Type.MAP);
		assertThat(this.environment.getProperty("foo.bar")).isEqualTo("new");
	}

	@Test
	public void andShouldChainAndAddSingleKeyValue() throws Exception {
		TestPropertyValues.of("foo.bar=baz").and("hello.world", "hi").and("bling.blah", "bing")
				.applyTo(this.environment, Type.MAP);
		assertThat(this.environment.getProperty("foo.bar")).isEqualTo("baz");
		assertThat(this.environment.getProperty("hello.world")).isEqualTo("hi");
		assertThat(this.environment.getProperty("bling.blah")).isEqualTo("bing");
	}
}