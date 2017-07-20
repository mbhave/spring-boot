/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint;

import org.junit.Test;

import org.springframework.boot.endpoint.Endpoint;
import org.springframework.boot.endpoint.EndpointType;
import org.springframework.boot.endpoint.jmx.JmxEndpointExtension;
import org.springframework.boot.endpoint.web.WebEndpointExtension;
import org.springframework.boot.test.context.ApplicationContextTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConditionalOnEnabledEndpoint}.
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
public class ConditionalOnEnabledEndpointTests {

	private final ApplicationContextTester context = new ApplicationContextTester();

	@Test
	public void enabledByDefault() {
		this.context.withUserConfiguration(FooConfig.class)
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void disabledViaSpecificProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.foo.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("foo"));
	}

	@Test
	public void disabledViaGeneralProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.all.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("foo"));
	}

	@Test
	public void enabledOverrideViaSpecificProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.foo.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void enabledOverrideViaSpecificWebProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.foo.enabled=false",
						"endpoints.foo.web.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void enabledOverrideViaSpecificJmxProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.foo.enabled=false",
						"endpoints.foo.jmx.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void enabledOverrideViaSpecificAnyProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.foo.enabled=false",
						"endpoints.foo.web.enabled=false",
						"endpoints.foo.jmx.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void enabledOverrideViaGeneralWebProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.all.web.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void enabledOverrideViaGeneralJmxProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.all.jmx.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void enabledOverrideViaGeneralAnyProperty() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.all.web.enabled=false",
						"endpoints.all.jmx.enabled=true")
				.run((context) -> assertThat(context).hasBean("foo"));
	}

	@Test
	public void disabledEvenWithEnabledGeneralProperties() {
		this.context.withUserConfiguration(FooConfig.class)
				.withPropertyValues("endpoints.all.enabled=true",
						"endpoints.all.web.enabled=true",
						"endpoints.all.jmx.enabled=true", "endpoints.foo.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("foo"));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlag() {
		this.context.withUserConfiguration(BarConfig.class)
				.run((context) -> assertThat(context).doesNotHaveBean("bar"));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlagEvenWithGeneralProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.all.enabled=true")
				.run((context) -> assertThat(context).doesNotHaveBean("bar"));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlagEvenWithGeneralWebProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.all.web.enabled=true")
				.run((context) -> assertThat(context).doesNotHaveBean("bar"));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlagEvenWithGeneralJmxProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.all.jmx.enabled=true")
				.run((context) -> assertThat(context).doesNotHaveBean("bar"));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndSpecificProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.bar.enabled=true")
				.run((context) -> assertThat(context).hasBean("bar"));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndSpecificWebProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.bar.web.enabled=true")
				.run((context) -> assertThat(context).hasBean("bar"));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndSpecificJmxProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.bar.jmx.enabled=true")
				.run((context) -> assertThat(context).hasBean("bar"));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndAnyProperty() {
		this.context.withUserConfiguration(BarConfig.class)
				.withPropertyValues("endpoints.bar.web.enabled=false",
						"endpoints.bar.jmx.enabled=true")
				.run((context) -> assertThat(context).hasBean("bar"));
	}

	@Test
	public void enabledOnlyWebByDefault() {
		this.context.withUserConfiguration(OnlyWebConfig.class)
				.run((context) -> assertThat(context).hasBean("onlyweb"));
	}

	@Test
	public void disabledOnlyWebViaEndpointProperty() {
		this.context.withUserConfiguration(OnlyWebConfig.class)
				.withPropertyValues("endpoints.onlyweb.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("onlyweb"));
	}

	@Test
	public void disabledOnlyWebViaSpecificTechProperty() {
		this.context.withUserConfiguration(OnlyWebConfig.class)
				.withPropertyValues("endpoints.onlyweb.web.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("onlyweb"));
	}

	@Test
	public void enableOverridesOnlyWebViaGeneralJmxPropertyHasNoEffect() {
		this.context.withUserConfiguration(OnlyWebConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.all.jmx.enabled=true")
				.run((context) -> assertThat(context).doesNotHaveBean("onlyweb"));
	}

	@Test
	public void enableOverridesOnlyWebViaSpecificJmxPropertyHasNoEffect() {
		this.context.withUserConfiguration(OnlyWebConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.onlyweb.jmx.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("onlyweb"));
	}

	@Test
	public void enableOverridesOnlyWebViaSpecificWebProperty() {
		this.context.withUserConfiguration(OnlyWebConfig.class)
				.withPropertyValues("endpoints.all.enabled=false",
						"endpoints.onlyweb.web.enabled=true")
				.run((context) -> assertThat(context).hasBean("onlyweb"));
	}

	@Test
	public void disabledOnlyWebEvenWithEnabledGeneralProperties() {
		this.context.withUserConfiguration(OnlyWebConfig.class).withPropertyValues(
				"endpoints.all.enabled=true", "endpoints.all.web.enabled=true",
				"endpoints.onlyweb.enabled=true", "endpoints.onlyweb.web.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean("foo"));
	}

	@Test
	public void contextFailIfEndpointTypeIsNotDetected() {
		this.context.withUserConfiguration(NonEndpointBeanConfig.class)
				.run((context) -> assertThat(context).getFailure()
						.hasMessageContaining("InvalidConfig.foo"));
	}

	@Test
	public void webExtensionWithEnabledByDefaultEndpoint() {
		this.context.withUserConfiguration(FooWebExtensionConfig.class).run((context) -> {
			assertThat(context).hasSingleBean(FooWebEndpointExtension.class);
		});
	}

	@Test
	public void webExtensionWithEnabledByDefaultEndpointCanBeDisabled() {
		this.context.withUserConfiguration(FooJmxExtensionConfig.class)
				.withPropertyValues("endpoints.foo.enabled=false")
				.run((context) -> assertThat(context)
						.hasSingleBean(FooWebEndpointExtension.class));
	}

	@Test
	public void jmxExtensionWithEnabledByDefaultEndpoint() {
		this.context.withUserConfiguration(FooJmxExtensionConfig.class).run((context) -> {
			assertThat(context.getBeansOfType(FooJmxEndpointExtension.class)).hasSize(1);
		});
	}

	@Test
	public void jmxExtensionWithEnabledByDefaultEndpointCanBeDisabled() {
		this.context.withUserConfiguration(FooJmxExtensionConfig.class)
				.withPropertyValues("endpoints.foo.enabled=false")
				.run((context) -> assertThat(context)
						.hasSingleBean(FooJmxEndpointExtension.class));
	}

	@Configuration
	static class FooConfig {

		@Bean
		@ConditionalOnEnabledEndpoint
		public FooEndpoint foo() {
			return new FooEndpoint();
		}

	}

	@Endpoint(id = "foo")
	static class FooEndpoint {

	}

	@Configuration
	static class BarConfig {

		@Bean
		@ConditionalOnEnabledEndpoint
		public BarEndpoint bar() {
			return new BarEndpoint();
		}

	}

	@Endpoint(id = "bar", types = { EndpointType.WEB,
			EndpointType.JMX }, enabledByDefault = false)
	static class BarEndpoint {

	}

	@Configuration
	static class OnlyWebConfig {

		@Bean(name = "onlyweb")
		@ConditionalOnEnabledEndpoint
		public OnlyWebEndpoint onlyWeb() {
			return new OnlyWebEndpoint();
		}

	}

	@Endpoint(id = "onlyweb", types = EndpointType.WEB)
	static class OnlyWebEndpoint {

	}

	@Configuration
	static class NonEndpointBeanConfig {

		@Bean
		@ConditionalOnEnabledEndpoint
		public String foo() {
			return "endpoint type cannot be detected";
		}

	}

	@JmxEndpointExtension(endpoint = FooEndpoint.class)
	static class FooJmxEndpointExtension {

	}

	@Configuration
	static class FooJmxExtensionConfig {

		@Bean
		@ConditionalOnEnabledEndpoint
		FooJmxEndpointExtension fooJmxEndpointExtension() {
			return new FooJmxEndpointExtension();
		}

	}

	@WebEndpointExtension(endpoint = FooEndpoint.class)
	static class FooWebEndpointExtension {

	}

	@Configuration
	static class FooWebExtensionConfig {

		@Bean
		@ConditionalOnEnabledEndpoint
		FooWebEndpointExtension fooJmxEndpointExtension() {
			return new FooWebEndpointExtension();
		}

	}

}
