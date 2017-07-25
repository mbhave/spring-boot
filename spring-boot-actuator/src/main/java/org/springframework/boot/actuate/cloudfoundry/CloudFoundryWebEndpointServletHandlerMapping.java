package org.springframework.boot.actuate.cloudfoundry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.endpoint.EndpointInfo;
import org.springframework.boot.endpoint.OperationInvoker;
import org.springframework.boot.endpoint.ParameterMappingException;
import org.springframework.boot.endpoint.web.WebEndpointOperation;
import org.springframework.boot.endpoint.web.WebEndpointResponse;
import org.springframework.boot.endpoint.web.WebOperationSecurityInterceptor;
import org.springframework.boot.endpoint.web.mvc.WebEndpointServletHandlerMapping;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Madhura Bhave
 */
public class CloudFoundryWebEndpointServletHandlerMapping extends WebEndpointServletHandlerMapping {

	private final HandlerInterceptor securityInterceptor;

	private final Method handle = ReflectionUtils.findMethod(OperationHandler.class,
			"handle", HttpServletRequest.class, Map.class);

	public CloudFoundryWebEndpointServletHandlerMapping(Collection<EndpointInfo<WebEndpointOperation>> webEndpoints, CorsConfiguration corsConfiguration, HandlerInterceptor interceptor) {
		super(webEndpoints, corsConfiguration, null);
		this.securityInterceptor = interceptor;
	}

	@Override
	protected void initHandlerMethods() {
		for (EndpointInfo<WebEndpointOperation> webEndpoint : getWebEndpoints()) {
			for (WebEndpointOperation webEndpointOperation : webEndpoint.getOperations()) {
				registerMappingForOperation(webEndpointOperation);
			}
		}
	}

	private void registerMappingForOperation(WebEndpointOperation operation) {
		registerMapping(createRequestMappingInfo(operation),
				new OperationHandler(operation.getOperationInvoker(), this.securityInterceptor), this.handle);
	}

	/**
	 * A handler for an endpoint operation.
	 */
	final class OperationHandler {

		private final OperationInvoker operationInvoker;

		OperationHandler(OperationInvoker operationInvoker, HandlerInterceptor securityInterceptor) {
			this.operationInvoker = operationInvoker;
		}

		@SuppressWarnings("unchecked")
		@ResponseBody
		public Object handle(HttpServletRequest request,
				@RequestBody(required = false) Map<String, String> body) {
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

		private Object sendFailureResponse(WebOperationSecurityInterceptor.SecurityResponse response) {
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

	class ServletAuthorizationChecker implements AuthorizationCheckerDunno {

		private final HttpServletRequest request;

		ServletAuthorizationChecker(HttpServletRequest request) {
			this.request = request;
		}

		@Override
		public boolean shouldCheck() {
			if (CorsUtils.isPreFlightRequest(this.request)) {
				return true;
			}
		}

		@Override
		public boolean isAuthorized() {
			 try {

			 HandlerMethod handlerMethod = (HandlerMethod) handler;
			 if (HttpMethod.OPTIONS.matches(request.getMethod())
			 && !(handlerMethod.getBean() instanceof MvcEndpoint)) {
			 return true;
			 }
			 MvcEndpoint mvcEndpoint = (MvcEndpoint) handlerMethod.getBean();
			 check(request, mvcEndpoint);
			 }
			 catch (CloudFoundryAuthorizationException ex) {
			 logger.error(ex);
			 response.setContentType(MediaType.APPLICATION_JSON.toString());
			 response.getWriter()
			 .write("{\"security_error\":\"" + ex.getMessage() + "\"}");
			 response.setStatus(ex.getStatusCode().value());
			 return false;
			 }
			return true;
		}
	}


}
