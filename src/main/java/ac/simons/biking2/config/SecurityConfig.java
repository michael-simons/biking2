/*
 * Copyright 2014-2022 michael-simons.eu.
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
package ac.simons.biking2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.to;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * @author Michael J. Simons
 * @since 2014-02-19
 */
@Configuration(proxyBeanMethods = false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SuppressWarnings({"squid:S1118"}) // This is not a utility class. It cannot have a private constructor.
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(@Value("${biking2.require-ssl:false}") final boolean requireSSL, final HttpSecurity http) throws Exception {
        // @formatter:off
        HttpSecurity builder = http
            .httpBasic()
                .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/system/env/java.runtime.version")
                    .permitAll()
                .requestMatchers(to(HealthEndpoint.class, InfoEndpoint.class, MetricsEndpoint.class))
                    .permitAll()
                .requestMatchers(to(EnvironmentEndpoint.class))
                    .authenticated()
                .requestMatchers("/**").permitAll()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(STATELESS)
                .and()
            .csrf()
                .disable()
            .headers()
                .frameOptions() // OEmbedController#embedTrack uses an iframe
                .disable().and();
        // @formatter:on
        if (requireSSL) {
            builder = builder.requiresChannel().anyRequest().requiresSecure().and();
        }
        return builder.build();
    }
}
