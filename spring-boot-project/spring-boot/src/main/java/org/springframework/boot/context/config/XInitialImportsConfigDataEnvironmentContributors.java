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

import java.util.Iterator;

import org.springframework.boot.context.properties.bind.Binder;

/**
 * {@link ConfigDataEnvironmentContributor} the provides the initial set of imports.
 *
 * @author Phillip Webb
 */
class XInitialImportsConfigDataEnvironmentContributors implements Iterable<ConfigDataEnvironmentContributor> {

	private static final String LOCATION_PROPERTY = "spring.config.location";

	private static final String ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location";

	private static final String[] DEFAULT_LOCATIONS = { "classpath:/", "classpath:/config/", "file:./",
			"file:./config/*/", "file:./config/" };

	private static final String[] DEFAULT_ADDITIONAL_LOCATIONS = {};

	XInitialImportsConfigDataEnvironmentContributors(Binder binder) {
		String[] locations = binder.bind(LOCATION_PROPERTY, String[].class).orElse(DEFAULT_LOCATIONS);
		String[] additionalLocations = binder.bind(ADDITIONAL_LOCATION_PROPERTY, String[].class)
				.orElse(DEFAULT_ADDITIONAL_LOCATIONS);
		// Add each one as in IC in a certain order
	}

	@Override
	public Iterator<ConfigDataEnvironmentContributor> iterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

}
