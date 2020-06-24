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

import org.apache.commons.logging.Log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.log.LogMessage;

/**
 * {@link EnvironmentPostProcessor} used to load external configuration.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class ConfigurationDataEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	/**
	 * The default order for the processor.
	 */
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	/**
	 * The "config name" property name.
	 */
	public static final String NAME_PROPERTY = "spring.config.name";

	/**
	 * The "config location" property name.
	 */
	public static final String LOCATION_PROPERTY = "spring.config.location";

	/**
	 * The "config additional location" property name.
	 */
	public static final String ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location";

	private static final String[] DEFAULT_LOCATIONS = { "classpath:/", "classpath:/config/", "file:./",
			"file:./config/*/", "file:./config/" };

	private static final String[] DEFAULT_NAMES = { "application" };

	private static final String[] NO_LOCATIONS = {};

	private final Log logger;

	private int order = DEFAULT_ORDER;

	public ConfigurationDataEnvironmentPostProcessor(Log logger) {
		this.logger = logger;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Binder binder = Binder.get(environment);
		String[] names = binder.bind(NAME_PROPERTY, String[].class).orElse(DEFAULT_NAMES);
		String[] locations = binder.bind(LOCATION_PROPERTY, String[].class).orElse(DEFAULT_LOCATIONS);
		String[] additionalLocations = binder.bind(ADDITIONAL_LOCATION_PROPERTY, String[].class).orElse(NO_LOCATIONS);
		this.logger.info(LogMessage.of(() -> "Names: " + Arrays.asList(names)));
		this.logger.info(LogMessage.of(() -> "Locations: " + Arrays.asList(locations)));
		this.logger.info(LogMessage.of(() -> "Additional Location: " + Arrays.asList(additionalLocations)));
	}

	@SuppressWarnings("deprecation")
	private void processUsingLegacyMethod(ConfigurableEnvironment environment, SpringApplication application) {
		ConfigFileApplicationListener legacyListener = new ConfigFileApplicationListener();
		legacyListener.postProcessEnvironment(environment, application);
	}

}
