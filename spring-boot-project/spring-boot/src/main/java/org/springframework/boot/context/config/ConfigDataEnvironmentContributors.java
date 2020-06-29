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
import java.util.List;

import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.ImportPhase;
import org.springframework.boot.context.properties.bind.Binder;

/**
 * An immutable collection of {@link ConfigDataEnvironmentContributor} instances.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributors {

	ConfigDataEnvironmentContributors(List<ConfigDataEnvironmentContributor> contributors) {
	}

	ConfigDataEnvironmentContributors processImports(ConfigDataImporter importer,
			ConfigDataActivationContext activationContext) {
		ImportPhase phase = ImportPhase.get(activationContext);
		ConfigDataEnvironmentContributors current = this;
		while (true) {
			List<ConfigDataEnvironmentContributor> next = processImports(current, phase, importer, activationContext);
			if (next == null) {
				return current;
			}
			current = new ConfigDataEnvironmentContributors(next);
		}
	}

	private List<ConfigDataEnvironmentContributor> processImports(ConfigDataEnvironmentContributors contributors,
			ImportPhase phase, ConfigDataImporter importer, ConfigDataActivationContext activationContext) {
		for (int i = 0; i < contributors.size(); i++) {
			ConfigDataEnvironmentContributor contributor = contributors.get(i);
			if (contributor.isActive(activationContext) && contributor.hasUnconsumedImports(phase)) {
				List<ConfigData> loaded = importer.loadImports(activationContext, contributors.getBinder(),
						contributor.getLocation(), contributor.getImports(phase));
				List<ConfigDataEnvironmentContributor> result = new ArrayList<>(contributors.size() + 10);
				result.addAll(contributors.getAllBefore(i));
				result.addAll(asContributors(loaded));
				result.add(contributor.withConsumedImports(phase));
				result.addAll(contributors.getAllAfter(i));
				return result;
			}
		}
		return null;
	}

	private List<ConfigDataEnvironmentContributor> asContributors(List<ConfigData> loaded) {
		List<ConfigDataEnvironmentContributor> contributors;
		// FIXME add in reverse
		// Collections.reverse(result);
		return null;
	}

	private ConfigDataEnvironmentContributor get(int i) {
		return null;
	}

	private int size() {
		return -1;
	}

	private List<ConfigDataEnvironmentContributor> getAllBefore(int i) {
		return null;
	}

	private List<ConfigDataEnvironmentContributor> getAllAfter(int i) {
		return null;
	}

	Binder getBinder() {
		return null;
	}

	Binder getBinder(BinderOption... binderOptions) {
		return null;
	}

	enum BinderOption {

		FAIL_ON_BIND_TO_INACTIVE_SOURCE;

	}

}
