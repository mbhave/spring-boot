/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.context.properties.bind.PropertyValueValidationException;

/**
 * Error thrown when a datasource property value in invalid.
 *
 * @author Madhura Bhave
 */
class InvalidDataSourcePropertyValueException extends PropertyValueValidationException {

	/**
	 * Creates a new instance for the specified property {@code value}, including a
	 * {@code reason} why the value is invalid.
	 * @param value the value of the property, can be {@code null}
	 * @param reason a human-readable text that describes why the reason is invalid.
	 * Starts with an upper-case and ends with a dots. Several sentences and carriage
	 */
	public InvalidDataSourcePropertyValueException(Object value, String reason) {
		super(value, reason);
	}

}
