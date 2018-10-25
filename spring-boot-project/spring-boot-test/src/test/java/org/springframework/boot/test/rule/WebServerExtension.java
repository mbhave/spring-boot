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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Madhura Bhave
 */
public class WebServerExtension implements BeforeAllCallback {

	private String url;

	@Override
	public void beforeAll(ExtensionContext context) {
		this.url = "http://example.org:8181";
	}

	public String getServerUrl() {
		return this.url;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		public Builder enableSecurity(boolean b) {
			return this;
		}

		public WebServerExtension build() {
			return new WebServerExtension();
		}

	}

}
