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

package org.springframework.boot.maven.layer.classes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.boot.loader.tools.Layer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ResourceStrategies}.
 *
 * @author Madhura Bhave
 */
public class ResourceStrategiesTests {

	@Test
	void iteratorShouldReturnResourceStrategies() {
		ResourceStrategies strategies = createStrategies();
		assertThat(strategies).hasSize(2);
		assertThat(strategies.stream().map((s) -> s.getMatchingLayer(any()).toString()).collect(Collectors.toList()))
				.containsExactly("test-layer-1", "test-layer-2");
	}

	@Test
	void defaultApplicationStrategyHasApplicationAsLayerName() {
		ResourceStrategy strategy = ResourceStrategies.defaultStrategy("application", null);
		assertThat(strategy.getMatchingLayer("test").toString()).isEqualTo("application");
	}

	@Test
	void defaultApplicationStrategyWithCustomLayerName() {
		ResourceStrategy strategy = ResourceStrategies.defaultStrategy("application", "my-app");
		assertThat(strategy.getMatchingLayer("test").toString()).isEqualTo("my-app");
	}

	@Test
	void defaultSnapshotDependenciesStrategyHasDependenciesAsLayerName() {
		ResourceStrategy strategy = ResourceStrategies.defaultStrategy("resources", null);
		assertThat(strategy.getMatchingLayer("resources/test.css").toString()).isEqualTo("resources");
	}

	@Test
	void defaultSnapshotDependenciesStrategyWithCustomLayerName() {
		ResourceStrategy strategy = ResourceStrategies.defaultStrategy("resources", "my-resources");
		assertThat(strategy.getMatchingLayer("resources/test.css").toString()).isEqualTo("my-resources");
	}

	@Test
	void invalidIdForDefaultStrategyShouldThrowException() {
		assertThatIllegalArgumentException().isThrownBy(() -> ResourceStrategies.defaultStrategy("invalid", null));
	}

	private ResourceStrategies createStrategies() {
		Map<String, ResourceStrategy> strategies = new LinkedHashMap<>();
		strategies.put("layer-1", mockResourceStrategy("test-layer-1"));
		strategies.put("layer-2", mockResourceStrategy("test-layer-2"));
		return new ResourceStrategies(strategies);
	}

	private ResourceStrategy mockResourceStrategy(String layer) {
		ResourceStrategy strategy = mock(ResourceStrategy.class);
		given(strategy.getMatchingLayer(any())).willReturn(new Layer(layer));
		return strategy;
	}

}
