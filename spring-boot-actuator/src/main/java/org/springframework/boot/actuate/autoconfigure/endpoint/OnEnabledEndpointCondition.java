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

package org.springframework.boot.actuate.autoconfigure.endpoint;

import org.springframework.boot.actuate.autoconfigure.endpoint.support.EndpointEnablement;
import org.springframework.boot.actuate.autoconfigure.endpoint.support.EndpointEnablementProvider;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.endpoint.Endpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A condition that checks if an endpoint is enabled.
 *
 * @author Stephane Nicoll
 */
class OnEnabledEndpointCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		EndpointAttributes endpoint = getEndpointAttributes(context, metadata);
		if (!StringUtils.hasText(endpoint.id)) {
			throw new IllegalStateException("Endpoint id could not be determined");
		}
		EndpointEnablementProvider enablementProvider = new EndpointEnablementProvider(
				context.getEnvironment());
		EndpointEnablement endpointEnablement = enablementProvider
				.getEndpointEnablement(endpoint.id, endpoint.enabled);
		return new ConditionOutcome(endpointEnablement.isEnabled(),
				ConditionMessage.forCondition(ConditionalOnEnabledEndpoint.class)
						.because(endpointEnablement.getReason()));
	}

	private EndpointAttributes getEndpointAttributes(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		if (metadata instanceof MethodMetadata
				&& metadata.isAnnotated(Bean.class.getName())) {
			MethodMetadata methodMetadata = (MethodMetadata) metadata;
			try {
				// We should be safe to load at this point since we are in the
				// REGISTER_BEAN phase
				Class<?> returnType = ClassUtils.forName(methodMetadata.getReturnTypeName(),
						context.getClassLoader());
				return extractEndpointAttributes(returnType);
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Failed to extract endpoint id for "
						+ methodMetadata.getDeclaringClassName() + "."
						+ methodMetadata.getMethodName(), ex);
			}
		}
		return null;
	}

	protected EndpointAttributes extractEndpointAttributes(Class<?> type) {
		Endpoint annotation = AnnotationUtils.findAnnotation(type, Endpoint.class);
		if (annotation != null) {
			return new EndpointAttributes(annotation.id(), annotation.enabledByDefault());
		}
		return null;
	}

	private static class EndpointAttributes {

		private final String id;

		private final boolean enabled;

		EndpointAttributes(String id, boolean enabled) {
			this.id = id;
			this.enabled = enabled;
		}

	}

}
