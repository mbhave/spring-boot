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

package org.springframework.boot.endpoint.web.mvc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.endpoint.EndpointInfo;
import org.springframework.boot.endpoint.OperationInvoker;
import org.springframework.boot.endpoint.ParameterMappingException;
import org.springframework.boot.endpoint.web.EndpointSecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.OperationRequestPredicate;
import org.springframework.boot.endpoint.web.RoleVerifier;
import org.springframework.boot.endpoint.web.SecurityConfiguration;
import org.springframework.boot.endpoint.web.WebEndpointOperation;
import org.springframework.boot.endpoint.web.WebEndpointResponse;
import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor;
import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor.SecurityResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

/**
 * A custom {@link RequestMappingInfoHandlerMapping} that makes web endpoints available
 * over HTTP using Spring MVC.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class WebEndpointServletHandlerMapping extends RequestMappingInfoHandlerMapping
		implements InitializingBean {

	private final Method handle = ReflectionUtils.findMethod(OperationHandler.class,
			"handle", HttpServletRequest.class, Map.class);

	private final Collection<EndpointInfo<WebEndpointOperation>> webEndpoints;

	private final CorsConfiguration corsConfiguration;

	private final EndpointSecurityConfigurationFactory securityConfigurationFactory;

	/**
	 * Creates a new {@code WebEndpointHandlerMapping} that provides mappings for the
	 * operations of the given {@code webEndpoints}.
	 * @param collection the web endpoints
	 */
	public WebEndpointServletHandlerMapping(
			Collection<EndpointInfo<WebEndpointOperation>> collection) {
		this(collection, null, null);
	}

	/**
	 * Creates a new {@code WebEndpointHandlerMapping} that provides mappings for the
	 * operations of the given {@code webEndpoints}.
	 * @param webEndpoints the web endpoints
	 * @param corsConfiguration the CORS configuraton for the endpoints
	 * @param securityConfigurationFactory the endpointSecurityConfigurationFactory
	 */
	public WebEndpointServletHandlerMapping(
			Collection<EndpointInfo<WebEndpointOperation>> webEndpoints,
			CorsConfiguration corsConfiguration, EndpointSecurityConfigurationFactory securityConfigurationFactory) {
		this.webEndpoints = webEndpoints;
		this.corsConfiguration = corsConfiguration;
		this.securityConfigurationFactory = securityConfigurationFactory;
		setOrder(-100);
	}

	@Override
	protected void initHandlerMethods() {
		for (EndpointInfo<WebEndpointOperation> webEndpoint : this.webEndpoints) {
			for (WebEndpointOperation webEndpointOperation : webEndpoint.getOperations()) {
				registerMappingForOperation(webEndpointOperation, webEndpoint.getId());
			}
		}
	}

	@Override
	protected CorsConfiguration initCorsConfiguration(Object handler, Method method,
			RequestMappingInfo mapping) {
		return this.corsConfiguration;
	}

	private void registerMappingForOperation(WebEndpointOperation operation, String id) {
		WebOperationSecurityInterceptor securityInterceptor = getSecurityInterceptor(id);
		registerMapping(createRequestMappingInfo(operation),
				new OperationHandler(operation.getOperationInvoker(), securityInterceptor), this.handle);
	}

	private WebOperationSecurityInterceptor getSecurityInterceptor(String id) {
		SecurityConfiguration configuration = new SecurityConfiguration(Collections.emptySet());
		if (this.securityConfigurationFactory != null) {
			configuration = this.securityConfigurationFactory.apply(id);
		}
		return new WebOperationSecurityInterceptor(configuration.getRoles());
	}

	private RequestMappingInfo createRequestMappingInfo(
			WebEndpointOperation operationInfo) {
		OperationRequestPredicate requestPredicate = operationInfo.getRequestPredicate();
		return new RequestMappingInfo(null,
				new PatternsRequestCondition(new String[] { requestPredicate.getPath() },
						null, null, false, false),
				new RequestMethodsRequestCondition(
						RequestMethod.valueOf(requestPredicate.getHttpMethod().name())),
				null, null,
				new ConsumesRequestCondition(
						toStringArray(requestPredicate.getConsumes())),
				new ProducesRequestCondition(
						toStringArray(requestPredicate.getProduces())),
				null);
	}

	private String[] toStringArray(Collection<String> collection) {
		return collection.toArray(new String[collection.size()]);
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return false;
	}

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method,
			Class<?> handlerType) {
		return null;
	}

	@Override
	protected void extendInterceptors(List<Object> interceptors) {
		interceptors.add(new SkipPathExtensionContentNegotiation());
	}

	/**
	 * A handler for an endpoint operation.
	 */
	final class OperationHandler {

		private final OperationInvoker operationInvoker;

		private final WebOperationSecurityInterceptor securityInterceptor;

		OperationHandler(OperationInvoker operationInvoker, WebOperationSecurityInterceptor securityInterceptor) {
			this.operationInvoker = operationInvoker;
			this.securityInterceptor = securityInterceptor;
		}

		@SuppressWarnings("unchecked")
		@ResponseBody
		public Object handle(HttpServletRequest request,
				@RequestBody(required = false) Map<String, String> body) {
			HttpServletRequestBasedRoleVerifier verifier = new HttpServletRequestBasedRoleVerifier(request);
			SecurityResponse response = this.securityInterceptor.handle(verifier);
			if (!response.equals(WebOperationSecurityInterceptor.SecurityResponse.SUCCESS)) {
				return sendFailureResponse(response);
			}
			Map<String, Object> arguments = new HashMap<>((Map<String, String>) request
					.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
			HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
			if (body != null && HttpMethod.POST == httpMethod) {
				arguments.putAll(body);
			}
			request.getParameterMap().forEach((name, values) -> arguments.put(name,
					values.length == 1 ? values[0] : Arrays.asList(values)));
			try {
				return handleResult(this.operationInvoker.invoke(arguments), httpMethod);
			}
			catch (ParameterMappingException ex) {
				return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
			}
		}

		private Object sendFailureResponse(SecurityResponse response) {
			return handleResult(new WebEndpointResponse<>(response.getFailureMessage(), response.getStatusCode()));
		}

		private Object handleResult(Object result) {
			return handleResult(result, null);
		}

		private Object handleResult(Object result, HttpMethod httpMethod) {
			if (result == null) {
				return new ResponseEntity<>(httpMethod == HttpMethod.GET
						? HttpStatus.NOT_FOUND : HttpStatus.NO_CONTENT);
			}
			if (!(result instanceof WebEndpointResponse)) {
				return result;
			}
			WebEndpointResponse<?> response = (WebEndpointResponse<?>) result;
			return new ResponseEntity<Object>(response.getBody(),
					HttpStatus.valueOf(response.getStatus()));
		}

	}

	/**
	 * {@link HandlerInterceptorAdapter} to ensure that
	 * {@link PathExtensionContentNegotiationStrategy} is skipped for actuator endpoints.
	 */
	private static final class SkipPathExtensionContentNegotiation
			extends HandlerInterceptorAdapter {

		private static final String SKIP_ATTRIBUTE = PathExtensionContentNegotiationStrategy.class
				.getName() + ".SKIP";

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
				Object handler) throws Exception {
			request.setAttribute(SKIP_ATTRIBUTE, Boolean.TRUE);
			return true;
		}

	}

	private final class HttpServletRequestBasedRoleVerifier implements RoleVerifier {

		private final HttpServletRequest request;

		HttpServletRequestBasedRoleVerifier(HttpServletRequest request) {
			this.request = request;
		}

		@Override
		public boolean isAuthenticated() {
			return (this.request.getUserPrincipal() != null);
		}

		@Override
		public boolean isUserInRole(String role) {
			return this.request.isUserInRole(role);
		}

	}


}
