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

import org.springframework.boot.origin.Origin;

/**
 * Exception thrown when an attempt is made to resolve a property against an inactive
 * {@link ConfigData} property source. Used to ensure that a user doesn't accidentally
 * attempt to specify a properties that can never be resolved.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class InactiveConfigDataAccessException extends ConfigDataException {

	private final ConfigDataLocation location;

	private final String propertyName;

	private final Origin origin;

	/**
	 * Create a new {@link InactiveConfigDataAccessException} instance.
	 * @param location the config data location
	 * @param propertyName the property name that was accessed
	 * @param origin the orgin of the accessed property
	 */
	InactiveConfigDataAccessException(ConfigDataLocation location, String propertyName, Origin origin) {
		super("", null); // FIXME
		this.location = location;
		this.propertyName = propertyName;
		this.origin = origin;
	}

	/**
	 * Return the config data location that was accessed.
	 * @return the config data location
	 */
	public ConfigDataLocation getLocation() {
		return this.location;
	}

	/**
	 * Return the property name that was accessed.
	 * @return the property name
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * Return the origin of the property or {@code null} if the origin is unknown.
	 * @return the property origin
	 */
	public Origin getOrigin() {
		return this.origin;
	}

}
