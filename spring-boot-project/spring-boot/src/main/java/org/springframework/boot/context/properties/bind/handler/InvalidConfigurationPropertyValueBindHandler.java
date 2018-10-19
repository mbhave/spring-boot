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
package org.springframework.boot.context.properties.bind.handler;

import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.PropertyValueValidationException;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;

/**
 * {@link BindHandler} that can be used to convert
 * {@link PropertyValueValidationException} to
 * {@link InvalidConfigurationPropertyValueException}.
 *
 * @author Madhura Bhave
 */
public class InvalidConfigurationPropertyValueBindHandler extends AbstractBindHandler {

	/**
	 * Create a new {@link InvalidConfigurationPropertyValueBindHandler} instance with a
	 * specific parent.
	 * @param parent the parent handler
	 */
	public InvalidConfigurationPropertyValueBindHandler(BindHandler parent) {
		super(parent);
	}

	@Override
	public Object onFailure(ConfigurationPropertyName name, Bindable<?> target,
			BindContext context, Exception error) throws Exception {
		PropertyValueValidationException ex = hasCause(error);
		if (ex != null) {
			throw new InvalidConfigurationPropertyValueException(name.toString(),
					ex.getValue(), ex.getReason());
		}
		throw error;
	}

	private PropertyValueValidationException hasCause(Throwable failure) {
		while (failure != null) {
			if (PropertyValueValidationException.class.isInstance(failure)) {
				return (PropertyValueValidationException) failure;
			}
			failure = failure.getCause();
		}
		return null;
	}

}
