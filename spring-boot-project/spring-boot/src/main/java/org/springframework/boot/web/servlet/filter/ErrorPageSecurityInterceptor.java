/*
 * Copyright 2012-2021 the original author or authors.
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

package org.springframework.boot.web.servlet.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

/**
 * @author Madhura Bhave
 */
public class ErrorPageSecurityInterceptor implements Filter {

	private AccessDecisionManager accessDecisionManager;

	private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private FilterInvocationSecurityMetadataSource metadataSource;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		FilterInvocation filterInvocation = new FilterInvocation(request, response, chain);
		if (filterInvocation.getRequest().getDispatcherType().equals(DispatcherType.ERROR)) {
			try {
				attemptAuthorization(filterInvocation);
				filterInvocation.getChain().doFilter(filterInvocation.getRequest(), filterInvocation.getResponse());
			}
			catch (AccessDeniedException accessDeniedException) {
				HttpServletResponse invocationResponse = filterInvocation.getResponse();
				invocationResponse.sendError(invocationResponse.getStatus(),
						HttpStatus.valueOf(filterInvocation.getResponse().getStatus()).getReasonPhrase());
			}
		}
		else {
			filterInvocation.getChain().doFilter(filterInvocation.getRequest(), filterInvocation.getResponse());
		}
	}

	private void attemptAuthorization(FilterInvocation invocation) {
		Collection<ConfigAttribute> attributes = this.metadataSource.getAttributes(invocation);
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			credentialsNotFound(this.messages.getMessage("AbstractSecurityInterceptor.authenticationNotFound",
					"An Authentication object was not found in the SecurityContext"));
		}
		Authentication authenticated = SecurityContextHolder.getContext().getAuthentication();
		this.accessDecisionManager.decide(authenticated, invocation, attributes);
	}

	private void credentialsNotFound(String reason) {
		throw new AuthenticationCredentialsNotFoundException(reason);
	}

	public void setMetadataSource(FilterInvocationSecurityMetadataSource metadataSource) {
		this.metadataSource = metadataSource;
	}

	public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
		this.accessDecisionManager = accessDecisionManager;
	}

}
