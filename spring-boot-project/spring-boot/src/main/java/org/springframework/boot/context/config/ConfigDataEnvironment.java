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

import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigDataEnvironmentContributors.BinderOption;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.log.LogMessage;
import org.springframework.util.StringUtils;

/**
 * Wrapper around a {@link ConfigurableEnvironment} that can be used to import and apply
 * {@link ConfigData}. Configures the initial set of
 * {@link ConfigDataEnvironmentContributors} by wrapping property sources from the Spring
 * {@link Environment} and adding the initial set of imports.
 * <p>
 * The initial imports can be influenced via the {@link #LOCATION_PROPERTY},
 * {@value #ADDITIONAL_LOCATION_PROPERTY} and {@value #SPRING_CONFIG_IMPORT} properties.
 * If not explicit properties are set, the {@link #DEFAULT_SEARCH_LOCATIONS} will be used.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironment {

	/**
	 * Property used override the imported locations.
	 */
	static final String LOCATION_PROPERTY = "spring.config.location";

	/**
	 * Property used to provide additional locations to import.
	 */
	static final String ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location";

	/**
	 * Property used to provide additional locations to import.
	 */
	static final String SPRING_CONFIG_IMPORT = "spring.config.import";

	/**
	 * Default search locations used if not {@link #LOCATION_PROPERTY} is found.
	 */
	static final String[] DEFAULT_SEARCH_LOCATIONS = { "classpath:/", "classpath:/config/", "file:./",
			"file:./config/*/", "file:./config/" };

	private static final String DEFAULT_PROPERTY_SOURCE_NAME = "defaultProperties";

	private static final String[] EMPTY_LOCATIONS = new String[0];

	private final DeferredLogFactory logFactory;

	private final Log logger;

	private final ConfigurableEnvironment environment;

	private final ConfigDataLocationResolvers resolvers;

	private final ConfigDataLoaders loaders;

	private final ConfigDataEnvironmentContributors contributors;

	/**
	 * Create a new {@link ConfigDataEnvironment} instance.
	 * @param logFactory the deferred log factory
	 * @param environment the Spring {@link Environment}.
	 */
	ConfigDataEnvironment(DeferredLogFactory logFactory, ConfigurableEnvironment environment) {
		Binder binder = Binder.get(environment);
		UseLegacyConfigProcessingException.throwIfRequested(binder);
		this.logFactory = logFactory;
		this.logger = logFactory.getLog(getClass());
		this.environment = environment;
		this.resolvers = new ConfigDataLocationResolvers(logFactory, binder);
		this.loaders = new ConfigDataLoaders(logFactory);
		this.contributors = getContributors(binder);
	}

	private ConfigDataEnvironmentContributors getContributors(Binder binder) {
		this.logger.trace("Building config data environment contributors");
		MutablePropertySources propertySources = this.environment.getPropertySources();
		List<ConfigDataEnvironmentContributor> contributors = new ArrayList<>(propertySources.size() + 10);
		PropertySource<?> defaultPropertySource = null;
		for (PropertySource<?> propertySource : propertySources) {
			if (isDefaultPropertySource(propertySource)) {
				defaultPropertySource = propertySource;
			}
			else {
				this.logger.trace(LogMessage.format("Creating wrapped config data contributor for '%s'",
						propertySource.getName()));
				contributors.add(wrapEnvironmentPropertySource(propertySource));
			}
		}
		contributors.addAll(getInitialImportContributors(binder));
		if (defaultPropertySource != null) {
			this.logger.trace("Creating wrapped config data contributor for default property source");
			contributors.add(wrapEnvironmentPropertySource(defaultPropertySource));
		}
		return new ConfigDataEnvironmentContributors(this.logFactory, contributors);
	}

	private boolean isDefaultPropertySource(PropertySource<?> propertySource) {
		return DEFAULT_PROPERTY_SOURCE_NAME.equals(propertySource.getName());
	}

	private ConfigDataEnvironmentContributor wrapEnvironmentPropertySource(PropertySource<?> propertySource) {
		return ConfigDataEnvironmentContributor.ofExisting(propertySource);
	}

	private List<ConfigDataEnvironmentContributor> getInitialImportContributors(Binder binder) {
		List<ConfigDataEnvironmentContributor> initialContributors = new ArrayList<>();
		addInitialImportContributors(initialContributors,
				binder.bind(LOCATION_PROPERTY, String[].class).orElse(DEFAULT_SEARCH_LOCATIONS));
		addInitialImportContributors(initialContributors,
				binder.bind(ADDITIONAL_LOCATION_PROPERTY, String[].class).orElse(EMPTY_LOCATIONS));
		addInitialImportContributors(initialContributors,
				binder.bind(SPRING_CONFIG_IMPORT, String[].class).orElse(EMPTY_LOCATIONS));
		return initialContributors;
	}

	private void addInitialImportContributors(List<ConfigDataEnvironmentContributor> initialContributors,
			String[] locations) {
		for (int i = locations.length - 1; i >= 0; i--) {
			initialContributors.add(createInitialImportContributor(locations[i]));
		}
	}

	private ConfigDataEnvironmentContributor createInitialImportContributor(String location) {
		this.logger.trace(LogMessage.format("Adding initial config data import from location '%s'", location));
		return ConfigDataEnvironmentContributor.ofInitialImport(location);
	}

	/**
	 * Process all contributions and apply any newly imported property sources to the
	 * {@link Environment}.
	 */
	void processAndApply() {
		ConfigDataImporter importer = new ConfigDataImporter(this.resolvers, this.loaders);
		ConfigDataEnvironmentContributors contributors = processInitial(this.contributors, importer);
		ConfigDataActivationContext activationContext = createActivationContext(contributors);
		contributors = processWithoutProfiles(contributors, importer, activationContext);
		activationContext = withProfiles(contributors, activationContext);
		contributors = processWithProfiles(contributors, importer, activationContext);
		applyToEnvironment(contributors, activationContext);
	}

	private ConfigDataEnvironmentContributors processInitial(ConfigDataEnvironmentContributors contributors,
			ConfigDataImporter importer) {
		this.logger.trace("Processing initial config data environment contributors without activation context");
		return contributors.withProcessedImports(importer, null);
	}

	private ConfigDataActivationContext createActivationContext(ConfigDataEnvironmentContributors contributors) {
		this.logger.trace("Creating config data activation context from initial contributions");
		Binder binder = contributors.getBinder(null, BinderOption.FAIL_ON_BIND_TO_INACTIVE_SOURCE);
		return new ConfigDataActivationContext(this.environment, binder);
	}

	private ConfigDataEnvironmentContributors processWithoutProfiles(ConfigDataEnvironmentContributors contributors,
			ConfigDataImporter importer, ConfigDataActivationContext activationContext) {
		this.logger.trace("Processing config data environment contributors with intial activation context");
		return contributors.withProcessedImports(importer, activationContext);
	}

	private ConfigDataActivationContext withProfiles(ConfigDataEnvironmentContributors contributors,
			ConfigDataActivationContext activationContext) {
		this.logger.trace("Deducing profiles from current config data environment contributors");
		Binder binder = contributors.getBinder(activationContext, BinderOption.FAIL_ON_BIND_TO_INACTIVE_SOURCE);
		Profiles profiles = new Profiles(this.environment, binder);
		return activationContext.withProfiles(profiles);
	}

	private ConfigDataEnvironmentContributors processWithProfiles(ConfigDataEnvironmentContributors contributors,
			ConfigDataImporter importer, ConfigDataActivationContext activationContext) {
		this.logger.trace("Processing config data environment contributors with profile activation context");
		return contributors.withProcessedImports(importer, activationContext);
	}

	private void applyToEnvironment(ConfigDataEnvironmentContributors contributors,
			ConfigDataActivationContext activationContext) {
		MutablePropertySources propertySources = this.environment.getPropertySources();
		PropertySource<?> defaultPropertySource = propertySources.remove(DEFAULT_PROPERTY_SOURCE_NAME);
		this.logger.trace("Applying config data environment contributions");
		for (ConfigDataEnvironmentContributor contributor : contributors) {
			if (contributor.getKind() == ConfigDataEnvironmentContributor.Kind.IMPORTED
					&& contributor.getPropertySource() != null) {
				if (!contributor.isActive(activationContext)) {
					this.logger.trace(LogMessage.format("Skipping inactive property source '%s'",
							contributor.getPropertySource().getName()));
				}
				else {
					this.logger.trace(LogMessage.format("Adding imported property source '%s'",
							contributor.getPropertySource().getName()));
					propertySources.addLast(contributor.getPropertySource());
				}
			}
		}
		if (defaultPropertySource != null) {
			propertySources.addLast(defaultPropertySource);
		}
		Profiles profiles = activationContext.getProfiles();
		this.logger.trace(LogMessage.format("Setting default profies: %s", profiles.getDefault()));
		this.environment.setDefaultProfiles(StringUtils.toStringArray(profiles.getDefault()));
		this.logger.trace(LogMessage.format("Setting active profiles: %s", profiles.getActive()));
		this.environment.setActiveProfiles(StringUtils.toStringArray(profiles.getActive()));
	}

}
