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

package org.springframework.boot.maven.layer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.boot.loader.tools.Layer;
import org.springframework.boot.maven.layer.classes.CustomResourceStrategy;
import org.springframework.boot.maven.layer.classes.NameFilter;
import org.springframework.boot.maven.layer.classes.ResourceFilter;
import org.springframework.boot.maven.layer.classes.ResourceStrategies;
import org.springframework.boot.maven.layer.classes.ResourceStrategy;
import org.springframework.boot.maven.layer.library.CoordinateFilter;
import org.springframework.boot.maven.layer.library.CustomLibraryStrategy;
import org.springframework.boot.maven.layer.library.LibraryFilter;
import org.springframework.boot.maven.layer.library.LibraryStrategies;
import org.springframework.boot.maven.layer.library.LibraryStrategy;
import org.springframework.boot.maven.layer.library.LocationFilter;

/**
 * Produces a {@link CustomLayers} based on the given {@link Document}.
 *
 * @author Madhura Bhave
 * @since 2.3.0
 */
public class CustomLayersProvider {

	public CustomLayers getLayers(Document document) {
		Element root = document.getDocumentElement();
		NodeList nl = root.getChildNodes();
		List<Layer> layers = new ArrayList<>();
		LibraryStrategies libraryStrategies = null;
		ResourceStrategies resourceStrategies = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				String nodeName = ele.getNodeName();
				if ("layers".equals(nodeName)) {
					layers.addAll(getLayers(ele));
				}
				if ("libraries".equals(nodeName)) {
					libraryStrategies = getLibraryStrategies(ele.getElementsByTagName("strategy"));
				}
				if ("classes".equals(nodeName)) {
					resourceStrategies = getResourceStrategies(ele.getElementsByTagName("strategy"));
				}
			}
		}
		return new CustomLayers(layers, resourceStrategies, libraryStrategies);
	}

	private LibraryStrategies getLibraryStrategies(NodeList strategies) {
		Map<String, LibraryStrategy> strategy = new LinkedHashMap<>();
		for (int i = 0; i < strategies.getLength(); i++) {
			Node item = strategies.item(i);
			List<LibraryFilter> filters = new ArrayList<>();
			if (item instanceof Element) {
				Element element = (Element) item;
				String id = element.getAttribute("id");
				String layer = (element.getAttribute("layer").isEmpty() ? id : element.getAttribute("layer"));
				if (item.getChildNodes().getLength() == 0) {
					try {
						LibraryStrategy libraryStrategy = LibraryStrategies.defaultStrategy(id, layer);
						strategy.put(id, libraryStrategy);
						continue;
					}
					catch (Exception ex) {
						throw new IllegalArgumentException("No default strategy found for id '" + id
								+ "' and custom strategy definition must not be empty.");
					}
				}
				NodeList filterList = item.getChildNodes();
				for (int k = 0; k < filterList.getLength(); k++) {
					Node filter = filterList.item(k);
					if (filter instanceof Element) {
						List<String> includeList = getPatterns(i, (Element) filter, "include");
						List<String> excludeList = getPatterns(i, (Element) filter, "exclude");
						addLibraryFilter(filters, filter, includeList, excludeList);
					}
				}
				strategy.put(id, new CustomLibraryStrategy(layer, filters));
			}
		}
		return new LibraryStrategies(strategy);
	}

	private void addLibraryFilter(List<LibraryFilter> filters, Node filter, List<String> includeList,
			List<String> excludeList) {
		if ("coordinates".equals(filter.getNodeName())) {
			filters.add(new CoordinateFilter(includeList, excludeList));
		}
		if ("locations".equals(filter.getNodeName())) {
			filters.add(new LocationFilter(includeList, excludeList));
		}
	}

	private ResourceStrategies getResourceStrategies(NodeList strategies) {
		Map<String, ResourceStrategy> strategy = new LinkedHashMap<>();
		for (int i = 0; i < strategies.getLength(); i++) {
			Node item = strategies.item(i);
			List<ResourceFilter> filters = new ArrayList<>();
			if (item instanceof Element) {
				Element element = (Element) item;
				String id = element.getAttribute("id");
				String layer = (element.getAttribute("layer").isEmpty() ? id : element.getAttribute("layer"));
				if (item.getChildNodes().getLength() == 0) {
					try {
						ResourceStrategy libraryStrategy = ResourceStrategies.defaultStrategy(id, layer);
						strategy.put(id, libraryStrategy);
						continue;
					}
					catch (Exception ex) {
						throw new IllegalArgumentException("No default strategy found for id '" + id
								+ "' and custom strategy definition must not be empty.");
					}
				}
				NodeList filterList = item.getChildNodes();
				for (int k = 0; k < filterList.getLength(); k++) {
					Node filter = filterList.item(k);
					if (filter instanceof Element) {
						List<String> includeList = getPatterns(i, (Element) filter, "include");
						List<String> excludeList = getPatterns(i, (Element) filter, "exclude");
						addFilter(filters, filter, includeList, excludeList);
					}
				}
				strategy.putIfAbsent(id, new CustomResourceStrategy(layer, filters));
			}
		}
		return new ResourceStrategies(strategy);
	}

	private void addFilter(List<ResourceFilter> filters, Node filter, List<String> includeList,
			List<String> excludeList) {
		if ("names".equals(filter.getNodeName())) {
			filters.add(new NameFilter(includeList, excludeList));
		}
		if ("locations".equals(filter.getNodeName())) {
			filters.add(new org.springframework.boot.maven.layer.classes.LocationFilter(includeList, excludeList));
		}
	}

	private List<String> getPatterns(int i, Element element, String key) {
		NodeList patterns = element.getElementsByTagName(key);
		List<String> values = new ArrayList<>();
		for (int j = 0; j < patterns.getLength(); j++) {
			Node item = patterns.item(j);
			if (item instanceof Element) {
				values.add(item.getTextContent());
			}
		}
		return values;
	}

	private List<Layer> getLayers(Element element) {
		List<Layer> layers = new ArrayList<>();
		NodeList nl = element.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				String nodeName = ele.getNodeName();
				if ("layer".equals(nodeName)) {
					layers.add(new Layer(ele.getTextContent()));
				}
			}
		}
		return layers;
	}

}
