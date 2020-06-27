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
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author Phillip Webb
 * @since 2.4.0
 */
public class Profiles {

	private static final String[] NONE = {};

	private final String[] active;

	Profiles(Environment environment, Binder binder) {
		this.active = deduceActiveProfiles(environment, binder);
	}

	private String[] deduceActiveProfiles(Environment environment, Binder binder) {
		if (hasExplicitActiveProfiles(environment)) {
			return environment.getActiveProfiles();
		}
		return binder.bind(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, String[].class).orElse(NONE);
	}

	private boolean hasExplicitActiveProfiles(Environment environment) {
		String propertyValue = environment.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		Set<String> activeProfiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));
		if (!StringUtils.hasLength(propertyValue)) {
			return !activeProfiles.isEmpty();
		}
		Set<String> propertyProfiles = StringUtils
				.commaDelimitedListToSet(StringUtils.trimAllWhitespace(propertyValue));
		return !propertyProfiles.equals(activeProfiles);
	}

	/**
	 * Return the active profiles or {@code null} if no profiles have been activated yet.
	 * @return the active profiles
	 */
	String[] getActive() {
		return this.active;
	}

}
