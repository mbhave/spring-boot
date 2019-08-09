/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.convert;

import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;

/**
 * Abstract base class for converting from a type to a {@link java.lang.Enum}.
 *
 * @author Madhura Bhave
 */
abstract class AbstractTypeToEnumConverterFactory<T> implements ConverterFactory<T, Enum> {

	<T extends Enum> Class<?> getEnumType(Class<T> targetType) {
		Class<?> enumType = targetType;
		while (enumType != null && !enumType.isEnum()) {
			enumType = enumType.getSuperclass();
		}
		Assert.notNull(enumType, () -> "The target type " + targetType.getName() + " does not refer to an enum");
		return enumType;
	}

	String getCanonicalName(String name) {
		StringBuilder canonicalName = new StringBuilder(name.length());
		name.chars().filter(Character::isLetterOrDigit).map(Character::toLowerCase)
				.forEach((c) -> canonicalName.append((char) c));
		return canonicalName.toString();
	}

}
