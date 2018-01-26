/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.boot.autoconfigure.security.servlet;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.ClassUtils;

/**
 * Configuration for a Spring Security in-memory {@link AuthenticationManager}. Adds an
 * {@link InMemoryUserDetailsManager} with a default user and generated password. This can
 * be disabled by providing a bean of type {@link AuthenticationManager},
 * {@link AuthenticationProvider} or {@link UserDetailsService}.
 *
 * @author Dave Syer
 * @author Rob Winch
 * @author Madhura Bhave
 */
@Configuration
@ConditionalOnBean(ObjectPostProcessor.class)
@ConditionalOnMissingBean({ AuthenticationManager.class, AuthenticationProvider.class,
		UserDetailsService.class })
public class AuthenticationManagerConfiguration {

	private static final String NOOP_PASSWORD_PREFIX = "{noop}";

	private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern
			.compile("^\\{.+}.*$");

	private static final Log logger = LogFactory
			.getLog(AuthenticationManagerConfiguration.class);

	@Bean
	@Conditional(OnUserCreateModeCondition.class)
	public InMemoryUserDetailsManager inMemoryUserDetailsManager(
			SecurityProperties properties,
			ObjectProvider<PasswordEncoder> passwordEncoder) throws Exception {
		SecurityProperties.User user = properties.getUser();
		List<String> roles = user.getRoles();
		return new InMemoryUserDetailsManager(
				User.withUsername(user.getName())
						.password(getOrDeducePassword(user,
								passwordEncoder.getIfAvailable()))
				.roles(roles.toArray(new String[roles.size()])).build());
	}

	public String getOrDeducePassword(SecurityProperties.User user,
			PasswordEncoder encoder) {
		String password = user.getPassword();
		if (user.isPasswordGenerated()) {
			logger.info(String.format("%n%nUsing generated security password: %s%n",
					user.getPassword()));
		}
		if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
			return password;
		}
		return NOOP_PASSWORD_PREFIX + password;
	}

	static class OnUserCreateModeCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			ConditionMessage.Builder message = ConditionMessage
					.forCondition("In-memory user Condition");
			Environment environment = context.getEnvironment();
			String createMode = environment.getProperty("spring.security.user.create-mode");
			SecurityProperties.InMemoryUserCreateMode mode = (createMode == null ? SecurityProperties.InMemoryUserCreateMode.DEFAULT : SecurityProperties.InMemoryUserCreateMode.valueOf(createMode));
			if (mode.equals(SecurityProperties.InMemoryUserCreateMode.ALWAYS_ON)) {
				return ConditionOutcome.match(message.because("In-memory user create mode is ALWAYS_ON"));
			}
			if (mode.equals(SecurityProperties.InMemoryUserCreateMode.ALWAYS_OFF)) {
				return ConditionOutcome.noMatch(message.because("In-memory user create mode is ALWAYS_OFF"));
			}
			if (isOAuth2Configured(context)) {
				return ConditionOutcome.noMatch(message.because("In-memory user create mode is DEFAULT and OAuth2 is configured."));
			}
			return ConditionOutcome.match(message.because("In-memory user create mode is ON"));
		}

		private boolean isOAuth2Configured(ConditionContext context) {
			try {
				Class<?> oAuthClass = ClassUtils.forName("org.springframework.security.oauth2.client.registration.ClientRegistrationRepository", context.getClassLoader());
				String[] beanNames = context.getBeanFactory().getBeanNamesForType(oAuthClass);
				if (beanNames.length < 1) {
					return false;
				}
			}
			catch (ClassNotFoundException | NoClassDefFoundError ex) {
				return false;
			}
			return true;
		}


	}

}
