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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

/**
 * @author Phillip Webb
 */
public class XXConfigDataEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	/**
	 * The default order for the processor.
	 */
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	private static final String DEFAULT_PROPERTIES = "defaultProperties";

	private final Log logger;

	public XXConfigDataEnvironmentPostProcessor(Log logger) {
		this.logger = logger;
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		DeferredLog initialLogger = new DeferredLog();
		Binder environmentBinder = Binder.get(environment);
		ConfigDataImporter importer = new ConfigDataImporter(environmentBinder);
		ConfigDataEnvironmentContributors contributors = createInitialContributors(binder,
				environment.getPropertySources());
		// Phase 1: Load enough information to create the context and check for a legacy
		// process request
		contributors = contributors.processImports(importer, null);
		if (isLegacyProcessingRequested(contributors.getBinder())) {
			this.logger.warn("Using legacy config file loading");
			processUsingLegacyProcessor(environment, application);
			return;
		}
		initialLogger.switchTo(this.logger);
		ConfigDataActivationContext context = new ConfigDataActivationContext(contributors.getBinder());
		// Phase 2: Process imports that are activate with current context
		contributors = contributors.processImports(importer, context);
		// Phase 3: Activate profiles and process imports with the updated context
		context = context.withActivedProfiles(environment.getActiveProfiles(), contributors.getBinder());
		contributors = contributors.processImports(importer, context);
		applyContributedConfig(environment, contributors, context);
	}

	// FIXME make a type and do the work in that
	private ConfigDataEnvironmentContributors createInitialContributors(Binder environmentBinder,
			PropertySources propertySources) {
		List<ConfigDataEnvironmentContributor> contributors = new ArrayList<>();
		PropertySource<?> defaultPropertySource = null;
		for (PropertySource<?> propertySource : propertySources) {
			if (isDefaultPropertySource(propertySource)) {
				defaultPropertySource = propertySource;
			}
			else {
				contributors.add(new PropertySourceConfigDataEnvironmentContributor(propertySource));
			}
		}
		new XInitialImportsConfigDataEnvironmentContributors(environmentBinder).forEach(contributors::add);
		if (defaultPropertySource != null) {
			contributors.add(new PropertySourceConfigDataEnvironmentContributor(defaultPropertySource));
		}
		return new ConfigDataEnvironmentContributors(contributors);
	}

	private boolean isDefaultPropertySource(PropertySource<?> propertySource) {
		return propertySource.getName().equals(DEFAULT_PROPERTIES);
	}

	private boolean isLegacyProcessingRequested(Binder binder) {
		return binder.bind("spring.config.use-legacy-processor", Boolean.class).orElse(false);
	}

	@SuppressWarnings("deprecation")
	private void processUsingLegacyProcessor(ConfigurableEnvironment environment, SpringApplication application) {
		new ConfigFileApplicationListener(this.logger).postProcessEnvironment(environment, application);
	}

	private void applyContributedConfig(ConfigurableEnvironment environment,
			ConfigDataEnvironmentContributors contributors, ConfigDataActivationContext context) {
	}

}
