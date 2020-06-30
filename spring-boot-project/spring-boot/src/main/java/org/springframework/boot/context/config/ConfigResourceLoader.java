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

import java.io.IOException;
import java.util.List;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * {@link ConfigDataLoader} for {@link Resource} backed locations.
 *
 * @author Phillip Webb
 */
class ConfigResourceLoader implements ConfigDataLoader<ConfigResourceLocation> {

	@Override
	public ConfigData load(ConfigResourceLocation location) throws IOException {
		PropertySourceLoader loader = null;
		String name = null;
		Resource resource = null;
		List<PropertySource<?>> load = loader.load(name, resource);
		// FIXME adapt to ConfigData;
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

}
