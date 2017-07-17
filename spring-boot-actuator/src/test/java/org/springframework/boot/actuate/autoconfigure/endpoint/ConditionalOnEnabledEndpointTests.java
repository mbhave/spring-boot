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
import org.springframework.boot.test.context.ContextConsumer;
import org.springframework.boot.test.context.ContextLoader;
import org.springframework.boot.test.context.StandardContextLoader;
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

	private final StandardContextLoader contextLoader = ContextLoader.standard();

	@Test
	public void enabledByDefault() {
		this.contextLoader.config(FooConfig.class).load(expectEndpoint("foo", true));
	}

	@Test
	public void disabledViaSpecificProperty() {
		this.contextLoader.config(FooConfig.class).env("endpoints.foo.enabled=false")
				.load(expectEndpoint("foo", false));
	}

	@Test
	public void disabledViaGeneralProperty() {
		this.contextLoader.config(FooConfig.class).env("endpoints.all.enabled=false")
				.load(expectEndpoint("foo", false));
	}

	@Test
	public void enabledOverrideViaSpecificProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.foo.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void enabledOverrideViaSpecificWebProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.foo.enabled=false", "endpoints.foo.web.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void enabledOverrideViaSpecificJmxProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.foo.enabled=false", "endpoints.foo.jmx.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void enabledOverrideViaSpecificAnyProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.foo.enabled=false", "endpoints.foo.web.enabled=false",
						"endpoints.foo.jmx.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void enabledOverrideViaGeneralWebProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.all.web.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void enabledOverrideViaGeneralJmxProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.all.jmx.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void enabledOverrideViaGeneralAnyProperty() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.all.web.enabled=false",
						"endpoints.all.jmx.enabled=true")
				.load(expectEndpoint("foo", true));
	}

	@Test
	public void disabledEvenWithEnabledGeneralProperties() {
		this.contextLoader.config(FooConfig.class)
				.env("endpoints.all.enabled=true", "endpoints.all.web.enabled=true",
						"endpoints.all.jmx.enabled=true", "endpoints.foo.enabled=false")
				.load(expectEndpoint("foo", false));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlag() {
		this.contextLoader.config(BarConfig.class).load(expectEndpoint("bar", false));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlagEvenWithGeneralProperty() {
		this.contextLoader.config(BarConfig.class).env("endpoints.all.enabled=true")
				.load(expectEndpoint("bar", false));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlagEvenWithGeneralWebProperty() {
		this.contextLoader.config(BarConfig.class).env("endpoints.all.web.enabled=true")
				.load(expectEndpoint("bar", false));
	}

	@Test
	public void disabledByDefaultWithAnnotationFlagEvenWithGeneralJmxProperty() {
		this.contextLoader.config(BarConfig.class).env("endpoints.all.jmx.enabled=true")
				.load(expectEndpoint("bar", false));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndSpecificProperty() {
		this.contextLoader.config(BarConfig.class).env("endpoints.bar.enabled=true")
				.load(expectEndpoint("bar", true));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndSpecificWebProperty() {
		this.contextLoader.config(BarConfig.class).env("endpoints.bar.web.enabled=true")
				.load(expectEndpoint("bar", true));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndSpecificJmxProperty() {
		this.contextLoader.config(BarConfig.class).env("endpoints.bar.jmx.enabled=true")
				.load(expectEndpoint("bar", true));
	}

	@Test
	public void enabledOverrideWithAndAnnotationFlagAndAnyProperty() {
		this.contextLoader.config(BarConfig.class)
				.env("endpoints.bar.web.enabled=false", "endpoints.bar.jmx.enabled=true")
				.load(expectEndpoint("bar", true));
	}

	@Test
	public void enabledOnlyWebByDefault() {
		this.contextLoader.config(OnlyWebConfig.class)
				.load(expectEndpoint("onlyweb", true));
	}

	@Test
	public void disabledOnlyWebViaEndpointProperty() {
		this.contextLoader.config(OnlyWebConfig.class)
				.env("endpoints.onlyweb.enabled=false")
				.load(expectEndpoint("onlyweb", false));
	}

	@Test
	public void disabledOnlyWebViaSpecificTechProperty() {
		this.contextLoader.config(OnlyWebConfig.class)
				.env("endpoints.onlyweb.web.enabled=false")
				.load(expectEndpoint("onlyweb", false));
	}

	@Test
	public void enableOverridesOnlyWebViaGeneralJmxPropertyHasNoEffect() {
		this.contextLoader.config(OnlyWebConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.all.jmx.enabled=true")
				.load(expectEndpoint("onlyweb", false));
	}

	@Test
	public void enableOverridesOnlyWebViaSpecificJmxPropertyHasNoEffect() {
		this.contextLoader.config(OnlyWebConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.onlyweb.jmx.enabled=false")
				.load(expectEndpoint("onlyweb", false));
	}

	@Test
	public void enableOverridesOnlyWebViaSpecificWebProperty() {
		this.contextLoader.config(OnlyWebConfig.class)
				.env("endpoints.all.enabled=false", "endpoints.onlyweb.web.enabled=true")
				.load(expectEndpoint("onlyweb", true));
	}

	@Test
	public void disabledOnlyWebEvenWithEnabledGeneralProperties() {
		this.contextLoader.config(OnlyWebConfig.class)
				.env("endpoints.all.enabled=true", "endpoints.all.web.enabled=true",
						"endpoints.onlyweb.enabled=true",
						"endpoints.onlyweb.web.enabled=false")
				.load(expectEndpoint("foo", false));
	}

	@Test
	public void contextFailIfEndpointTypeIsNotDetected() {
		this.contextLoader.config(NonEndpointBeanConfig.class).loadAndFail(ex -> {
			assertThat(ex.getMessage().contains("InvalidConfig.foo"));
		});
	}

	@Test
	public void webExtensionWithEnabledByDefaultEndpoint() {
		this.contextLoader.config(FooWebExtensionConfig.class).load((context) -> {
			assertThat(context.getBeansOfType(FooWebEndpointExtension.class)).hasSize(1);
		});
	}

	@Test
	public void webExtensionWithEnabledByDefaultEndpointCanBeDisabled() {
		StandardContextLoader contextLoader = this.contextLoader
				.config(FooJmxExtensionConfig.class).env("endpoints.foo.enabled=false");
		contextLoader.load((context) -> {
			assertThat(context.getBeansOfType(FooWebEndpointExtension.class)).hasSize(0);
		});
	}

	@Test
	public void jmxExtensionWithEnabledByDefaultEndpoint() {
		this.contextLoader.config(FooJmxExtensionConfig.class).load((context) -> {
			assertThat(context.getBeansOfType(FooJmxEndpointExtension.class)).hasSize(1);
		});
	}

	@Test
	public void jmxExtensionWithEnabledByDefaultEndpointCanBeDisabled() {
		StandardContextLoader contextLoader = this.contextLoader
				.config(FooJmxExtensionConfig.class).env("endpoints.foo.enabled=false");
		contextLoader.load((context) -> {
			assertThat(context.getBeansOfType(FooJmxEndpointExtension.class)).hasSize(0);
		});
	}

	private ContextConsumer expectEndpoint(String id, boolean expected) {
		return context -> assertThat(context.containsBean(id)).isEqualTo(expected);
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
