/*
 * Copyright 2017-2019 michael-simons.eu.
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

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Michael J. Simons
 *
 * @since 2017-02-03
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(locations = "/application-test.properties", properties = "spring.main.banner-mode = LOG")
class EndpointConfigIT {

    @Autowired
    private TestRestTemplate template;

    @Test
    void someEndpointsShouldBeAccessible() {

        final ResponseEntity<Map> vmProperties = template.getForEntity("/api/system/env/java.runtime.version", Map.class);
        assertEquals(HttpStatus.OK, vmProperties.getStatusCode());
        assertTrue(vmProperties.getBody().containsKey("property"));

        final ResponseEntity<Map> metrics = template.getForEntity("/api/system/metrics", Map.class);
        assertEquals(HttpStatus.OK, metrics.getStatusCode());
    }
    
    @Test
    void notAllEnvsShouldBeShown() {

        ResponseEntity<Map> env = template.getForEntity("/api/system/env", Map.class);
        assertEquals(HttpStatus.UNAUTHORIZED, env.getStatusCode());

        env = template.getForEntity("/api/system/env/server.*", Map.class);
        assertEquals(HttpStatus.UNAUTHORIZED, env.getStatusCode());
    }
}
