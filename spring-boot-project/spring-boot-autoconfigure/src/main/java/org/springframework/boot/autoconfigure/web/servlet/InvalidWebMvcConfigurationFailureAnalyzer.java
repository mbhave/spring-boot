/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.autoconfigure.web.servlet;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Madhura Bhave
 */
public class InvalidWebMvcConfigurationFailureAnalyzer
		extends AbstractFailureAnalyzer<InvalidWebMvcConfigurationException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, InvalidWebMvcConfigurationException cause) {
		return new FailureAnalysis(
				"Invalid Spring Web MVC configuration detected. A bean of type WebMvcConfigurationSupport was found, either due to the presence of @EnableWebMvc or explicit bean declaration.",
				"Verify your configuration, remove @EnableWebMvc or the bean declaration for WebMvcConfigurationSupport"
						+ ", or set spring.mvc.use-manual-configuration to true.",
				cause);
	}

}
