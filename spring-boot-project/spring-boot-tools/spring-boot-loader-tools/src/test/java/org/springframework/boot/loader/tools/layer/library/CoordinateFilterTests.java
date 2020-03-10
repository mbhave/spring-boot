/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.loader.tools.layer.library;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.loader.tools.Library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link CoordinateFilter}.
 *
 * @author Madhura Bhave
 */
class CoordinateFilterTests {

	private CoordinateFilter filter;

	@Test
	void isLibraryIncludedWhenGroupIdIsNullAndToMatchHasWildcard() {
		List<String> includes = Collections.singletonList("*:*");
		this.filter = new CoordinateFilter(includes, Collections.emptyList());
		Library library = mock(Library.class);
		given(library.getGroupId()).willReturn(null);
		assertThat(this.filter.isLibraryIncluded(library)).isTrue();
	}

	@Test
	void isLibraryIncludedWhenArtifactIdIsNullAndToMatchHasWildcard() {
		List<String> includes = Collections.singletonList("org.acme:*");
		this.filter = new CoordinateFilter(includes, Collections.emptyList());
		Library library = mock(Library.class);
		given(library.getGroupId()).willReturn("org.acme");
		given(library.getArtifactId()).willReturn(null);
		assertThat(this.filter.isLibraryIncluded(library)).isTrue();
	}

	@Test
	void isLibraryIncludedWhenGroupIdDoesNotMatch() {
		List<String> includes = Collections.singletonList("org.acme:*");
		this.filter = new CoordinateFilter(includes, Collections.emptyList());
		Library library = mock(Library.class);
		given(library.getGroupId()).willReturn("other.foo");
		given(library.getArtifactId()).willReturn(null);
		assertThat(this.filter.isLibraryIncluded(library)).isFalse();
	}

	@Test
	void isLibraryIncludedWhenArtifactIdDoesNotMatch() {
		List<String> includes = Collections.singletonList("org.acme:test");
		this.filter = new CoordinateFilter(includes, Collections.emptyList());
		Library library = mock(Library.class);
		given(library.getGroupId()).willReturn("org.acme");
		given(library.getArtifactId()).willReturn("other");
		assertThat(this.filter.isLibraryIncluded(library)).isFalse();
	}

}
