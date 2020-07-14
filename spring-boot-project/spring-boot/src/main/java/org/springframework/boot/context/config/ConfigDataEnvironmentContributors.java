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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigDataEnvironmentContributor.ImportPhase;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.util.ObjectUtils;

/**
 * An immutable tree structure of {@link ConfigDataEnvironmentContributors} used to
 * process imports.
 *
 * @author Phillip Webb
 */
class ConfigDataEnvironmentContributors implements Iterable<ConfigDataEnvironmentContributor> {

	private final Log logger;

	private final ConfigDataEnvironmentContributor root;

	/**
	 * Create a new {@link ConfigDataEnvironmentContributors} instance.
	 * @param logFactory the log factory
	 * @param contributors the initial set of contributors
	 */
	ConfigDataEnvironmentContributors(DeferredLogFactory logFactory,
			List<ConfigDataEnvironmentContributor> contributors) {
		this.logger = logFactory.getLog(getClass());
		this.root = ConfigDataEnvironmentContributor.of(contributors);
	}

	private ConfigDataEnvironmentContributors(Log logger, ConfigDataEnvironmentContributor root) {
		this.logger = logger;
		this.root = root;
	}

	private ConfigDataEnvironmentContributor getRoot() {
		return this.root;
	}

	/**
	 * Processes imports from all active contributors and return a new
	 * {@link ConfigDataEnvironmentContributors} instance.
	 * @param importer the importer used to import {@link ConfigData}
	 * @param activationContext the current activation context or {@code null} if the
	 * context has not get been created
	 * @return a {@link ConfigDataEnvironmentContributors} instance with all relevant
	 * imports have been processed
	 */
	ConfigDataEnvironmentContributors withProcessedImports(ConfigDataImporter importer,
			ConfigDataActivationContext activationContext) {
		ImportPhase importPhase = ImportPhase.get(activationContext);
		this.logger.trace(LogMessage.format("Processing imports for phase %s. %s", importPhase,
				(activationContext != null) ? activationContext : "no activation context"));
		ConfigDataEnvironmentContributors result = this;
		int processedCount = 0;
		while (true) {
			ConfigDataEnvironmentContributor unprocessed = getFirstUnprocessed(result, activationContext, importPhase);
			if (unprocessed == null) {
				this.logger.trace(LogMessage.format("Processed imports for of %d contributors", processedCount));
				return result;
			}
			ConfigDataLocationResolverContext locationResolverContext = new ContributorLocationResolverContext(result,
					unprocessed, activationContext);
			List<String> imports = unprocessed.getImports();
			this.logger.trace(LogMessage.format("Processing imports %s", imports));
			Map<ConfigDataLocation, ConfigData> imported = importer.resolveAndLoad(activationContext,
					locationResolverContext, imports);
			this.logger.trace(LogMessage.of(() -> imported.isEmpty() ? "Nothing imported" : "Imported "
					+ imported.size() + " location " + ((imported.size() != 1) ? "s" : "") + imported.keySet()));
			ConfigDataEnvironmentContributor processed = unprocessed.withChildren(importPhase,
					asContributors(activationContext, imported));
			result = new ConfigDataEnvironmentContributors(this.logger,
					result.getRoot().withReplacement(unprocessed, processed));
			processedCount++;
		}
	}

	private ConfigDataEnvironmentContributor getFirstUnprocessed(ConfigDataEnvironmentContributors contributors,
			ConfigDataActivationContext activationContext, ImportPhase importPhase) {
		for (ConfigDataEnvironmentContributor contributor : contributors.getRoot()) {
			if (contributor.isActive(activationContext) && contributor.hasUnprocessedImports(importPhase)) {
				return contributor;
			}
		}
		return null;
	}

	private List<ConfigDataEnvironmentContributor> asContributors(ConfigDataActivationContext activationContext,
			Map<ConfigDataLocation, ConfigData> imported) {
		List<ConfigDataEnvironmentContributor> contributors = new ArrayList<>(imported.size() * 5);
		imported.forEach((location, data) -> {
			for (int i = data.getPropertySources().size() - 1; i >= 0; i--) {
				contributors.add(ConfigDataEnvironmentContributor.ofImported(location, data, i, activationContext));
			}
		});
		return Collections.unmodifiableList(contributors);
	}

	/**
	 * Return a {@link Binder} that works against all active contributors.
	 * @param activationContext the activation context
	 * @param options binder options to apply
	 * @return a binder instance
	 */
	Binder getBinder(ConfigDataActivationContext activationContext, BinderOption... options) {
		return getBinder(activationContext, ObjectUtils.isEmpty(options) ? EnumSet.noneOf(BinderOption.class)
				: EnumSet.copyOf(Arrays.asList(options)));
	}

	private Binder getBinder(ConfigDataActivationContext activationContext, Set<BinderOption> options) {
		Iterable<ConfigurationPropertySource> sources = () -> this.root
				.iterator(ConfigDataEnvironmentContributor::getConfigurationPropertySource);
		PlaceholdersResolver placeholdersResolver = new ConfigDataEnvironmentContributorPlaceholdersResolver(this.root,
				activationContext, options.contains(BinderOption.FAIL_ON_BIND_TO_INACTIVE_SOURCE));
		BindHandler bindHandler = null;
		return new Binder(sources, placeholdersResolver, null, null, bindHandler);
	}

	@Override
	public Iterator<ConfigDataEnvironmentContributor> iterator() {
		return this.root.iterator();
	}

	/**
	 * {@link ConfigDataLocationResolverContext} backed by a
	 * {@link ConfigDataEnvironmentContributor}.
	 */
	private static class ContributorLocationResolverContext implements ConfigDataLocationResolverContext {

		private final ConfigDataEnvironmentContributors contributors;

		private final ConfigDataEnvironmentContributor contributor;

		private final ConfigDataActivationContext activationContext;

		private volatile Binder binder;

		ContributorLocationResolverContext(ConfigDataEnvironmentContributors contributors,
				ConfigDataEnvironmentContributor contributor, ConfigDataActivationContext activationContext) {
			this.contributors = contributors;
			this.contributor = contributor;
			this.activationContext = activationContext;
		}

		@Override
		public Binder getBinder() {
			Binder binder = this.binder;
			if (binder == null) {
				binder = this.contributors.getBinder(this.activationContext);
				this.binder = binder;
			}
			return binder;
		}

		@Override
		public ConfigDataLocation getParent() {
			return this.contributor.getLocation();
		}

	}

	/**
	 * Binder options that can be used with
	 * {@link ConfigDataEnvironmentContributors#getBinder(ConfigDataActivationContext, BinderOption...)}.
	 */
	enum BinderOption {

		/**
		 * Throw an exception if an inactive contributor contains a bound value.
		 */
		FAIL_ON_BIND_TO_INACTIVE_SOURCE;

	}

}
