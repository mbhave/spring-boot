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

package org.springframework.boot.actuate.autoconfigure;

/**
 * Tests for {@link ManagementWebSecurityAutoConfiguration}.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 */
public class ManagementWebSecurityAutoConfigurationTests {

	// TODO Security for the new endpoint infrastructure

	// private AnnotationConfigWebApplicationContext context;
	//
	// @After
	// public void close() {
	// if (this.context != null) {
	// this.context.close();
	// }
	// }
	//
	// @Test
	// public void testWebConfiguration() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(SecurityAutoConfiguration.class,
	// WebMvcAutoConfiguration.class,
	// ManagementWebSecurityAutoConfiguration.class,
	// JacksonAutoConfiguration.class,
	// HttpMessageConvertersAutoConfiguration.class,
	// EndpointAutoConfiguration.class, EndpointServletWebAutoConfiguration.class,
	// PropertyPlaceholderAutoConfiguration.class, AuditAutoConfiguration.class);
	// TestPropertyValues.of("security.basic.enabled:false").applyTo(this.context);
	// this.context.refresh();
	// assertThat(this.context.getBean(AuthenticationManagerBuilder.class)).isNotNull();
	// FilterChainProxy filterChainProxy = this.context.getBean(FilterChainProxy.class);
	// // 1 for static resources, one for management endpoints and one for the rest
	// assertThat(filterChainProxy.getFilterChains()).hasSize(3);
	// assertThat(filterChainProxy.getFilters("/application/beans")).isNotEmpty();
	// assertThat(filterChainProxy.getFilters("/application/beans/")).isNotEmpty();
	// assertThat(filterChainProxy.getFilters("/application/beans.foo")).isNotEmpty();
	// assertThat(filterChainProxy.getFilters("/application/beans/foo/bar"))
	// .isNotEmpty();
	// }
	//
	// @Test
	// public void testPathNormalization() throws Exception {
	// String path = "admin/./error";
	// assertThat(StringUtils.cleanPath(path)).isEqualTo("admin/error");
	// }
	//
	// @Test
	// public void testWebConfigurationWithExtraRole() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(WebConfiguration.class);
	// this.context.refresh();
	// UserDetails user = getUser();
	// ArrayList<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
	// assertThat(authorities).containsAll(AuthorityUtils
	// .commaSeparatedStringToAuthorityList("ROLE_USER,ROLE_ACTUATOR"));
	// }
	//
	// private UserDetails getUser() {
	// ProviderManager parent = (ProviderManager) this.context
	// .getBean(AuthenticationManager.class);
	// DaoAuthenticationProvider provider = (DaoAuthenticationProvider) parent
	// .getProviders().get(0);
	// UserDetailsService service = (UserDetailsService) ReflectionTestUtils
	// .getField(provider, "userDetailsService");
	// UserDetails user = service.loadUserByUsername("user");
	// return user;
	// }
	//
	// @Test
	// public void testDisableIgnoredStaticApplicationPaths() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(SecurityAutoConfiguration.class,
	// ManagementWebSecurityAutoConfiguration.class,
	// EndpointAutoConfiguration.class,
	// PropertyPlaceholderAutoConfiguration.class);
	// TestPropertyValues.of("security.ignored:none").applyTo(this.context);
	// this.context.refresh();
	// // Just the application and management endpoints now
	// assertThat(this.context.getBean(FilterChainProxy.class).getFilterChains())
	// .hasSize(2);
	// }
	//
	// @Test
	// public void testDisableBasicAuthOnApplicationPaths() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(WebConfiguration.class);
	// TestPropertyValues.of("security.basic.enabled:false").applyTo(this.context);
	// this.context.refresh();
	// // Just the management endpoints (one filter) and ignores now plus the backup
	// // filter on app endpoints
	// assertThat(this.context.getBean(FilterChainProxy.class).getFilterChains())
	// .hasSize(3);
	// }
	//
	// @Test
	// public void testOverrideAuthenticationManager() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(TestConfiguration.class, SecurityAutoConfiguration.class,
	// ManagementWebSecurityAutoConfiguration.class,
	// EndpointAutoConfiguration.class,
	// PropertyPlaceholderAutoConfiguration.class);
	// this.context.refresh();
	// assertThat(this.context.getBean(AuthenticationManager.class)).isEqualTo(
	// this.context.getBean(TestConfiguration.class).authenticationManager);
	// }
	//
	// @Test
	// public void testSecurityPropertiesNotAvailable() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(TestConfiguration.class, SecurityAutoConfiguration.class,
	// ManagementWebSecurityAutoConfiguration.class,
	// EndpointAutoConfiguration.class,
	// PropertyPlaceholderAutoConfiguration.class);
	// this.context.refresh();
	// assertThat(this.context.getBean(AuthenticationManager.class)).isEqualTo(
	// this.context.getBean(TestConfiguration.class).authenticationManager);
	// }
	//
	// // gh-2466
	// @Test
	// public void realmSameForManagement() throws Exception {
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(AuthenticationConfig.class, SecurityAutoConfiguration.class,
	// ManagementWebSecurityAutoConfiguration.class,
	// JacksonAutoConfiguration.class,
	// HttpMessageConvertersAutoConfiguration.class,
	// EndpointAutoConfiguration.class, EndpointServletWebAutoConfiguration.class,
	// WebMvcAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class,
	// AuditAutoConfiguration.class);
	// this.context.refresh();
	//
	// Filter filter = this.context.getBean("springSecurityFilterChain", Filter.class);
	// MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
	// .addFilters(filter).build();
	//
	// // no user (Main)
	// mockMvc.perform(MockMvcRequestBuilders.get("/home"))
	// .andExpect(MockMvcResultMatchers.status().isUnauthorized())
	// .andExpect(springAuthenticateRealmHeader());
	//
	// // invalid user (Main)
	// mockMvc.perform(
	// MockMvcRequestBuilders.get("/home").header("authorization", "Basic xxx"))
	// .andExpect(MockMvcResultMatchers.status().isUnauthorized())
	// .andExpect(springAuthenticateRealmHeader());
	//
	// // no user (Management)
	// mockMvc.perform(MockMvcRequestBuilders.get("/beans"))
	// .andExpect(MockMvcResultMatchers.status().isUnauthorized())
	// .andExpect(springAuthenticateRealmHeader());
	//
	// // invalid user (Management)
	// mockMvc.perform(
	// MockMvcRequestBuilders.get("/beans").header("authorization", "Basic xxx"))
	// .andExpect(MockMvcResultMatchers.status().isUnauthorized())
	// .andExpect(springAuthenticateRealmHeader());
	// }
	//
	// @Test
	// public void testMarkAllEndpointsSensitive() throws Exception {
	// // gh-4368
	// this.context = new AnnotationConfigWebApplicationContext();
	// this.context.setServletContext(new MockServletContext());
	// this.context.register(WebConfiguration.class);
	// TestPropertyValues.of("endpoints.sensitive:true").applyTo(this.context);
	// this.context.refresh();
	//
	// MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.context) //
	// .apply(springSecurity()) //
	// .build();
	//
	// mockMvc //
	// .perform(get("/health")) //
	// .andExpect(status().isUnauthorized());
	// mockMvc //
	// .perform(get("/info")) //
	// .andExpect(status().isUnauthorized());
	// }
	//
	// private ResultMatcher springAuthenticateRealmHeader() {
	// return MockMvcResultMatchers.header().string("www-authenticate",
	// Matchers.containsString("realm=\"Spring\""));
	// }
	//
	// @Configuration
	// @ImportAutoConfiguration({ SecurityAutoConfiguration.class,
	// WebMvcAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
	// JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
	// EndpointAutoConfiguration.class, EndpointServletWebAutoConfiguration.class,
	// PropertyPlaceholderAutoConfiguration.class, AuditAutoConfiguration.class,
	// FallbackWebSecurityAutoConfiguration.class })
	// static class WebConfiguration {
	//
	// }
	//
	// @EnableGlobalAuthentication
	// @Configuration
	// static class AuthenticationConfig {
	//
	// @Autowired
	// public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	// auth.inMemoryAuthentication().withUser("user").password("password")
	// .roles("USER");
	// }
	//
	// }
	//
	// @Configuration
	// protected static class TestConfiguration {
	//
	// private AuthenticationManager authenticationManager;
	//
	// @Bean
	// public AuthenticationManager myAuthenticationManager() {
	// this.authenticationManager = new AuthenticationManager() {
	//
	// @Override
	// public Authentication authenticate(Authentication authentication)
	// throws AuthenticationException {
	// return new TestingAuthenticationToken("foo", "bar");
	// }
	// };
	// return this.authenticationManager;
	// }
	//
	// }

}
