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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.bind.Binder;

/**
 * Imports {@link ConfigData} by {@link ConfigDataLocationResolver resolving} and
 * {@link ConfigDataLoader loading} imports. {@link ConfigDataLocation locations} are
 * tracked to ensure that they are not imported multiple times.
 *
 * @author Phillip Webb
 */
class ConfigDataImporter {

	private ConfigDataLocationResolvers locationResolvers;

	private ConfigDataLoaders loaders;

	private Set<ConfigDataLocation> loadedLocations = new HashSet<>();

	ConfigDataImporter(Binder binder) {
		this.locationResolvers = new ConfigDataLocationResolvers(binder);
	}

	List<ConfigData> loadImports(ConfigDataActivationContext context, Binder binder, ConfigDataLocation parent,
			List<String> locations) {
		return load(this.locationResolvers.resolveAll(binder, parent, locations, context.getProfiles()));
	}

	private List<ConfigData> load(List<ConfigDataLocation> locations) {
		List<ConfigData> result = new ArrayList<>();
		for (ConfigDataLocation location : locations) {
			if (this.loadedLocations.add(location)) {
				result.add(this.loaders.load(location));
			}
		}
		Collections.reverse(result);
		return result;
	}

}
