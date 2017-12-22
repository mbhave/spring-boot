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

package org.springframework.boot.test.autoconfigure.data.redis;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.testcontainers.containers.DockerComposeContainer;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DataRedisTest}.
 *
 * @author Jayaram Pradhan
 */
@RunWith(SpringRunner.class)
@DataRedisTest
public class DataRedisTestIntegrationTests {

	@ClassRule
	public static DockerComposeContainer environment =
			new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
					.withExposedService("redis", 6379);

//	@Rule
//	public RedisTestServer redis = new RedisTestServer();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private RedisOperations<Object, Object> operations;

	@Autowired
	private ExampleRepository exampleRepository;

	@Autowired
	private ApplicationContext applicationContext;

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	@Test
	public void testRepository() {
		testConnection(createConnectionFactory("localhost"));
		PersonHash personHash = new PersonHash();
		personHash.setDescription("Look, new @DataRedisTest!");
		assertThat(personHash.getId()).isNull();
		PersonHash savedEntity = this.exampleRepository.save(personHash);
		assertThat(savedEntity.getId()).isNotNull();
		assertThat(this.operations.execute((RedisConnection connection) -> connection
				.exists(("persons:" + savedEntity.getId()).getBytes(CHARSET)))).isTrue();
		this.exampleRepository.deleteAll();
	}

	@Test
	public void didNotInjectExampleService() {
		this.thrown.expect(NoSuchBeanDefinitionException.class);
		this.applicationContext.getBean(ExampleService.class);
	}

	private RedisConnectionFactory createConnectionFactory(String hostname) {
		RedisConnectionFactory cf;
		String hostName = System.getProperty("redis.hostname", hostname);
		cf = new LettuceConnectionFactoryConfiguration().createConnectionFactory(hostName);
		testConnection(cf);
		return cf;
	}

	private void testConnection(RedisConnectionFactory connectionFactory) {
		connectionFactory.getConnection().close();
	}

	private static class LettuceConnectionFactoryConfiguration {

		RedisConnectionFactory createConnectionFactory(String hostName) {
			LettuceClientConfiguration config = LettuceClientConfiguration.builder()
					.shutdownTimeout(Duration.ofMillis(0)).build();
			LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
					new RedisStandaloneConfiguration(hostName), config);
			connectionFactory.afterPropertiesSet();
			return connectionFactory;
		}

	}

}
