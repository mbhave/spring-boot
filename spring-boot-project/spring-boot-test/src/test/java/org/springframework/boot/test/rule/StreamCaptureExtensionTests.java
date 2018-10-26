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
package org.springframework.boot.test.rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StreamCaptureExtension}.
 *
 * @author Madhura Bhave
 */
public class StreamCaptureExtensionTests {

	@RegisterExtension
	StreamCaptureExtension output = new StreamCaptureExtension();

	@Test
	void captureShouldReturnAllCapturedOutput() {
		System.out.println("Hello World");
		System.err.println("Error!!!");
		assertThat(this.output).contains("Hello World");
		assertThat(this.output).contains("Error!!!");
	}

	@Test
	void reset() {
		System.out.println("Hello");
		System.err.println("Error1");
		this.output.reset();
		System.out.println("World");
		System.err.println("Error2");
		assertThat(this.output).doesNotContain("Hello")
				.doesNotContain("Error1").contains("World").contains("Error2");
	}

	@Test
	void errorShouldReturnOnlyErrorOutput() {
		System.out.println("Hello World");
		System.err.println("Error!!!");
		assertThat(this.output.justStdError()).contains("Error!!!");
		assertThat(this.output.justStdError()).doesNotContain("Hello World");
	}

	@Test
	void outShouldReturnOnlyOut() {
		System.out.println("Hello World");
		System.err.println("Error!!!");
		assertThat(this.output.justStdOut()).contains("Hello World");
		assertThat(this.output.justStdOut()).doesNotContain("Error!!!");
	}

}