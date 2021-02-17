/*
 * Copyright 2012-2021 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.AbstractEnvironment;

/**
 * Exception thrown if an invalid property is found when processing config data.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class InvalidConfigDataPropertyException extends ConfigDataException {

	private static final Map<Property, ConfigurationPropertyName> WARNINGS;
	static {
		Map<Property, ConfigurationPropertyName> warnings = new LinkedHashMap<>();
		warnings.put(Property.forCollection(ConfigurationPropertyName.of("spring.profiles")),
				ConfigurationPropertyName.of("spring.config.activate.on-profile"));
		WARNINGS = Collections.unmodifiableMap(warnings);
	}

	private static final Set<Property> PROFILE_SPECIFIC_ERRORS;
	static {
		Set<Property> errors = new LinkedHashSet<>();
		errors.add(Property.forCollection(Profiles.INCLUDE_PROFILES));
		errors.add(Property
				.forCollection(ConfigurationPropertyName.of(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME)));
		errors.add(Property
				.forCollection(ConfigurationPropertyName.of(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME)));
		PROFILE_SPECIFIC_ERRORS = Collections.unmodifiableSet(errors);
	}

	private final ConfigurationProperty property;

	private final ConfigurationPropertyName replacement;

	private final ConfigDataResource location;

	InvalidConfigDataPropertyException(ConfigurationProperty property, boolean profileSpecific,
			ConfigurationPropertyName replacement, ConfigDataResource location) {
		super(getMessage(property, profileSpecific, replacement, location), null);
		this.property = property;
		this.replacement = replacement;
		this.location = location;
	}

	/**
	 * Return source property that caused the exception.
	 * @return the invalid property
	 */
	public ConfigurationProperty getProperty() {
		return this.property;
	}

	/**
	 * Return the {@link ConfigDataResource} of the invalid property or {@code null} if
	 * the source was not loaded from {@link ConfigData}.
	 * @return the config data location or {@code null}
	 */
	public ConfigDataResource getLocation() {
		return this.location;
	}

	/**
	 * Return the replacement property that should be used instead or {@code null} if not
	 * replacement is available.
	 * @return the replacement property name
	 */
	public ConfigurationPropertyName getReplacement() {
		return this.replacement;
	}

	/**
	 * Throw a {@link InvalidConfigDataPropertyException} or log a warning if the given
	 * {@link ConfigDataEnvironmentContributor} contains any invalid property. A warning
	 * is logged if the property is still supported, but not recommended. An error is
	 * thrown if the property is completely unsupported.
	 * @param logger the logger to use for warnings
	 * @param contributor the contributor to check
	 */
	static void throwOrWarn(Log logger, ConfigDataEnvironmentContributor contributor) {
		ConfigurationPropertySource propertySource = contributor.getConfigurationPropertySource();
		if (propertySource != null) {
			WARNINGS.forEach((property, replacement) -> throwOrWarn(propertySource, property,
					(p) -> logger.warn(getMessage(p, false, replacement, contributor.getResource()))));
			if (contributor.isProfileSpecific() && contributor.isNotIgnoringProfiles()) {
				PROFILE_SPECIFIC_ERRORS.forEach((property) -> throwOrWarn(propertySource, property, (p) -> {
					throw new InvalidConfigDataPropertyException(p, true, null, contributor.getResource());
				}));
			}
		}
	}

	private static void throwOrWarn(ConfigurationPropertySource propertySource, Property property,
			Consumer<ConfigurationProperty> action) {
		ConfigurationProperty configurationProperty = propertySource.getConfigurationProperty(property.getName());
		if (configurationProperty != null) {
			action.accept(configurationProperty);
		}
		else if (Collection.class.isAssignableFrom(property.getType())) {
			Collection<?> result = new Binder(propertySource).bind(property.getName(), Bindable.of(Collection.class))
					.orElse(null);
			if (result != null) {
				ConfigurationProperty indexedProperty = propertySource
						.getConfigurationProperty(property.getName().append("[0]"));
				action.accept(indexedProperty);
			}
		}
	}

	private static String getMessage(ConfigurationProperty property, boolean profileSpecific,
			ConfigurationPropertyName replacement, ConfigDataResource location) {
		StringBuilder message = new StringBuilder("Property '");
		message.append(property.getName());
		if (location != null) {
			message.append("' imported from location '");
			message.append(location);
		}
		message.append("' is invalid");
		if (profileSpecific) {
			message.append(" in a profile specific resource");
		}
		if (replacement != null) {
			message.append(" and should be replaced with '");
			message.append(replacement);
			message.append("'");
		}
		if (property.getOrigin() != null) {
			message.append(" [origin: ");
			message.append(property.getOrigin());
			message.append("]");
		}
		return message.toString();
	}

	static class Property {

		private final ConfigurationPropertyName name;

		private final Class<?> type;

		Property(ConfigurationPropertyName name, Class<?> type) {
			this.name = name;
			this.type = type;
		}

		public ConfigurationPropertyName getName() {
			return this.name;
		}

		public Class<?> getType() {
			return this.type;
		}

		static Property forCollection(ConfigurationPropertyName name) {
			return new Property(name, Collection.class);
		}

	}

}
