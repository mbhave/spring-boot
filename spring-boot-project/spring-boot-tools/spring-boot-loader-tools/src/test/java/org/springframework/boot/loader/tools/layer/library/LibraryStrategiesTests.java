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

package org.springframework.boot.loader.tools.layer.library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.boot.loader.tools.Layer;
import org.springframework.boot.loader.tools.Library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link LibraryStrategies}.
 *
 * @author Madhura Bhave
 */
class LibraryStrategiesTests {

	@Test
	void iteratorShouldReturnLibraryStrategies() {
		LibraryStrategies strategies = createStrategies();
		assertThat(strategies).hasSize(2);
		assertThat(strategies.stream().map((s) -> s.getMatchingLayer(any()).toString()).collect(Collectors.toList()))
				.containsExactly("test-layer-1", "test-layer-2");
	}

	@Test
	void defaultDependenciesStrategyHasDependenciesAsLayerName() {
		LibraryStrategy strategy = LibraryStrategies.defaultStrategy("dependencies", null);
		assertThat(strategy.getMatchingLayer(mock(Library.class)).toString()).isEqualTo("dependencies");
	}

	@Test
	void defaultDependenciesStrategyWithCustomLayerName() {
		LibraryStrategy strategy = LibraryStrategies.defaultStrategy("dependencies", "my-deps");
		assertThat(strategy.getMatchingLayer(mock(Library.class)).toString()).isEqualTo("my-deps");
	}

	@Test
	void defaultSnapshotDependenciesStrategyHasDependenciesAsLayerName() {
		Library library = mock(Library.class);
		given(library.getName()).willReturn("test-SNAPSHOT.jar");
		LibraryStrategy strategy = LibraryStrategies.defaultStrategy("snapshot-dependencies", null);
		assertThat(strategy.getMatchingLayer(library).toString()).isEqualTo("snapshot-dependencies");
	}

	@Test
	void defaultSnapshotDependenciesStrategyWithCustomLayerName() {
		Library library = mock(Library.class);
		given(library.getName()).willReturn("test-SNAPSHOT.jar");
		LibraryStrategy strategy = LibraryStrategies.defaultStrategy("snapshot-dependencies", "my-snapshot-deps");
		assertThat(strategy.getMatchingLayer(library).toString()).isEqualTo("my-snapshot-deps");
	}

	@Test
	void invalidIdForDefaultStrategyShouldThrowException() {
		assertThatIllegalArgumentException().isThrownBy(() -> LibraryStrategies.defaultStrategy("invalid", null));
	}

	private LibraryStrategies createStrategies() {
		List<LibraryStrategy> strategies = new ArrayList<>();
		strategies.add(mockLibraryStrategy("test-layer-1"));
		strategies.add(mockLibraryStrategy("test-layer-2"));
		return new LibraryStrategies(strategies);
	}

	private LibraryStrategy mockLibraryStrategy(String layer) {
		LibraryStrategy strategy = mock(LibraryStrategy.class);
		given(strategy.getMatchingLayer(any())).willReturn(new Layer(layer));
		return strategy;
	}

}
