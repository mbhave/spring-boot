/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.actuate.autoconfigure.endpoint.support.EndpointEnablementProvider;
import org.springframework.boot.endpoint.Endpoint;
import org.springframework.boot.endpoint.EndpointInfo;
import org.springframework.boot.endpoint.EndpointType;
import org.springframework.boot.endpoint.jmx.EndpointMBean;
import org.springframework.boot.endpoint.jmx.JmxAnnotationEndpointDiscoverer;
import org.springframework.boot.endpoint.jmx.JmxEndpointMBeanFactory;
import org.springframework.boot.endpoint.jmx.JmxEndpointOperation;
import org.springframework.core.env.Environment;
import org.springframework.jmx.JmxException;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.StringUtils;

/**
 * Exports all available {@link Endpoint}.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
class JmxEndpointExporter {

	private final EndpointEnablementProvider endpointEnablementProvider;

	private final MBeanExporter mBeanExporter;

	private final JmxAnnotationEndpointDiscoverer endpointDiscoverer;

	private final JmxEndpointMBeanFactory mBeanFactory;

	JmxEndpointExporter(Environment environment, MBeanExporter mBeanExporter,
			JmxAnnotationEndpointDiscoverer endpointDiscoverer,
			ObjectMapper objectMapper) {
		this.endpointEnablementProvider = new EndpointEnablementProvider(environment);
		this.mBeanExporter = mBeanExporter;
		this.endpointDiscoverer = endpointDiscoverer;
		DataConverter dataConverter = new DataConverter(objectMapper);
		this.mBeanFactory = new JmxEndpointMBeanFactory(dataConverter::convert);
	}

	@PostConstruct
	public void exportMBeans() {
		this.mBeanFactory.createMBeans(getEnabledJmxEndpoints()).forEach(this::register);
	}

	private Collection<EndpointInfo<JmxEndpointOperation>> getEnabledJmxEndpoints() {
		return this.endpointDiscoverer.discoverEndpoints().stream()
				.filter(endpoint -> this.endpointEnablementProvider.getEndpointEnablement(
						endpoint.getId(), endpoint.isEnabledByDefault(),
						EndpointType.JMX).isEnabled()).collect(Collectors.toList());
	}

	private void register(EndpointMBean mBean) {
		try {
			ObjectName objectName = createObjectName(mBean);
			this.mBeanExporter.registerManagedResource(mBean, objectName);
		}
		catch (MalformedObjectNameException | JmxException ex) {
			throw new IllegalStateException(
					String.format("Failed to register Endpoint with id '%s'",
							mBean.getEndpointId()),
					ex);
		}
	}

	private String domain = "org.springframework.boot";

	private ObjectName createObjectName(EndpointMBean mBean)
			throws MalformedObjectNameException {
		StringBuilder builder = new StringBuilder();
		builder.append(this.domain);
		builder.append(":type=Endpoint");
		builder.append(",name=" + StringUtils.capitalize(mBean.getEndpointId()));
		return ObjectNameManager.getInstance(builder.toString());
	}

	static class DataConverter {

		private final ObjectMapper objectMapper;

		private final JavaType listObject;

		private final JavaType mapStringObject;

		DataConverter(ObjectMapper objectMapper) {
			this.objectMapper = (objectMapper == null ? new ObjectMapper()
					: objectMapper);
			this.listObject = this.objectMapper.getTypeFactory()
					.constructParametricType(List.class, Object.class);
			this.mapStringObject = this.objectMapper.getTypeFactory()
					.constructParametricType(Map.class, String.class, Object.class);

		}

		public Object convert(Object data) {
			if (data == null) {
				return null;
			}
			if (data instanceof String) {
				return data;
			}
			if (data.getClass().isArray() || data instanceof Collection) {
				return this.objectMapper.convertValue(data, this.listObject);
			}
			return this.objectMapper.convertValue(data, this.mapStringObject);
		}

	}

}
