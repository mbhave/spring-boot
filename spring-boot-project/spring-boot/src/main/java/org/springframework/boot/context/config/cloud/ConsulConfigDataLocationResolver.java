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

package org.springframework.boot.context.config.cloud;

import java.util.ArrayList;
import java.util.List;

import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.v1.ConsulClient;
import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;

import static org.springframework.boot.context.config.cloud.ConsulConfigProperties.Format.FILES;

/**
 * @author Madhura Bhave
 */
public class ConsulConfigDataLocationResolver implements ConfigDataLocationResolver<ConsulConfigDataLocation> {

	private static final String PREFIX = "consul:";

	private final Log logger;

	private final ConsulClient consulClient;

	private final ConsulConfigProperties properties;

	private final String applicationName;

	public ConsulConfigDataLocationResolver(Log logger, Binder binder) {
		this.logger = logger;
		this.consulClient = createConsulClient(binder);
		this.properties = getProperties(binder);
		applicationName = getApplicationName(binder);
	}

	private ConsulClient createConsulClient(Binder binder) {
		ConsulClientProperties consulProperties = binder
				.bind("spring.config.consul", Bindable.of(ConsulClientProperties.class))
				.orElse(new ConsulClientProperties());
		final int agentPort = consulProperties.getPort();
		final String agentHost = !StringUtils.isEmpty(consulProperties.getScheme())
				? consulProperties.getScheme() + "://" + consulProperties.getHost() : consulProperties.getHost();

		if (consulProperties.getTls() != null) {
			ConsulClientProperties.TLSConfig tls = consulProperties.getTls();
			TLSConfig tlsConfig = new TLSConfig(tls.getKeyStoreInstanceType(), tls.getCertificatePath(),
					tls.getCertificatePassword(), tls.getKeyStorePath(), tls.getKeyStorePassword());
			return new ConsulClient(agentHost, agentPort, tlsConfig);
		}
		return new ConsulClient(agentHost, agentPort);
	}

	private ConsulConfigProperties getProperties(Binder binder) {
		return binder.bind("spring.config.consul.config", Bindable.of(ConsulConfigProperties.class))
				.orElse(new ConsulConfigProperties());
	}

	private String getApplicationName(Binder binder) {
		return (this.properties.getName() != null) ? this.properties.getName()
				: binder.bind("spring.application.name", String.class).orElse(null);
	}

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, String location) {
		return location.startsWith(PREFIX);
	}

	@Override
	public List<ConsulConfigDataLocation> resolve(ConfigDataLocationResolverContext context, String location) {
		List<ConsulConfigDataLocation> dataLocations = new ArrayList<>();
		getContexts(null).forEach((c) -> dataLocations.add(new ConsulConfigDataLocation(this.consulClient, c, this.properties)));
		return dataLocations;
	}

	private List<String> getContexts(Profiles profiles) {
		List<String> contexts = new ArrayList<>();
		String prefix = this.properties.getPrefix();
		List<String> suffixes = new ArrayList<>();
		if (this.properties.getFormat() != FILES) {
			suffixes.add("/");
		}
		else {
			suffixes.add(".yml");
			suffixes.add(".yaml");
			suffixes.add(".properties");
		}
		String defaultContext = getContext(prefix, this.properties.getDefaultContext());
		String baseContext = getContext(prefix, this.applicationName);
		if (profiles == null) {
			for (String suffix : suffixes) {
				contexts.add(defaultContext + suffix);
			}
			for (String suffix : suffixes) {
				contexts.add(baseContext + suffix);
			}
		}
		else {
			for (String suffix : suffixes) {
				contexts.add(defaultContext + suffix);
			}
			for (String suffix : suffixes) {
				addProfiles(contexts, defaultContext, profiles.getAccepted(), suffix);
			}
			for (String suffix : suffixes) {
				contexts.add(baseContext + suffix);
			}
			for (String suffix : suffixes) {
				addProfiles(contexts, baseContext, profiles.getAccepted(), suffix);
			}
		}
		return contexts;
	}

	private String getContext(String prefix, String context) {
		if (StringUtils.isEmpty(prefix)) {
			return context;
		}
		else {
			return prefix + "/" + context;
		}
	}

	private void addProfiles(List<String> contexts, String baseContext, List<String> profiles, String suffix) {
		for (String profile : profiles) {
			contexts.add(baseContext + this.properties.getProfileSeparator() + profile + suffix);
		}
	}

	@Override
	public List<ConsulConfigDataLocation> resolveProfileSpecific(ConfigDataLocationResolverContext context,
			String location, Profiles profiles) {
		List<ConsulConfigDataLocation> dataLocations = new ArrayList<>();
		getContexts(profiles).forEach((c) -> dataLocations.add(new ConsulConfigDataLocation(this.consulClient, c, this.properties)));
		return dataLocations;
	}

}
