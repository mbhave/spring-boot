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
package org.springframework.boot.context.properties.bind;

/**
 * Error thrown when a configuration property value fails validation.
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class PropertyValueValidationException extends RuntimeException {

	private final Object value;

	private final String reason;

	public PropertyValueValidationException(Object value, String reason) {
		this.value = value;
		this.reason = reason;
	}

	public Object getValue() {
		return this.value;
	}

	public String getReason() {
		return this.reason;
	}
}
