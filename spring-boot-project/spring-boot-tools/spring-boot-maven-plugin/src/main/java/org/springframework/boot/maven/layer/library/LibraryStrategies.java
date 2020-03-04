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

package org.springframework.boot.maven.layer.library;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.loader.tools.Layer;
import org.springframework.boot.loader.tools.Library;

/**
 *
 * A collection of {@link LibraryStrategy library strategies}.
 *
 * @author Madhura Bhave
 * @since 2.3.0
 */
public class LibraryStrategies implements Iterable<LibraryStrategy> {

	private final Map<String, LibraryStrategy> strategies = new LinkedHashMap<>();

	private static final String DEPENDENCIES_LAYER_NAME = "dependencies";

	private static final String SNAPSHOT_LAYER_NAME = "snapshot-dependencies";

	public LibraryStrategies(Map<String, LibraryStrategy> strategies) {
		this.strategies.putAll(strategies);
	}

	@Override
	public Iterator<LibraryStrategy> iterator() {
		return this.strategies.values().iterator();
	}

	/**
	 * Stream all {@link LibraryStrategies library strategies}.
	 * @return a stream of library strategies
	 */
	public Stream<LibraryStrategy> stream() {
		return this.strategies.values().stream();
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
			String layerName = (layer != null) ? layer : DEPENDENCIES_LAYER_NAME;
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
			String layerName = (layer != null) ? layer : SNAPSHOT_LAYER_NAME;
			this.layer = new Layer(layerName);
		}

		@Override
		public Layer getMatchingLayer(Library library) {
			return (library.getName().contains("SNAPSHOT.")) ? this.layer : null;
		}

	}

}
