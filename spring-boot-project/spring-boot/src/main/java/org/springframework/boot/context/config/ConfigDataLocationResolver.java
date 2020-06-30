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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * Strategy interface used to resolve {@link ConfigDataLocation locations} from a String
 * based location address. Implementations should be added as a {@code spring.factories}
 * entries. The following constructor parameter types are supported:
 * <ul>
 * <li>{@link Log} - if the resolver needs deferred logging</li>
 * <li>{@link Binder} - if the resolver needs to obtain values from the initial
 * {@link Environment}</li>
 * </ul>
 * <p>
 * Resolvers may implement {@link Ordered} or use the {@link Order @Order} annotation. The
 * first resolver that supports the given location will be used.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public interface ConfigDataLocationResolver {

	/**
	 * Returns if the specified location address can be resolved by this resolver.
	 * @param binder a binder that can be used to obtain previously contributed values
	 * @param parent the parent location that specified the location or {@code null}
	 * @param location the location to check.
	 * @return if the location is supported by this loader
	 */
	boolean isResolvable(Binder binder, ConfigDataLocation parent, String location);

	/**
	 * Resolve a location string into one or more {@link ConfigDataLocation} instances.
	 * @param binder a binder that can be used to obtain previously contributed values
	 * @param parent the parent location that specified the location or {@code null}
	 * @param location the location that should be resolved
	 * @return a list of resolved locations in ascending priority order.
	 */
	List<ConfigDataLocation> resolve(Binder binder, ConfigDataLocation parent, String location);

	/**
	 * Resolve a location string into one or more {@link ConfigDataLocation} instances
	 * based on available profiles. This method is called once profiles have been deduced
	 * from the contributed values. By default this method returns an empty list.
	 * @param binder a binder that can be used to obtain previously contributed values
	 * @param parent the parent location that specified the location or {@code null}
	 * @param location the location that should be resolved
	 * @param profiles profile information
	 * @return a list of resolved locations in ascending priority order.
	 */
	default List<ConfigDataLocation> resolveProfileSpecific(Binder binder, ConfigDataLocation parent, String location,
			Profiles profiles) {
		return Collections.emptyList();
	}

}
