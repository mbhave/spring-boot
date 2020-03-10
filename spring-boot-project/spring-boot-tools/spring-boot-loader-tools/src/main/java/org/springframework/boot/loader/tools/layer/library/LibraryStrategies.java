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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.loader.tools.Layer;
import org.springframework.boot.loader.tools.Library;
import org.springframework.util.StringUtils;

/**
 *
 * A collection of {@link LibraryStrategy library strategies}.
 *
 * @author Madhura Bhave
 * @since 2.3.0
 */
public class LibraryStrategies implements Iterable<LibraryStrategy> {

	private final List<LibraryStrategy> strategies = new ArrayList<>();

	private static final String DEPENDENCIES_LAYER_NAME = "dependencies";

	private static final String SNAPSHOT_LAYER_NAME = "snapshot-dependencies";

	public LibraryStrategies(List<LibraryStrategy> strategies) {
		this.strategies.addAll(strategies);
	}

	@Override
	public Iterator<LibraryStrategy> iterator() {
		return this.strategies.iterator();
	}

	/**
	 * Stream all {@link LibraryStrategies library strategies}.
	 * @return a stream of library strategies
	 */
	public Stream<LibraryStrategy> stream() {
		return this.strategies.stream();
	}

	public static LibraryStrategy defaultStrategy(String id, String layer) {
		switch (id) {
		case SNAPSHOT_LAYER_NAME:
			return new SnapshotDependenciesStrategy(layer);
		case DEPENDENCIES_LAYER_NAME:
			return new DependenciesStrategy(layer);
		}
		throw new IllegalArgumentException("Given id does not match any default strategy.");
	}

	static class DependenciesStrategy implements LibraryStrategy {

		private final Layer layer;

		DependenciesStrategy(String layer) {
			String layerName = (StringUtils.hasText(layer)) ? layer : DEPENDENCIES_LAYER_NAME;
			this.layer = new Layer(layerName);
		}

		@Override
		public Layer getMatchingLayer(Library library) {
			return this.layer;
		}

	}

	static class SnapshotDependenciesStrategy implements LibraryStrategy {

		private final Layer layer;

		SnapshotDependenciesStrategy(String layer) {
			String layerName = (StringUtils.hasText(layer)) ? layer : SNAPSHOT_LAYER_NAME;
			this.layer = new Layer(layerName);
		}

		@Override
		public Layer getMatchingLayer(Library library) {
			return (library.getName().contains("SNAPSHOT.")) ? this.layer : null;
		}

	}

}
