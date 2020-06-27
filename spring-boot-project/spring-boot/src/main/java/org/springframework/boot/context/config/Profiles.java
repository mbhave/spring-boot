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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Provides access to environment profiles that have either been set directly on the
 * {@link Environment} or will be set based on configuration data property values.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class Profiles implements Iterable<String> {

	private static final Set<String> UNSET_ACTIVE = Collections.emptySet();

	private static final Set<String> UNSET_DEFAULT = Collections.singleton("default");

	private final List<String> activeProfiles;

	private final List<String> defaultProfiles;

	private final List<String> all;

	Profiles(Environment environment, Binder binder) {
		this.activeProfiles = asList(get(environment, binder, environment::getActiveProfiles,
				AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, UNSET_ACTIVE));
		this.defaultProfiles = asList(get(environment, binder, environment::getDefaultProfiles,
				AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME, UNSET_DEFAULT));
		this.all = merge(this.activeProfiles, this.defaultProfiles);
	}

	private String[] get(Environment environment, Binder binder, Supplier<String[]> supplier, String propertyName,
			Set<String> unset) {
		String propertyValue = environment.getProperty(propertyName);
		if (hasExplicit(supplier, propertyValue, unset)) {
			return supplier.get();
		}
		return binder.bind(propertyName, String[].class).orElse(StringUtils.toStringArray(unset));
	}

	private boolean hasExplicit(Supplier<String[]> supplier, String propertyValue, Set<String> unset) {
		Set<String> profiles = new LinkedHashSet<>(Arrays.asList(supplier.get()));
		if (!StringUtils.hasLength(propertyValue)) {
			return !unset.equals(profiles);
		}
		Set<String> propertyProfiles = StringUtils
				.commaDelimitedListToSet(StringUtils.trimAllWhitespace(propertyValue));
		return !propertyProfiles.equals(profiles);
	}

	private List<String> merge(List<String> list1, List<String> list2) {
		List<String> merged = new ArrayList<>(list1.size() + list2.size());
		merged.addAll(list1);
		merged.addAll(list2);
		return Collections.unmodifiableList(merged);
	}

	private List<String> asList(String[] array) {
		return Collections.unmodifiableList(Arrays.asList(array));
	}

	@Override
	public Iterator<String> iterator() {
		return this.all.iterator();
	}

	/**
	 * Return the active profiles.
	 * @return the active profiles
	 */
	public List<String> getActive() {
		return this.activeProfiles;
	}

	/**
	 * Return the default profiles.
	 * @return the active profiles
	 */
	public List<String> getDefault() {
		return this.defaultProfiles;
	}

	/**
	 * Return all profiles (both active and default).
	 * @return all profiles
	 */
	public List<String> getAll() {
		return this.all;
	}

}
