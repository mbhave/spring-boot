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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * A single element that may directly or indirectly contribute configuration data to the
 * {@link Environment}. There are several different {@link Kind kinds} of contributor, all
 * are immutable and will be replaced with new versions as imports are processed.
 * <p>
 * Contributors may provide a set of imports that should be processed and ultimately
 * turned into children. There are two distinct import phases:
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
class ConfigDataEnvironmentContributor implements Iterable<ConfigDataEnvironmentContributor> {

	private final ConfigDataLocation location;

	private final PropertySource<?> propertySource;

	private final ConfigurationPropertySource configurationPropertySource;

	private final ConfigDataProperties properties;

	private final Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children;

	private final Kind kind;

	/**
	 * Create a new {@link ConfigDataEnvironmentContributor} instance.
	 * @param kind the contributor kind
	 * @param location the location that contributed the data or {@code null}
	 * @param propertySource the property source for the data or {@code null}
	 * @param configurationPropertySource the configuration property source for the data
	 * or {@code null}
	 * @param properties the config data properties or {@code null}
	 * @param children the children of this contributor at each {@link ImportPhase}
	 */
	protected ConfigDataEnvironmentContributor(Kind kind, ConfigDataLocation location, PropertySource<?> propertySource,
			ConfigurationPropertySource configurationPropertySource, ConfigDataProperties properties,
			Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children) {
		this.kind = kind;
		this.location = location;
		this.properties = properties;
		this.propertySource = propertySource;
		this.configurationPropertySource = configurationPropertySource;
		this.children = (children != null) ? children : Collections.emptyMap();
	}

	/**
	 * Return the contributor kind.
	 * @return the kind of contributor
	 */
	Kind getKind() {
		return this.kind;
	}

	/**
	 * Return if this contributor is currently active.
	 * @param activationContext the activation context
	 * @return if the contributor is active
	 */
	boolean isActive(ConfigDataActivationContext activationContext) {
		return this.properties == null || this.properties.isActive(activationContext);
	}

	/**
	 * Return the location that contributed this instance.
	 * @return the location or {@code null}
	 */
	ConfigDataLocation getLocation() {
		return this.location;
	}

	/**
	 * Return the property source for this contributor.
	 * @return the property source or {@code null}
	 */
	PropertySource<?> getPropertySource() {
		return this.propertySource;
	}

	/**
	 * Return the configuration property source for this contributor.
	 * @return the configuration property source or {@code null}
	 */
	ConfigurationPropertySource getConfigurationPropertySource() {
		return this.configurationPropertySource;
	}

	/**
	 * Return any imports requested by this contributor.
	 * @return the imports
	 */
	List<String> getImports() {
		return (this.properties != null) ? this.properties.getImports() : Collections.emptyList();
	}

	/**
	 * Return true if this contributor has imports that have not yet been processed in the
	 * given phase.
	 * @param importPhase the import phase
	 * @return if there are unprocessed imports
	 */
	boolean hasUnprocessedImports(ImportPhase importPhase) {
		if (getImports().isEmpty()) {
			return false;
		}
		return !this.children.containsKey(importPhase);
	}

	/**
	 * Return children of this contributor for the given phase.
	 * @param importPhase the import phase
	 * @return a list of children
	 */
	List<ConfigDataEnvironmentContributor> getChildren(ImportPhase importPhase) {
		return this.children.getOrDefault(importPhase, Collections.emptyList());
	}

	/**
	 * Returns an {@link Iterator} that traverses this contributor and all its children in
	 * priority order.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ConfigDataEnvironmentContributor> iterator() {
		return iterator(Function.identity());
	}

	/**
	 * Returns an {@link Iterator} that traverses and extracts data from this contributor
	 * and all its children in priority order.
	 * @param <E> the element type
	 * @param extractor an extractor used to extract specific data
	 * @return an iterator that traverses the contributors
	 */
	<E> Iterator<E> iterator(Function<ConfigDataEnvironmentContributor, E> extractor) {
		return new ContributorIterator<>(extractor);
	}

	/**
	 * Create a new {@link ConfigDataEnvironmentContributor} instance with a new set of
	 * children for the given phase.
	 * @param importPhase the import phase
	 * @param children the new children
	 * @return a new contributor instance
	 */
	ConfigDataEnvironmentContributor withChildren(ImportPhase importPhase,
			List<ConfigDataEnvironmentContributor> children) {
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> updatedChildren = new LinkedHashMap<>(this.children);
		updatedChildren.put(importPhase, children);
		return new ConfigDataEnvironmentContributor(this.kind, this.location, this.propertySource,
				this.configurationPropertySource, this.properties, updatedChildren);
	}

	/**
	 * Create a new {@link ConfigDataEnvironmentContributor} instance where a existing
	 * child is replaced.
	 * @param existing the existing node that should be replaced
	 * @param replacement the replacement node that should be used instead
	 * @return a new {@link ConfigDataEnvironmentContributor} instance
	 */
	ConfigDataEnvironmentContributor withReplacement(ConfigDataEnvironmentContributor existing,
			ConfigDataEnvironmentContributor replacement) {
		if (this == existing) {
			return replacement;
		}
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> updatedChildren = new LinkedHashMap<>(
				this.children.size());
		this.children.forEach((importPhase, contributors) -> {
			List<ConfigDataEnvironmentContributor> updatedContributors = new ArrayList<>(contributors.size());
			for (ConfigDataEnvironmentContributor contributor : contributors) {
				updatedContributors.add(contributor.withReplacement(existing, replacement));
			}
			updatedChildren.put(importPhase, Collections.unmodifiableList(updatedContributors));
		});
		return new ConfigDataEnvironmentContributor(this.kind, this.location, this.propertySource,
				this.configurationPropertySource, this.properties, updatedChildren);
	}

	/**
	 * Factory method to create a {@link Kind#ROOT root} contributor.
	 * @param contributors the immediate children of the root
	 * @return a new {@link ConfigDataEnvironmentContributor} instance
	 */
	static ConfigDataEnvironmentContributor of(List<ConfigDataEnvironmentContributor> contributors) {
		Map<ImportPhase, List<ConfigDataEnvironmentContributor>> children = new LinkedHashMap<>();
		children.put(ImportPhase.BEFORE_PROFILE_ACTIVATION, Collections.unmodifiableList(contributors));
		return new ConfigDataEnvironmentContributor(Kind.ROOT, null, null, null, null, children);
	}

	/**
	 * Factory method to create a {@link Kind#INITIAL_IMPORT initial import} contributor.
	 * This contributor is used to trigger initial imports of additional contributors. It
	 * does not contribute any properties itself.
	 * @param importLocation the initial import location
	 * @return a new {@link ConfigDataEnvironmentContributor} instance
	 */
	static ConfigDataEnvironmentContributor ofInitialImport(String importLocation) {
		List<String> imports = Collections.singletonList(importLocation);
		ConfigDataProperties properties = new ConfigDataProperties(imports, null);
		return new ConfigDataEnvironmentContributor(Kind.INITIAL_IMPORT, null, null, null, properties, null);
	}

	/**
	 * Factory method to create a contributor that wraps an {@link Kind#EXISTING existing}
	 * property source. The contributor provides access to existing properties, but
	 * doesn't actively import any additional contributors.
	 * @param propertySource the property source to wrap
	 * @return a new {@link ConfigDataEnvironmentContributor} instance
	 */
	static ConfigDataEnvironmentContributor ofExisting(PropertySource<?> propertySource) {
		return new ConfigDataEnvironmentContributor(Kind.EXISTING, null, propertySource,
				ConfigurationPropertySource.from(propertySource), null, null);
	}

	/**
	 * Factory method to create a {@link Kind#IMPORTED imported} contributor. This
	 * contributor has been actively imported from another contributor and may itself
	 * import further contributors later.
	 * @param location the location of imported config data
	 * @param configData the config data
	 * @param propertySourceIndex the index of the property source that should be used
	 * @param activationContext the current activation context
	 * @return a new {@link ConfigDataEnvironmentContributor} instance
	 */
	static ConfigDataEnvironmentContributor ofImported(ConfigDataLocation location, ConfigData configData,
			int propertySourceIndex, ConfigDataActivationContext activationContext) {
		PropertySource<?> propertySource = configData.getPropertySources().get(propertySourceIndex);
		ConfigurationPropertySource configurationPropertySource = ConfigurationPropertySource.from(propertySource);
		Binder binder = new Binder(configurationPropertySource);
		UseLegacyConfigProcessingException.throwIfRequested(binder);
		ConfigDataProperties properties = ConfigDataProperties.get(binder);
		// FIXME we need to check here for invalid profile properties etc. We probably
		// need the activation context. Here might not actually be best. Since the user
		// might flip later
		if (configData.getOptions().contains(ConfigData.Option.IGNORE_IMPORTS)) {
			properties = properties.withoutImports();
		}
		return new ConfigDataEnvironmentContributor(Kind.IMPORTED, location, propertySource,
				configurationPropertySource, properties, null);
	}

	/**
	 * The various kinds of contributor.
	 */
	enum Kind {

		/**
		 * A root contributor used contain the initial set of children.
		 */
		ROOT,

		/**
		 * An initial import that needs to be processed.
		 */
		INITIAL_IMPORT,

		/**
		 * An existing property source that contributes properties but no imports.
		 */
		EXISTING,

		/**
		 * A contributor with {@link ConfigData} imported from another contributor.
		 */
		IMPORTED;

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
			if (activationContext != null && activationContext.getProfiles() != null) {
				return AFTER_PROFILE_ACTIVATION;
			}
			return BEFORE_PROFILE_ACTIVATION;
		}

	}

	/**
	 * Iterator that traverses the contributor tree.
	 */
	private class ContributorIterator<E> implements Iterator<E> {

		private final Function<ConfigDataEnvironmentContributor, E> extractor;

		private ImportPhase phase;

		private Iterator<ConfigDataEnvironmentContributor> children;

		private Iterator<E> current;

		private E next;

		ContributorIterator(Function<ConfigDataEnvironmentContributor, E> extractor) {
			this.extractor = extractor;
			this.phase = ImportPhase.AFTER_PROFILE_ACTIVATION;
			this.children = getChildren(this.phase).iterator();
			this.current = Collections.emptyIterator();
		}

		@Override
		public boolean hasNext() {
			return fetchIfNecessary() != null;
		}

		@Override
		public E next() {
			E next = fetchIfNecessary();
			if (next == null) {
				throw new NoSuchElementException();
			}
			this.next = null;
			return next;
		}

		private E fetchIfNecessary() {
			if (this.next != null) {
				return this.next;
			}
			if (this.current.hasNext()) {
				this.next = this.current.next();
				return this.next;
			}
			if (this.children.hasNext()) {
				this.current = this.children.next().iterator(this.extractor);
				return fetchIfNecessary();
			}
			if (this.phase == ImportPhase.AFTER_PROFILE_ACTIVATION) {
				this.phase = ImportPhase.BEFORE_PROFILE_ACTIVATION;
				this.children = getChildren(this.phase).iterator();
				return fetchIfNecessary();
			}
			if (this.phase == ImportPhase.BEFORE_PROFILE_ACTIVATION) {
				this.phase = null;
				this.next = this.extractor.apply(ConfigDataEnvironmentContributor.this);
				return this.next;
			}
			return null;
		}

	}

}
