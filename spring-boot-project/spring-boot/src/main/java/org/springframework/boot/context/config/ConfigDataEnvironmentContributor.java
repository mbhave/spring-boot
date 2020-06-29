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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.env.Environment;

/**
 * A single element that may directly or indirectly contribute configuration data to the
 * {@link Environment}. Contributors are immutable and will be replaced with new versions
 * as they are consumed.
 * <p>
 * The following types of contributor are expected:
 * <ul>
 * <li>A wrapper around an existing {@link Environment} property source that is used to
 * provide values for use when {@link ConfigDataLocationResolver resolving}
 * locations.</li>
 * <li>A wrapper around a {@link ConfigData} property source that provides access to
 * values and exposes import/condition properties.</li>
 * <li>A set of initial imports that need to be processed.</li>
 * </ul>
 * <p>
 * Contributors may expose a set of imports that should be processed. There are two
 * distinct import phases:
 * <ul>
 * <li>{@link ImportPhase#BEFORE_PROFILE_ACTIVATION Before} profiles have been
 * activated.</li>
 * <li>{@link ImportPhase#AFTER_PROFILE_ACTIVATION After} profiles have been
 * activated.</li>
 * </ul>
 * In each phase <em>all</em> imports will be resolved before they are loaded.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributor {

	private final List<String> imports;

	private final Set<ImportPhase> consumedImports;

	/**
	 * Create a new {@link ConfigDataEnvironmentContributors} that provides imports
	 * without and properties.
	 * @param imports the imports provided
	 */
	ConfigDataEnvironmentContributor(String... imports) {
		this.imports = Collections.unmodifiableList(Arrays.asList(imports));
		this.consumedImports = Collections.unmodifiableSet(EnumSet.noneOf(ImportPhase.class));
	}

	private ConfigDataEnvironmentContributor(List<String> imports, Set<ImportPhase> consumedImports) {
		this.imports = imports;
		this.consumedImports = consumedImports;
	}

	/**
	 * Return the {@link ConfigDataLocation} of the contributor or {@code null} the
	 * contributor is not backed by {@link ConfigData}.
	 * @return the config data location
	 */
	ConfigDataLocation getLocation() {
		return null;
	}

	/**
	 * Returns unconsumed imports in the given phase. If the phase has already been
	 * consumed then an empty list is returned.
	 * @param phase the import phase
	 * @return the imports
	 */
	List<String> getImports(ImportPhase phase) {
		return hasUnconsumedImports(phase) ? this.imports : Collections.emptyList();
	}

	/**
	 * Return {@code true} if there are unconsumed imports for the given phase.
	 * @param phase the phase to check
	 * @return if there are unconsumed imports
	 */
	boolean hasUnconsumedImports(ImportPhase phase) {
		return !this.imports.isEmpty() && !this.consumedImports.contains(phase);
	}

	boolean isActive(ConfigDataActivationContext activationContext) {
		return true;
	}

	/**
	 * Return a new {@link ConfigDataEnvironmentContributors} from this one with imports
	 * consumed in the given phase.
	 * @param phase the phase consumed
	 * @return a new {@link ConfigDataEnvironmentContributors} instance
	 */
	ConfigDataEnvironmentContributor withConsumedImports(ImportPhase phase) {
		Set<ImportPhase> consumedImports = EnumSet.noneOf(ImportPhase.class);
		consumedImports.addAll(this.consumedImports);
		consumedImports.add(phase);
		return new ConfigDataEnvironmentContributor(this.imports, consumedImports);
	}

	/**
	 * Import phases that can be used when obtaining imports.
	 */
	enum ImportPhase {

		/**
		 * The phase before profiles have been activated.
		 */
		BEFORE_PROFILE_ACTIVATION,

		/**
		 * The phase after profiles have been activated.
		 */
		AFTER_PROFILE_ACTIVATION;

		/**
		 * Return the {@link ImportPhase} based on the given activation context.
		 * @param activationContext the activation context
		 * @return the import phase
		 */
		static ImportPhase get(ConfigDataActivationContext activationContext) {
			return (activationContext.getProfiles() != null) ? AFTER_PROFILE_ACTIVATION : BEFORE_PROFILE_ACTIVATION;
		}

	}

}
