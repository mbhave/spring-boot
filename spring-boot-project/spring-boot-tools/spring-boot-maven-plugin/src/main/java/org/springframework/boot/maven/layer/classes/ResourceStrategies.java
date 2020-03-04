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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.loader.tools.Layer;

/**
 * A collection of {@link ResourceStrategy resource strategies}.
 *
 * @author Madhura Bhave
 * @since 2.3.0
 */
public class ResourceStrategies implements Iterable<ResourceStrategy> {

	private final Map<String, ResourceStrategy> strategies = new LinkedHashMap<>();

	private static final String APPLICATION_LAYER_NAME = "application";

	private static final String RESOURCES_LAYER_NAME = "resources";

	public ResourceStrategies(Map<String, ResourceStrategy> strategies) {
		this.strategies.putAll(strategies);
	}

	@Override
	public Iterator<ResourceStrategy> iterator() {
		return this.strategies.values().iterator();
	}

	/**
	 * Stream all {@link ResourceStrategy resource strategies}.
	 * @return a stream of resource strategies
	 */
	public Stream<ResourceStrategy> stream() {
		return this.strategies.values().stream();
	}

	public static ResourceStrategy defaultStrategy(String id, String layer) {
		switch (id) {
		case APPLICATION_LAYER_NAME:
			return new ApplicationStrategy(layer);
		case RESOURCES_LAYER_NAME:
			return new ResourcesStrategy(layer);
		}
		throw new IllegalArgumentException("Given id does not match any default strategy.");
	}

	static class ApplicationStrategy implements ResourceStrategy {

		private final Layer layer;

		ApplicationStrategy(String layer) {
			String layerName = (layer != null) ? layer : APPLICATION_LAYER_NAME;
			this.layer = new Layer(layerName);
		}

		@Override
		public Layer getMatchingLayer(String resourceName) {
			return this.layer;
		}

	}

	static class ResourcesStrategy implements ResourceStrategy {

		private final Layer layer;

		private static final String[] RESOURCE_LOCATIONS = { "META-INF/resources/", "resources/", "static/",
				"public/" };

		ResourcesStrategy(String layer) {
			String layerName = (layer != null) ? layer : RESOURCES_LAYER_NAME;
			this.layer = new Layer(layerName);
		}

		@Override
		public Layer getMatchingLayer(String name) {
			if (!isClassFile(name) && isInResourceLocation(name)) {
				return this.layer;
			}
			return null;
		}

		private boolean isClassFile(String name) {
			return name.endsWith(".class");
		}

		private boolean isInResourceLocation(String name) {
			for (String resourceLocation : RESOURCE_LOCATIONS) {
				if (name.startsWith(resourceLocation)) {
					return true;
				}
			}
			return false;
		}

	}

}
