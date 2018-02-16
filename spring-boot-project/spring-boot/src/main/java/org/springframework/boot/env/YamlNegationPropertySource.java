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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * {@link PropertySource} created from a YAML document that keeps track of
 * the negated profiles that the document should match. It checks the Spring Environment's
 * active profiles to determine if the property is present or not.
 *
 * @author Madhura Bhave
 */
public class YamlNegationPropertySource
		extends OriginTrackedMapPropertySource {
	private static final String[] EMPTY_STRING_ARRAY = {};

	private final Environment environment;
	private final Set<String> negatedProfiles;

	YamlNegationPropertySource(String name,
			Map<String, Object> source, Environment environment,
			Set<String> negatedProfiles) {
		super(name, source);
		this.environment = environment;
		this.negatedProfiles = negatedProfiles;
		source.remove(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
	}

	YamlNegationPropertySource withName(String name) {
		return new YamlNegationPropertySource(name, this.source,
				this.environment, this.negatedProfiles);
	}

	@Override
	public Object getProperty(String name) {
		if (AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME.equals(name)) {
			return null;
		}
		return (isActive() ? super.getProperty(name) : null);
	}

	@Override
	public boolean containsProperty(String name) {
		if (AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME.equals(name)) {
			return false;
		}
		return (isActive() && super.containsProperty(name));
	}

	@Override
	public String[] getPropertyNames() {
		return (isActive() ? super.getPropertyNames() : EMPTY_STRING_ARRAY);
	}

	private boolean isActive() {
		return Collections.disjoint(this.negatedProfiles,
				Arrays.asList(this.environment.getActiveProfiles()));
	}
}
