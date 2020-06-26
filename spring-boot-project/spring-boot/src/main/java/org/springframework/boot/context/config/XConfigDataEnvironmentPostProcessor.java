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
import java.util.List;

import org.apache.commons.logging.Log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * {@link EnvironmentPostProcessor} used to load external configuration.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class XConfigDataEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final String DEFAULT_PROPERTIES = "defaultProperties";

	/**
	 * The default order for the processor.
	 */
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	private final Log logger;

	private int order = DEFAULT_ORDER;

	public XConfigDataEnvironmentPostProcessor(Log logger) {
		this.logger = logger;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		this.logger.trace("Adding config data to environment");
		List<PropertySource<?>> sources = getRelevantPropertySources(environment);
		XDunno dunno = new XDunno(sources);
		// FIXME if using legacy method...
		if (dunno.isLegacyProcessingRequested()) {
			postProcessUsingLegacyProcessor(environment, application);
			return;
		}
		XLoadedDunno loaded = dunno.load();

	}

	@SuppressWarnings("deprecation")
	private void postProcessUsingLegacyProcessor(ConfigurableEnvironment environment, SpringApplication application) {
		new ConfigFileApplicationListener().postProcessEnvironment(environment, application);
	}

	private List<PropertySource<?>> getRelevantPropertySources(ConfigurableEnvironment environment) {
		List<PropertySource<?>> sources = new ArrayList<>(environment.getPropertySources().size());
		for (PropertySource<?> source : environment.getPropertySources()) {
			if (!DEFAULT_PROPERTIES.equals(source.getName())) {
				sources.add(source);
			}
		}
		return Collections.unmodifiableList(sources);
	}

}
