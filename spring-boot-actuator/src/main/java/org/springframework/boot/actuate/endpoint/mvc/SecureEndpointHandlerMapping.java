/*
 * Copyright 2012-2016 the original author or authors.
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

package org.springframework.boot.actuate.endpoint.mvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * {@link HandlerMapping} to map {@link Endpoint}s which can be configured with an
 * additional {@link HandlerInterceptor}.
 *
 * @param <E> The endpoint type
 * @author Madhura Bhave
 */
public abstract class SecureEndpointHandlerMapping<E extends MvcEndpoint>
		extends AbstractEndpointHandlerMapping<E> {

	private HandlerInterceptor securityInterceptor;

	public SecureEndpointHandlerMapping(Collection<? extends E> endpoints) {
		super(endpoints);
	}

	public SecureEndpointHandlerMapping(Collection<? extends E> endpoints,
			CorsConfiguration corsConfiguration) {
		super(endpoints, corsConfiguration);
	}

	public void setSecurityInterceptor(HandlerInterceptor securityInterceptor) {
		this.securityInterceptor = securityInterceptor;
	}

	@Override
	protected HandlerExecutionChain getCorsHandlerExecutionChain(
			HttpServletRequest request, HandlerExecutionChain chain,
			CorsConfiguration config) {
		chain = super.getCorsHandlerExecutionChain(request, chain, config);
		HandlerInterceptor[] interceptors = addSecurityInterceptor(
				chain.getInterceptors());
		return new HandlerExecutionChain(chain.getHandler(), interceptors);
	}

	private HandlerInterceptor[] addSecurityInterceptor(HandlerInterceptor[] existing) {
		List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();
		if (existing != null) {
			interceptors.addAll(Arrays.asList(existing));
		}
		interceptors.add(this.securityInterceptor);
		return interceptors.toArray(new HandlerInterceptor[interceptors.size()]);
	}
}
