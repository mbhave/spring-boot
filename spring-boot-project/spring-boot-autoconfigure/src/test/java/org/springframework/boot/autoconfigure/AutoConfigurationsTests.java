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

package org.springframework.boot.autoconfigure;

import org.junit.Test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.annotation.Configurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AutoConfigurations}.
 *
 * @author Phillip Webb
 */
public class AutoConfigurationsTests {

	@Test
	public void ofShouldCreateOrderedConfigurations() {
		Configurations configurations = AutoConfigurations.of(AutoConfigureA.class,
				AutoConfigureB.class);
		assertThat(Configurations.getClasses(configurations))
				.containsExactly(AutoConfigureB.class, AutoConfigureA.class);
	}

	@Test
	public void contextShouldNotHaveBeenIfConfigurationClassConditionalDoesNotMatch() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
				.withClassLoader(new FilteredClassLoader(Foo.class))
				.withConfiguration(AutoConfigurations.of(ConditionalConfig.class));
		contextRunner.run((context -> assertThat(context).doesNotHaveBean(ConditionalConfig.class)));
	}

	@AutoConfigureAfter(AutoConfigureB.class)
	public static class AutoConfigureA {

	}

	public static class AutoConfigureB {

	}

	@Configuration
	@ConditionalOnClass(Foo.class)
	static class ConditionalConfig {

	}

	static class Foo {

	}

}
