/*
 * Copyright 2014-2018 michael-simons.eu.
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
package ac.simons.biking2.tracks;

import java.math.BigDecimal;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons, 2014-12-11
 */
@Configuration
@EnableAutoConfiguration
@EntityScan("ac.simons.biking2")
@EnableJpaRepositories
@EnableTransactionManagement
@Profile("test")
public class OEmbedControllerTestConfig {


    @Bean
    public Coordinate home() {
        return new Coordinate(new BigDecimal("-122.4194200"), new BigDecimal("37.7749300"));
    }

    @Bean
    public OEmbedController oEmbedController(TrackRepository trackRepository, Coordinate home) {
        return new OEmbedController(trackRepository, home);
    }
}
