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
import org.springframework.core.env.Environment;

/**
 * A collection of {@link ConfigDataEnvironmentContributor} instances that will ultimately
 * contribute to the {@link Environment}.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributors {

	/**
	 * @param contributors
	 */
	public ConfigDataEnvironmentContributors(List<ConfigDataEnvironmentContributor> contributors) {
		// TODO Auto-generated constructor stub
	}

	public ConfigDataEnvironmentContributors processImports(ConfigDataImporter importer,
			ConfigDataActivationContext context) {
		ImportPhase phase = ImportPhase.get(context);
		ConfigDataEnvironmentContributors current = this;
		while (true) {
			List<ConfigDataEnvironmentContributor> next = null;
			for (int i = 0; i < current.size(); i++) {
				ConfigDataEnvironmentContributor contributor = current.get(i);
				if (contributor.isActive(context) && contributor.hasUnconsumedImports(phase)) {
					next = new ArrayList<>(current.size() + 10);
					next.addAll(current.getAllBefore(i));
					next.addAll(importer.loadImports(context, current.getBinder(), contributor.getLocation(),
							contributor.getImports(phase)));
					next.add(contributor.withConsumedImports(phase));
					next.addAll(current.getAllAfter(i));
					break;
				}
			}
			if (next == null) {
				return current;
			}
			current = new ConfigDataEnvironmentContributors(next);
		}
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

	Binder getBinder(boolean failOnBindToInactiveSource) {
		return null;
	}

}
