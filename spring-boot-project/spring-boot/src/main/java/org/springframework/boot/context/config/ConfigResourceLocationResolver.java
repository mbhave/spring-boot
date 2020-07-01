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

package org.springframework.boot.context.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Is able to resolve standard resource locations.
 *
 * @author Phillip Webb
 */
class ConfigResourceLocationResolver implements ConfigDataLocationResolver<ConfigResourceLocation> {

	private static final String CONFIG_FILE_URL_PREFIX = "configfile:";

	static final String CONFIG_NAME_PROPERTY = "spring.config.name";

	private static final String[] DEFAULT_CONFIG_NAMES = { "application" };

	private static final Resource[] EMPTY_RESOURCES = {};

	private static final Comparator<File> FILE_COMPARATOR = Comparator.comparing(File::getAbsolutePath);

	private static final Pattern URL_PREFIX = Pattern.compile("^([a-zA-Z][a-zA-Z0-9]*?:)(.*$)");

	private final List<PropertySourceLoader> propertySourceLoaders;

	private final ResourceLoader resourceLoader;

	private final String[] configNames;

	ConfigResourceLocationResolver(Log logger, Binder binder) {
		this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class,
				getClass().getClassLoader());
		this.configNames = getConfigNames(binder);
		this.resourceLoader = new DefaultResourceLoader();
	}

	private String[] getConfigNames(Binder binder) {
		String[] configNames = binder.bind(CONFIG_NAME_PROPERTY, String[].class).orElse(DEFAULT_CONFIG_NAMES);
		Arrays.stream(configNames).forEach(this::validateConfigName);
		return configNames;
	}

	private void validateConfigName(String name) {
		Assert.state(!name.contains("*"), "Config name '" + name + "' cannot contain '*'");
	}

	@Override
	public boolean isResolvable(Binder binder, ConfigDataLocation parent, String location) {
		return true;
	}

	@Override
	public List<ConfigResourceLocation> resolve(Binder binder, ConfigDataLocation parent, String location) {
		List<ConfigResourceLocation> configLocations = new ArrayList<>();
		Map<String, PropertySourceLoader> loadableLocations = getLoadableLocations(location);
		for (Map.Entry<String, PropertySourceLoader> file : loadableLocations.entrySet()) {
			String fileName = file.getKey();
			Resource[] resources = getResources(fileName);
			for (Resource resource : resources) {
				if (resource.exists()) {
					String name = getLocationName(fileName, resource);
					PropertySourceLoader loader = file.getValue();
					configLocations.add(new ConfigResourceLocation(name, resource, loader));
				}
			}
		}
		return configLocations;
	}

	private Map<String, PropertySourceLoader> getLoadableLocations(String location) {
		Map<String, PropertySourceLoader> loadableLocations = new LinkedHashMap<>();
		boolean isDirectory = location.endsWith("/");
		if (isDirectory) {
			for (String name : this.configNames) {
				for (PropertySourceLoader loader : this.propertySourceLoaders) {
					String[] fileExtensions = loader.getFileExtensions();
					Arrays.stream(fileExtensions)
							.forEach((extension) -> loadableLocations.put(location + name + "." + extension, loader));
				}
			}
		}
		else {
			for (PropertySourceLoader loader : this.propertySourceLoaders) {
				if (canLoadFileExtension(loader, location)) {
					loadableLocations.put(location, loader);
					break;
				}
			}
		}
		return loadableLocations;
	}

	private Resource[] getResources(String location) {
		try {
			if (location.contains("*")) {
				validateWildcardLocation(location);
				return getResourcesFromPatternLocation(location);
			}
			return new Resource[] { this.resourceLoader.getResource(location) };
		}
		catch (IOException ex) {
			return EMPTY_RESOURCES;
		}
	}

	private boolean canLoadFileExtension(PropertySourceLoader loader, String name) {
		return Arrays.stream(loader.getFileExtensions())
				.anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(name, fileExtension));
	}

	private String getLocationName(String location, Resource resource) {
		if (!location.contains("*")) {
			return location;
		}
		if (resource instanceof FileSystemResource) {
			return ((FileSystemResource) resource).getPath();
		}
		return resource.getDescription();
	}

	@Override
	public List<ConfigDataLocation> resolveProfileSpecific(Binder binder, ConfigDataLocation parent, String location,
			Profiles profiles) {
		return null;
	}

	// private void dunno(ConfigDataLocation parent, String location) {
	// if (location.startsWith(CONFIG_FILE_URL_PREFIX)) {
	// location = location.substring(CONFIG_FILE_URL_PREFIX.length() + 1);
	// }
	// boolean isDirectory = location.endsWith("/");
	// if (location.startsWith("/") || URL_PREFIX.matcher(location).matches()) {
	// //
	// }
	// Resource parentResource = null;
	// parentResource.createRelative(location);
	// }
	//
	// private void dunno() {
	// try {
	// if (location.contains("*")) {
	// return getResourcesFromPatternLocation(location);
	// }
	// return new Resource[] { this.resourceLoader.getResource(location) };
	// }
	// catch (Exception ex) {
	// return EMPTY_RESOURCES;
	// }
	// }

	private Resource[] getResourcesFromPatternLocation(String location) throws IOException {
		String directoryPath = location.substring(0, location.indexOf("*/"));
		Resource resource = this.resourceLoader.getResource(directoryPath);
		File[] files = resource.getFile().listFiles(File::isDirectory);
		if (files != null) {
			String fileName = location.substring(location.lastIndexOf("/") + 1);
			Arrays.sort(files, FILE_COMPARATOR);
			return Arrays.stream(files).map((file) -> file.listFiles((dir, name) -> name.equals(fileName)))
					.filter(Objects::nonNull).flatMap((Function<File[], Stream<File>>) Arrays::stream)
					.map(FileSystemResource::new).toArray(Resource[]::new);
		}
		return EMPTY_RESOURCES;
	}

	private void validateWildcardLocation(String path) {
		if (path.contains("*")) {
			Assert.state(StringUtils.countOccurrencesOf(path, "*") == 1,
					() -> "Search location '" + path + "' cannot contain multiple wildcards");
			String directoryPath = path.substring(0, path.lastIndexOf("/") + 1);
			Assert.state(directoryPath.endsWith("*/"), () -> "Search location '" + path + "' must end with '*/'");
		}
	}

}
