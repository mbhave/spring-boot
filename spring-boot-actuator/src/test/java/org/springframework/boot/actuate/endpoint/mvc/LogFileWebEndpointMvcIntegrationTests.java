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

package org.springframework.boot.actuate.endpoint.mvc;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointInfrastructureAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointServletWebAutoConfiguration;
import org.springframework.boot.actuate.endpoint.web.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.ContextLoader;
import org.springframework.boot.test.context.ServletWebContextLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link LogFileWebEndpoint} exposed by Spring MVC.
 *
 * @author Andy Wilkinson
 */
public class LogFileWebEndpointMvcIntegrationTests {

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	private File logFile;

	@Before
	public void setUp() throws IOException {
		this.logFile = this.temp.newFile();
		FileCopyUtils.copy("--TEST--".getBytes(), this.logFile);
	}

	@Test
	public void getRequestProduces404ResponseWhenLogFileNotFound() throws Exception {
		ServletWebContextLoader contextLoader = ContextLoader.servletWeb()
				.autoConfig(JacksonAutoConfiguration.class,
						HttpMessageConvertersAutoConfiguration.class,
						WebMvcAutoConfiguration.class,
						DispatcherServletAutoConfiguration.class,
						EndpointInfrastructureAutoConfiguration.class,
						EndpointServletWebAutoConfiguration.class)
				.config(TestConfiguration.class);
		contextLoader.loadWeb((context) -> {
			MockMvcBuilders.webAppContextSetup(context).build()
					.perform(get("/application/logfile"))
					.andExpect(status().isNotFound());
		});
	}

	@Test
	public void getRequestProducesResponseWithLogFile() throws Exception {
		ServletWebContextLoader contextLoader = ContextLoader.servletWeb()
				.autoConfig(JacksonAutoConfiguration.class,
						HttpMessageConvertersAutoConfiguration.class,
						WebMvcAutoConfiguration.class,
						DispatcherServletAutoConfiguration.class,
						EndpointInfrastructureAutoConfiguration.class,
						EndpointServletWebAutoConfiguration.class)
				.config(TestConfiguration.class)
				.env("logging.file:" + this.logFile.getAbsolutePath());
		contextLoader.loadWeb((context) -> {
			MockMvcBuilders.webAppContextSetup(context).build()
					.perform(get("/application/logfile")).andExpect(status().isOk())
					.andExpect(content().string("--TEST--"));
		});
	}

	// TODO Test HEAD and range requests once they are supported

	static class TestConfiguration {

		@Bean
		public LogFileWebEndpoint logFileEndpoint(Environment environment) {
			return new LogFileWebEndpoint(environment);
		}

	}

}
