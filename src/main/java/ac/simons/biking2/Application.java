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
package ac.simons.biking2;

import ac.simons.biking2.misc.Coordinate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Michael J. Simons, 2014-02-08
 */
@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan
public class Application extends SpringBootServletInitializer {

    @Bean
    public Coordinate home(
	    final @Value("${biking2.home.longitude}") String longitude,
	    final @Value("${biking2.home.latitude}") String latitude
    ) {
	return new Coordinate(longitude, latitude);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	return application.sources(Application.class);
    }

    public static void main(String... args) {
	System.setProperty("spring.profiles.default", System.getProperty("spring.profiles.default", "dev"));
	final ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
    }
}
