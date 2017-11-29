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
package ac.simons.biking2.config;

import ac.simons.biking2.support.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

/**
 * @author Michael J. Simons, 2014-03-24
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
public class RoutingTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Test
    public void testStaticRoutes() {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();

        Stream.of("/", "/bikes", "/milages", "/gallery", "/tracks", "/tracks/23", "/location", "/about").forEach(path -> {
            try {
                mockMvc
                        .perform(get(path))
                        .andExpect(forwardedUrl("/index.html"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
