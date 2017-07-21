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

package org.springframework.boot.endpoint.web.jersey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.Resource.Builder;

import org.springframework.boot.endpoint.EndpointInfo;
import org.springframework.boot.endpoint.OperationInvoker;
import org.springframework.boot.endpoint.ParameterMappingException;
import org.springframework.boot.endpoint.web.OperationRequestPredicate;
import org.springframework.boot.endpoint.web.RoleVerifier;
import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor.SecurityResponse;
import org.springframework.boot.endpoint.web.SecurityConfiguration;
import org.springframework.boot.endpoint.web.SecurityConfigurationFactory;
import org.springframework.boot.endpoint.web.WebEndpointOperation;
import org.springframework.boot.endpoint.web.WebEndpointResponse;
import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor;
import org.springframework.util.CollectionUtils;

/**
 * A factory for creating Jersey {@link Resource Resources} for web endpoint operations.
 *
 * @author Andy Wilkinson
 */
public class JerseyEndpointResourceFactory {

	private final SecurityConfigurationFactory securityConfigurationFactory;

	public JerseyEndpointResourceFactory(SecurityConfigurationFactory securityConfigurationFactory) {
		this.securityConfigurationFactory = securityConfigurationFactory;
	}

	/**
	 * Creates {@link Resource Resources} for the operations of the given
	 * {@code webEndpoints}.
	 * @param webEndpoints the web endpoints
	 * @return the resources for the operations
	 */
	public Collection<Resource> createEndpointResources(
			Collection<EndpointInfo<WebEndpointOperation>> webEndpoints) {
		List<Resource> list = new ArrayList<>();
		for (EndpointInfo<WebEndpointOperation> endpointInfo : webEndpoints) {
			for (WebEndpointOperation webEndpointOperation : endpointInfo.getOperations()) {
				Resource resource = createResource(webEndpointOperation, endpointInfo.getId());
				list.add(resource);
			}
		}
		return list;
	}

	private Resource createResource(WebEndpointOperation operation, String id) {
		SecurityConfiguration configuration = this.securityConfigurationFactory.apply(id);
		WebOperationSecurityInterceptor securityInterceptor = new WebOperationSecurityInterceptor(configuration.getRoles());
		OperationRequestPredicate requestPredicate = operation.getRequestPredicate();
		Builder resourceBuilder = Resource.builder().path(requestPredicate.getPath());
		resourceBuilder.addMethod(requestPredicate.getHttpMethod().name())
				.consumes(toStringArray(requestPredicate.getConsumes()))
				.produces(toStringArray(requestPredicate.getProduces()))
				.handledBy(new EndpointInvokingInflector(operation.getOperationInvoker(),
						!requestPredicate.getConsumes().isEmpty(), securityInterceptor));
		return resourceBuilder.build();
	}

	private String[] toStringArray(Collection<String> collection) {
		return collection.toArray(new String[collection.size()]);
	}

	private static final class EndpointInvokingInflector
			implements Inflector<ContainerRequestContext, Object> {

		private final OperationInvoker operationInvoker;

		private final boolean readBody;

		private final WebOperationSecurityInterceptor securityInterceptor;

		private EndpointInvokingInflector(OperationInvoker operationInvoker,
				boolean readBody, WebOperationSecurityInterceptor securityInterceptor) {
			this.operationInvoker = operationInvoker;
			this.readBody = readBody;
			this.securityInterceptor = securityInterceptor;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Response apply(ContainerRequestContext data) {
			SecurityContextBasedRoleVerifier verifier = new SecurityContextBasedRoleVerifier(data.getSecurityContext());
			SecurityResponse response = this.securityInterceptor.handle(verifier);
			if (!response.equals(WebOperationSecurityInterceptor.SecurityResponse.SUCCESS)) {
				sendFailureResponse(response);
			}
			Map<String, Object> arguments = new HashMap<>();
			if (this.readBody) {
				Map<String, Object> body = ((ContainerRequest) data)
						.readEntity(Map.class);
				if (body != null) {
					arguments.putAll(body);
				}
			}
			arguments.putAll(extractPathParmeters(data));
			arguments.putAll(extractQueryParmeters(data));
			try {
				return convertToJaxRsResponse(this.operationInvoker.invoke(arguments),
						data.getRequest().getMethod());
			}
			catch (ParameterMappingException ex) {
				return Response.status(Status.BAD_REQUEST).build();
			}
		}

		private Map<String, Object> extractPathParmeters(
				ContainerRequestContext requestContext) {
			return extract(requestContext.getUriInfo().getPathParameters());
		}

		private Map<String, Object> extractQueryParmeters(
				ContainerRequestContext requestContext) {
			return extract(requestContext.getUriInfo().getQueryParameters());
		}

		private Map<String, Object> extract(
				MultivaluedMap<String, String> multivaluedMap) {
			Map<String, Object> result = new HashMap<>();
			multivaluedMap.forEach((name, values) -> {
				if (!CollectionUtils.isEmpty(values)) {
					result.put(name, values.size() == 1 ? values.get(0) : values);
				}
			});
			return result;
		}

		private Object sendFailureResponse(SecurityResponse response) {
			return convertToJaxRsResponse(new WebEndpointResponse<>(response.getFailureMessage(), response.getStatusCode()));
		}

		private Response convertToJaxRsResponse(Object response) {
			return convertToJaxRsResponse(response, null);
		}

		private Response convertToJaxRsResponse(Object response, String httpMethod) {
			if (response == null) {
				return Response.status(HttpMethod.GET.equals(httpMethod)
						? Status.NOT_FOUND : Status.NO_CONTENT).build();
			}
			if (!(response instanceof WebEndpointResponse)) {
				if (response instanceof org.springframework.core.io.Resource) {
					try {
						return Response.status(Status.OK)
								.entity(((org.springframework.core.io.Resource) response)
										.getInputStream())
								.build();
					}
					catch (IOException ex) {
						return Response.status(Status.INTERNAL_SERVER_ERROR).build();
					}
				}
				return Response.status(Status.OK).entity(response).build();
			}
			WebEndpointResponse<?> webEndpointResponse = (WebEndpointResponse<?>) response;
			return Response.status(webEndpointResponse.getStatus())
					.entity(webEndpointResponse.getBody()).build();
		}

	}

	private static final class SecurityContextBasedRoleVerifier implements RoleVerifier {

		private final SecurityContext context;

		public SecurityContextBasedRoleVerifier(SecurityContext context) {
			this.context = context;
		}

		@Override
		public boolean isAuthenticated() {
			return (this.context.getUserPrincipal() != null);
		}

		@Override
		public boolean isUserInRole(String role) {
			return this.context.isUserInRole(role);
		}

	}

}
