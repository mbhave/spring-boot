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

package org.springframework.boot.context.config.cloud;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.context.config.ConfigDataLocation;

/**
 * @author Madhura Bhave
 */
public class ConsulConfigDataLocation extends ConfigDataLocation {

	private final ConsulClient consulClient;

	private final String context;

	private final ConsulConfigProperties properties;

	public ConsulConfigDataLocation(ConsulClient consulClient, String context, ConsulConfigProperties properties) {
		this.consulClient = consulClient;
		this.context = context;
		this.properties = properties;
	}

	public ConsulClient getConsulClient() {
		return this.consulClient;
	}

	public String getContext() {
		return this.context;
	}

	public ConsulConfigProperties getProperties() {
		return this.properties;
	}

}
