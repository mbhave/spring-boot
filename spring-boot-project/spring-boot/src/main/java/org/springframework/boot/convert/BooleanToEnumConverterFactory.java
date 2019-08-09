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

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Converts that llows mapping of YAML style {@code "false"} and {@code "true"} to enums {@code ON}
 * and {@code OFF}
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class BooleanToEnumConverterFactory extends AbstractTypeToEnumConverterFactory<Boolean> {

	private static Map<Boolean, List<String>> ALIASES;

	static {
		MultiValueMap<Boolean, String> aliases = new LinkedMultiValueMap<>();
		aliases.add(true, "on");
		aliases.add(false, "off");
		ALIASES = Collections.unmodifiableMap(aliases);
	}

	@Override
	public <T extends Enum> Converter<Boolean, T> getConverter(Class<T> targetType) {
		Class<?> enumType = getEnumType(targetType);
		return new BooleanToEnum(enumType);
	}

	private class BooleanToEnum<T extends Enum> implements Converter<Boolean, T> {

		private final Class<T> enumType;

		BooleanToEnum(Class<T> enumType) {
			this.enumType = enumType;
		}

		@Override
		public T convert(Boolean source) {
			Map<String, T> candidates = new LinkedHashMap<>();
			for (T candidate : (Set<T>) EnumSet.allOf(this.enumType)) {
				candidates.put(getCanonicalName(candidate.name()), candidate);
			}
			for (String alias : ALIASES.getOrDefault(source, Collections.emptyList())) {
				T result = candidates.get(alias);
				if (result != null) {
					return result;
				}
			}
			throw new IllegalArgumentException("No enum constant " + this.enumType.getCanonicalName() + "." + source);
		}

	}
}
