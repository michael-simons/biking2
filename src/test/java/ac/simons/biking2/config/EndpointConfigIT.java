/*
 * Copyright 2017 michael-simons.eu.
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

import ac.simons.biking2.Application;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Michael J. Simons, 2017-02-03
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Application.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(locations = "/application-test.properties")
public class EndpointConfigIT {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void someEndpointsShouldBeAccessible() throws Exception {
        final ResponseEntity<Map> vmProperties = template.getForEntity("/api/system/env/java.(runtime|vm).*", Map.class);
        assertThat(vmProperties.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(vmProperties.getBody().containsKey("java.runtime.name"), is(true));
        
        final ResponseEntity<Map> metrics = template.getForEntity("/api/system/metrics", Map.class);
        assertThat(metrics.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }
    
    @Test
    public void notAllEnvsShouldBeShown() throws Exception {
        ResponseEntity<Map> env = template.getForEntity("/api/system/env", Map.class);
        assertThat(env.getStatusCode(), is(equalTo(HttpStatus.UNAUTHORIZED)));
        env = template.getForEntity("/api/system/env/server.*", Map.class);
        assertThat(env.getStatusCode(), is(equalTo(HttpStatus.UNAUTHORIZED)));
    }
}
