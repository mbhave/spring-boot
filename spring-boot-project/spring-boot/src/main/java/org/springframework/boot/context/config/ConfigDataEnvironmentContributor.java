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

import java.util.EnumSet;
import java.util.List;

import org.springframework.core.env.Environment;

/**
 * A single element that may eventually contribute configuration data in some way to the
 * {@link Environment}. Atomic unit of work.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributor {

	private List<String> imports;

	private EnumSet<ImportPhase> consumedImports;

	public ConfigDataEnvironmentContributor() {
	}

	List<String> getImports(ImportPhase phase) {
		return null;
	}

	ConfigDataEnvironmentContributor withConsumedImports(ImportPhase phase) {
		return null;
	}

	enum ImportPhase {

		BEFORE_PROFILE_ACTIVATION,

		AFTER_PROFILE_ACTIVATION;

		/**
		 * @param context
		 * @return
		 */
		static ImportPhase get(ConfigDataActivationContext context) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Auto-generated method stub");
		}

	}

	/**
	 * @param phase
	 * @return
	 */
	public boolean hasUnconsumedImports(ImportPhase phase) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	/**
	 * @param context
	 * @return
	 */
	public boolean isActive(ConfigDataActivationContext context) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	/**
	 * @return
	 */
	public ConfigDataLocation getLocation() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

}
