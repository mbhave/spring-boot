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

package org.springframework.boot.endpoint.web.reactive;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.endpoint.EndpointInfo;
import org.springframework.boot.endpoint.EndpointOperationType;
import org.springframework.boot.endpoint.OperationInvoker;
import org.springframework.boot.endpoint.ParameterMappingException;
import org.springframework.boot.endpoint.web.OperationRequestPredicate;
import org.springframework.boot.endpoint.web.SecurityConfiguration;
import org.springframework.boot.endpoint.web.SecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.WebEndpointOperation;
import org.springframework.boot.endpoint.web.WebEndpointResponse;
import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.condition.ConsumesRequestCondition;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.condition.ProducesRequestCondition;
import org.springframework.web.reactive.result.condition.RequestMethodsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * A custom {@link RequestMappingInfoHandlerMapping} that makes web endpoints available
 * over HTTP using Spring WebFlux.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class WebEndpointReactiveHandlerMapping extends RequestMappingInfoHandlerMapping
		implements InitializingBean {

	private static final PathPatternParser pathPatternParser = new PathPatternParser();

	private final Method handleRead = ReflectionUtils
			.findMethod(ReadOperationHandler.class, "handle", ServerWebExchange.class);

	private final Method handleWrite = ReflectionUtils.findMethod(
			WriteOperationHandler.class, "handle", ServerWebExchange.class, Map.class);

	private final Collection<EndpointInfo<WebEndpointOperation>> webEndpoints;

	private final CorsConfiguration corsConfiguration;

	private final SecurityConfigurationFactory securityConfigurationFactory;

	/**
	 * Creates a new {@code WebEndpointHandlerMapping} that provides mappings for the
	 * operations of the given {@code webEndpoints}.
	 * @param collection the web endpoints
	 */
	public WebEndpointReactiveHandlerMapping(
			Collection<EndpointInfo<WebEndpointOperation>> collection) {
		this(collection, null, null);
	}

	/**
	 * Creates a new {@code WebEndpointHandlerMapping} that provides mappings for the
	 * operations of the given {@code webEndpoints}.
	 * @param webEndpoints the web endpoints
	 * @param corsConfiguration the CORS configuraton for the endpoints
	 * @param securityConfigurationFactory
	 */
	public WebEndpointReactiveHandlerMapping(
			Collection<EndpointInfo<WebEndpointOperation>> webEndpoints,
			CorsConfiguration corsConfiguration, SecurityConfigurationFactory securityConfigurationFactory) {
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
		SecurityConfiguration configuration = this.securityConfigurationFactory.apply(id);
		WebOperationSecurityInterceptor interceptor = new WebOperationSecurityInterceptor(configuration.getRoles());
		EndpointOperationType operationType = operation.getType();
		registerMapping(createRequestMappingInfo(operation),
				operationType == EndpointOperationType.WRITE
						? new WriteOperationHandler(operation.getOperationInvoker(), interceptor)
						: new ReadOperationHandler(operation.getOperationInvoker(), interceptor),
				operationType == EndpointOperationType.WRITE ? this.handleWrite
						: this.handleRead);
	}

	private RequestMappingInfo createRequestMappingInfo(
			WebEndpointOperation operationInfo) {
		OperationRequestPredicate requestPredicate = operationInfo.getRequestPredicate();
		return new RequestMappingInfo(null,
				new PatternsRequestCondition(
						pathPatternParser.parse(requestPredicate.getPath())),
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

	/**
	 * Base class for handlers for endpoint operations.
	 */
	abstract class AbstractOperationHandler {

		private final OperationInvoker operationInvoker;

		private final WebOperationSecurityInterceptor securityInterceptor;

		AbstractOperationHandler(OperationInvoker operationInvoker, WebOperationSecurityInterceptor securityInterceptor) {
			this.operationInvoker = operationInvoker;
			this.securityInterceptor = securityInterceptor;
		}

		@SuppressWarnings("unchecked")
		ResponseEntity<?> doHandle(ServerWebExchange exchange, Map<String, String> body) {
			//TODO security interceptor
			Map<String, Object> arguments = new HashMap<>((Map<String, String>) exchange
					.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
			if (body != null) {
				arguments.putAll(body);
			}
			exchange.getRequest().getQueryParams().forEach((name, values) -> {
				arguments.put(name, values.size() == 1 ? values.get(0) : values);
			});
			try {
				return handleResult(this.operationInvoker.invoke(arguments),
						exchange.getRequest().getMethod());
			}
			catch (ParameterMappingException ex) {
				return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
			}
		}

		private ResponseEntity<?> handleResult(Object result, HttpMethod httpMethod) {
			if (result == null) {
				return new ResponseEntity<>(httpMethod == HttpMethod.GET
						? HttpStatus.NOT_FOUND : HttpStatus.NO_CONTENT);
			}
			if (!(result instanceof WebEndpointResponse)) {
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			WebEndpointResponse<?> response = (WebEndpointResponse<?>) result;
			return new ResponseEntity<Object>(response.getBody(),
					HttpStatus.valueOf(response.getStatus()));
		}

	}

	/**
	 * A handler for an endpoint write operation.
	 */
	final class WriteOperationHandler extends AbstractOperationHandler {

		WriteOperationHandler(OperationInvoker operationInvoker, WebOperationSecurityInterceptor securityInterceptor) {
			super(operationInvoker, securityInterceptor);
		}

		@ResponseBody
		public ResponseEntity<?> handle(ServerWebExchange exchange,
				@RequestBody(required = false) Map<String, String> body) {
			return doHandle(exchange, body);
		}

	}

	/**
	 * A handler for an endpoint write operation.
	 */
	final class ReadOperationHandler extends AbstractOperationHandler {

		ReadOperationHandler(OperationInvoker operationInvoker, WebOperationSecurityInterceptor securityInterceptor) {
			super(operationInvoker, securityInterceptor);
		}

		@ResponseBody
		public ResponseEntity<?> handle(ServerWebExchange exchange) {
			return doHandle(exchange, null);
		}

	}

}
