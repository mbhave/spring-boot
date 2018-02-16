/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.env;

import java.util.List;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * Strategy to load '.yml' (or '.yaml') files into a {@link PropertySource}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class YamlPropertySourceLoader implements PropertySourceLoader, EnvironmentAware {

	private Environment environment;

	@Override
	public String[] getFileExtensions() {
		return new String[] { "yml", "yaml" };
	}

	@Override
	public PropertySource<?> load(String name, Resource resource, String profile) {
		if (!ClassUtils.isPresent("org.yaml.snakeyaml.Yaml", null)) {
			throw new IllegalStateException("Attempted to load " + name
					+ " but snakeyaml was not found on the classpath");
		}
		List<MapPropertySource> propertySources = new OriginTrackedYamlLoader(resource, profile, environment)
				.load();
		if (!propertySources.isEmpty()) {
			if (propertySources.size() == 1) {
				return renamePropertySource(propertySources.get(0), name);
			}
			CompositePropertySource result = new CompositePropertySource(name);
			for (MapPropertySource documentPropertySource : propertySources) {
				result.addPropertySource(documentPropertySource);
			}
			return result;
		}
		return null;
	}

	private MapPropertySource renamePropertySource(MapPropertySource ps, String name) {
		if (ps instanceof YamlNegationPropertySource) {
			return ((YamlNegationPropertySource) ps).withName(name);
		}
		return new MapPropertySource(name, ps.getSource());
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
