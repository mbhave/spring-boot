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

import org.springframework.core.ResolvableType;

/**
 * Strategy class that can be used used to load {@link ConfigData} instances from a
 * {@link ConfigDataLocation location}.
 *
 * @author Phillip Webb
 * @param <L> the location type
 */
public interface ConfigDataLoader<L extends ConfigDataLocation> {

	/**
	 * Returns if the specified location can be loaded by this instance.
	 * @param location the location to check.
	 * @return if the location is supported by this loader
	 */
	default boolean isLoadable(L location) {
		return ResolvableType.forClass(getClass()).as(ConfigDataLoader.class).resolve().isInstance(location);
	}

	/**
	 * Load {@link ConfigData} for the given location.
	 * @param location the location to load
	 * @return the loaded config data or {@code null} if the location should be skipped
	 */
	ConfigData load(L location);

}
