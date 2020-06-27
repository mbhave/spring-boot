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

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Context information used when determining when to activate
 * {@link ConfigDataEnvironmentContributor contributed} {@link ConfigData}.
 *
 * @author Phillip Webb
 */
class ConfigDataActivationContext {

	private static final String[] NO_PROFILES = {};

	private final CloudPlatform cloudPlatform;

	private final String[] activeProfiles;

	/**
	 * Create a new {@link ConfigDataActivationContext} instance before any profiles have
	 * been activated.
	 * @param environment the source environment
	 * @param binder a binder providing access to relevant config data contributions
	 */
	ConfigDataActivationContext(Environment environment, Binder binder) {
		this.cloudPlatform = deduceCloudPlatform(environment, binder);
		this.activeProfiles = null;
	}

	private ConfigDataActivationContext(CloudPlatform cloudPlatform, String[] activeProfiles) {
		this.cloudPlatform = cloudPlatform;
		this.activeProfiles = activeProfiles;
	}

	private CloudPlatform deduceCloudPlatform(Environment environment, Binder binder) {
		for (CloudPlatform candidate : CloudPlatform.values()) {
			if (candidate.isEnfoced(binder)) {
				return candidate;
			}
		}
		return CloudPlatform.getActive(environment);
	}

	/**
	 * Return a new {@link ConfigDataActivationContext} with acivated profiles.
	 * @param environment the source environment
	 * @param binder a binder providing access to relevant config data contributions
	 * @return a new {@link ConfigDataActivationContext} with activated profiles
	 */
	ConfigDataActivationContext withActivedProfiles(Environment environment, Binder binder) {
		return new ConfigDataActivationContext(this.cloudPlatform, deduceActiveProfiles(environment, binder));
	}

	private String[] deduceActiveProfiles(Environment environment, Binder binder) {
		if (hasExplicitActiveProfiles(environment)) {
			return environment.getActiveProfiles();
		}
		return binder.bind(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, String[].class).orElse(NO_PROFILES);
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
	 * Return the active {@link CloudPlatform} or {@code null}.
	 * @return the active cloud platform
	 */
	CloudPlatform getCloudPlatform() {
		return this.cloudPlatform;
	}

	/**
	 * Return {@code true} if profiles have been activated.
	 * @return if profiles have been activated
	 */
	boolean hasActivatedProfiles() {
		return this.activeProfiles != null;
	}

	/**
	 * Return the active profiles or {@code null} if no profiles have been activated yet.
	 * @return the active profiles
	 */
	String[] getActiveProfiles() {
		return this.activeProfiles;
	}

}
