/*
 * Copyright 2014 Michael J. Simons.
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * @author Michael J. Simons, 2014-02-19
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile({"default", "prod"})
public class SecurityConfig {

    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    @ConditionalOnBean(SecurityConfig.class)
    protected static class ApplicationWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(final HttpSecurity http) throws Exception {	  
	    http
		.httpBasic()
		    .and()
		.authorizeRequests()	
		    .antMatchers(		
			    "/api/system/env/java.(runtime|vm).*",
			    "/api/system/metrics/**"
		    ).permitAll()
		    .antMatchers("/api/system/env/**").denyAll()		    
		    .antMatchers("/**").permitAll()		    
		    .and()
		.sessionManagement()
		    .sessionCreationPolicy(STATELESS)
		    .and()
		.csrf()
		    .disable()
		.headers()
		    .frameOptions() // OEmbedController#embedTrack uses an iframe
		    .disable()
		;  
	}
    }
}