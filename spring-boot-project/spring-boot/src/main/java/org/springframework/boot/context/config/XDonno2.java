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

package org.springframework.boot.context.config;

import java.util.List;

/**
 * @author pwebb
 */
public class XDonno2 {

	void testName() {

		// The initial snapshot is
		// all the property sources

		// Round one
		// get the config locations
		// load each one
		// setup the context with the CP and OS
		// create a new SNAPSHOT with new shit

		// Round two
		// run the import loop until we done

		// Round three
		// update the context with the Profiles

		// Round four
		// update the import loop until we done

	}

	static class Snapshot {

		private List<Thing> things;

	}

	static abstract class Thing {

	}

	static class PropertySourceThing {

	}

	static class InitialImportsThing {

	}

	static class ConfigDataThing {

	}

}
