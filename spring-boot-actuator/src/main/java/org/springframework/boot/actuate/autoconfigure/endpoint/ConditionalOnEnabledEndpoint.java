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

package org.springframework.boot.actuate.autoconfigure.endpoint;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.endpoint.Endpoint;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional} that checks whether an endpoint is enabled or not. Matches if the
 * value of the {@code endpoints.<id>.enabled} property is {@code true}. Otherwise,
 * matches if the value of the {@code endpoints.all.enabled} property is {@code true} or
 * if it is not configured.
 * <p>
 * This condition must be placed on a {@code @Bean} method producing an endpoint as its
 * id and other attributes are inferred from the {@link Endpoint} annotation set on the
 * return type of the factory method. Consider the following valid example:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;ConditionalOnEnabledEndpoint
 *     &#064;Bean
 *     public MyEndpoint myEndpoint() {
 *         ...
 *     }
 *
 *     &#064;Endpoint(id = "my")
 *     static class MyEndpoint { ... }
 *
 * }</pre>
 * <p>
 *
 * In the sample above the id of the endpoint will be {@code my}.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Conditional(OnEnabledEndpointCondition.class)
public @interface ConditionalOnEnabledEndpoint {

}
