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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A collection of {@link ConfigDataLocationResolver} instances loaded via
 * {@code spring.factories}.
 *
 * @author Phillip Webb
 */
class ConfigDataLocationResolvers {

	private final List<ConfigDataLocationResolver> resolvers;

	/**
	 * Create a new {@link ConfigDataLocationResolvers} instance.
	 * @param binder {@link Binder} providing values from the initial {@link Environment}
	 */
	ConfigDataLocationResolvers(Binder binder) {
		this(binder, SpringFactoriesLoader.loadFactoryNames(ConfigDataLocationResolver.class, null));
	}

	ConfigDataLocationResolvers(Binder binder, List<String> names) {
		List<ConfigDataLocationResolver> resolvers = new ArrayList<>(names.size());
		for (String name : names) {
			resolvers.add(createResolver(name, binder));
		}
		AnnotationAwareOrderComparator.sort(resolvers);
		this.resolvers = Collections.unmodifiableList(resolvers);
	}

	@SuppressWarnings("unchecked")
	private ConfigDataLocationResolver createResolver(String name, Binder binder) {
		try {
			Class<?> type = ClassUtils.forName(name, null);
			Assert.isAssignable(ConfigDataLocationResolver.class, type);
			return createResolver(binder, (Class<? extends ConfigDataLocationResolver>) type);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException("Unable to instantiate ConfigDataLocationResolver [" + name + "]", ex);
		}
	}

	private ConfigDataLocationResolver createResolver(Binder binder, Class<? extends ConfigDataLocationResolver> type)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		try {
			return ReflectionUtils.accessibleConstructor(type, Binder.class).newInstance(binder);
		}
		catch (NoSuchMethodException ex) {
			return ReflectionUtils.accessibleConstructor(type).newInstance();
		}
	}

	List<ConfigDataLocation> resolveAll(Binder binder, ConfigDataLocation parent, List<String> locations,
			Profiles profiles) {
		List<ConfigDataLocation> resolved = new ArrayList<>(locations.size());
		for (String location : locations) {
			resolved.addAll(resolveAll(binder, parent, location, profiles));
		}
		return resolved;
	}

	private List<ConfigDataLocation> resolveAll(Binder binder, ConfigDataLocation parent, String location,
			Profiles profiles) {
		for (ConfigDataLocationResolver resolver : getResolvers()) {
			if (resolver.isResolvable(binder, parent, location)) {
				return resolve(resolver, binder, parent, location, profiles);
			}
		}
		throw new UnsupportedConfigDataLocationException(location);
	}

	private List<ConfigDataLocation> resolve(ConfigDataLocationResolver resolver, Binder binder,
			ConfigDataLocation parent, String location, Profiles profiles) {
		List<ConfigDataLocation> resolved = nonNullList(resolver.resolve(binder, parent, location));
		if (profiles == null) {
			return resolved;
		}
		List<ConfigDataLocation> profileSpecific = nonNullList(
				resolver.resolveProfileSpecific(binder, parent, location, profiles));
		return merge(resolved, profileSpecific);
	}

	List<ConfigDataLocationResolver> getResolvers() {
		return this.resolvers;
	}

	private <T> List<T> nonNullList(List<T> list) {
		return (list != null) ? list : Collections.emptyList();
	}

	private <T> List<T> merge(List<T> list1, List<T> list2) {
		List<T> merged = new ArrayList<>(list1.size() + list2.size());
		merged.addAll(list1);
		merged.addAll(list2);
		return merged;
	}

}
