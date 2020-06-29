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

import java.util.Collection;
import java.util.List;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Is able to resolve standard resource locations.
 *
 * @author Phillip Webb
 */
class ConfigResourceLocationResolver implements ConfigDataLocationResolver {

	/**
	 * The "config name" property name.
	 */
	public static final String NAME_PROPERTY = "spring.config.name";

	private static final String[] DEFAULT_NAMES = { "application" };

	private final List<PropertySourceLoader> propertySourceLoaders;

	ConfigResourceLocationResolver(Binder binder) {
		this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class,
				getClass().getClassLoader());
	}

	@Override
	public List<ConfigDataLocation> resolve(Binder binder, ConfigDataLocation parent, String address) {
		// Deals with pattern searching and ends with '/' etc
		return null;
	}

	@Override
	public List<ConfigDataLocation> resolveProfileSpecificLocations(Binder binder, ConfigDataLocation parent,
			String address, Collection<String> activeProfiles) {
		return null;
	}

}
