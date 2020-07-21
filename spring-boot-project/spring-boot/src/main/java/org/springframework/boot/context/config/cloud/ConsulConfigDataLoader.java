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

import java.io.IOException;
import java.util.Collections;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;

import static org.springframework.boot.context.config.cloud.ConsulConfigProperties.Format.FILES;

/**
 * @author Madhura Bhave
 */
public class ConsulConfigDataLoader implements ConfigDataLoader<ConsulConfigDataLocation> {

	@Override
	public ConfigData load(ConsulConfigDataLocation location) throws IOException {
		ConsulPropertySource propertySource = null;
		String context = location.getContext();
		ConsulConfigProperties properties = location.getProperties();
		ConsulClient consulClient = location.getConsulClient();
		if (properties.getFormat() == FILES) {
			Response<GetValue> response = consulClient.getKVValue(context, properties.getAclToken());
			if (response.getValue() != null) {
				ConsulFilesPropertySource filesPropertySource = new ConsulFilesPropertySource(context, consulClient,
						properties);
				filesPropertySource.init(response.getValue());
				propertySource = filesPropertySource;
			}
		}
		else {
			propertySource = create(context, consulClient, properties);
		}
		return new ConfigData(Collections.singletonList(propertySource));
	}

	private ConsulPropertySource create(String context, ConsulClient consulClient, ConsulConfigProperties properties) {
		ConsulPropertySource propertySource = new ConsulPropertySource(context, consulClient, properties);
		propertySource.init();
		return propertySource;
	}

}
