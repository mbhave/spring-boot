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

package org.springframework.boot.logging;

import java.util.List;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to ensure that {@link LogbackLoggingSystem} and {@link Log4J2LoggingSystem} are
 * aligned w.r.t loggers returned from the logging system.
 *
 * @author Madhura Bhave
 */
class LogbackAndLog4J2AlignmentTests {

	private final LogbackLoggingSystem logbackLoggingSystem = new LogbackLoggingSystem(getClass().getClassLoader());

	private final Log4J2LoggingSystem log4J2LoggingSystem = new Log4J2LoggingSystem(getClass().getClassLoader());

	private Logger logbackLogger;

	private org.apache.logging.log4j.Logger log4J2Logger;

	private LoggingInitializationContext initializationContext;

	private Configuration configuration;

	private static LoggerRegistry<org.apache.logging.log4j.core.Logger> loggerRegistry;

	@BeforeEach
	void setup() {
		this.logbackLoggingSystem.cleanUp();
		this.logbackLogger = ((LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory())
				.getLogger(getClass());
		MockEnvironment environment = new MockEnvironment();
		Nested nested = new Nested();
		this.initializationContext = new LoggingInitializationContext(environment);
		org.apache.logging.log4j.core.LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager
				.getContext(false);
		this.configuration = loggerContext.getConfiguration();
		this.log4J2LoggingSystem.cleanUp();
		this.log4J2Logger = LogManager.getLogger(getClass());
		if (loggerRegistry == null) {
			loggerRegistry = (LoggerRegistry<org.apache.logging.log4j.core.Logger>) ReflectionTestUtils
					.getField(loggerContext, "loggerRegistry");
		}
		ReflectionTestUtils.setField(loggerContext, "loggerRegistry", loggerRegistry);
	}

	@AfterEach
	void cleanUp() {
		this.logbackLoggingSystem.cleanUp();
		((LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory()).stop();
		this.log4J2LoggingSystem.cleanUp();
		org.apache.logging.log4j.core.LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager
				.getContext(false);
		loggerContext.stop();
		loggerContext.start(((Reconfigurable) this.configuration).reconfigure());
	}

	@Test
	void getLoggerConfigurationsShouldBeAligned() {
		initialize();
		List<LoggerConfiguration> log4J2configurations = this.log4J2LoggingSystem.getLoggerConfigurations();
		List<LoggerConfiguration> logbackConfigurations = this.logbackLoggingSystem.getLoggerConfigurations();
		assertThat(log4J2configurations).isNotEmpty();
		assertThat(log4J2configurations.get(0).getName()).isEqualTo(LoggingSystem.ROOT_LOGGER_NAME);
		assertThat(logbackConfigurations).isNotEmpty();
		assertThat(logbackConfigurations.get(0).getName()).isEqualTo(LoggingSystem.ROOT_LOGGER_NAME);
		assertThat(log4J2configurations.size()).isEqualTo(logbackConfigurations.size());
		List<String> log4j2 = log4J2configurations.stream().map(LoggerConfiguration::getName)
				.collect(Collectors.toList());
		List<String> logback = logbackConfigurations.stream().map(LoggerConfiguration::getName)
				.collect(Collectors.toList());
		assertThat(log4j2).containsExactly(logback.toArray(new String[] {}));
	}

	@Test
	void getLoggerConfigurationShouldBeAligned() {
		initialize();
		LoggerConfiguration log4J2Logger = this.log4J2LoggingSystem.getLoggerConfiguration("org");
		LoggerConfiguration logbackLogger = this.logbackLoggingSystem.getLoggerConfiguration("org");
		assertThat(log4J2Logger).isEqualTo(logbackLogger);
	}

	private void initialize() {
		this.log4J2LoggingSystem.beforeInitialize();
		this.log4J2LoggingSystem.initialize(null, null, null);
		this.log4J2LoggingSystem.setLogLevel(getClass().getName(), LogLevel.DEBUG);
		this.logbackLoggingSystem.beforeInitialize();
		this.logbackLoggingSystem.initialize(this.initializationContext, null, null);
		this.logbackLoggingSystem.setLogLevel(getClass().getName(), LogLevel.DEBUG);
		loggerRegistry.getLoggers().stream().map(AbstractLogger::getName).forEach(LoggerFactory::getLogger);
	}

	static class Nested {

		private static final Log logger = LogFactory.getLog(Nested.class);

	}

}
