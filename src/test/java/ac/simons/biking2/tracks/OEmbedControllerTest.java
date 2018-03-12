/*
 * Copyright 2014-2017 michael-simons.eu.
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

import ac.simons.biking2.support.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Here we need the full Spring Boot context to have the same configuration for
 * JAXB, format preferences and Object Mapper available to test the correct
 * serialisation. We use {@link SpringBootTest}.
 *
 * @author Michael J. Simons
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {TestConfig.class, OEmbedControllerTestConfig.class})
@DirtiesContext
public class OEmbedControllerTest {

    private final static String expectedJsonResult = "{\"author_name\":\"Michael J. Simons\",\"author_url\":\"http://michael-simons.eu\",\"cache_age\":86400,\"html\":\"<iframe width='1024' height='576' src='http://biking.michael-simons.eu/tracks/n/embed?width=1024&height=576' class='bikingTrack'></iframe>\",\"provider_name\":\"biking2\",\"provider_url\":\"http://biking.michael-simons.eu\",\"title\":\"RR bis Simmerath\",\"type\":\"rich\",\"version\":\"1.0\"}";
    private final static String expectedXmlResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><oembed><type>rich</type><version>1.0</version><title>RR bis Simmerath</title><author_name>Michael J. Simons</author_name><author_url>http://michael-simons.eu</author_url><provider_name>biking2</provider_name><provider_url>http://biking.michael-simons.eu</provider_url><cache_age>86400</cache_age><html>&lt;iframe width='1024' height='576' src='http://biking.michael-simons.eu/tracks/n/embed?width=1024&amp;height=576' class='bikingTrack'&gt;&lt;/iframe&gt;</html></oembed>";

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private Coordinate home;

    @Test
    public void getEmbeddableTrack_shouldBeValidJson() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/oembed?url=http://biking.michael-simons.eu/tracks/n&format=json"))
                .andExpect(content().string(expectedJsonResult));
    }

    @Test
    public void getEmbeddableTrack_shouldBeValidXml() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/oembed?url=http://biking.michael-simons.eu/tracks/n&format=xml"))
                .andExpect(content().string(expectedXmlResult));
    }

    @Test
    public void getEmbeddableTrack_shouldHandleUnacceptableRequests() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/oembed?url=http://biking.michael-simons.eu/tracks/n&format=poef"))
                .andExpect(status().isNotAcceptable());
        mockMvc
                .perform(get("http://biking.michael-simons.eu/oembed?url=http://biking.michael-simons.eu/tracks/_&format=json"))
                .andExpect(status().isNotAcceptable());
        mockMvc
                .perform(get("http://biking.michael-simons.eu/oembed?url=http://biking.michael-simons.eu/123&format=json"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void getEmbeddableTrack_shouldHandleInvalidTracks() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/oembed?url=http://biking.michael-simons.eu/tracks/1&format=json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void embedTrack_shouldHandleUnacceptableRequests() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/tracks/_/embed"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void embedTrack_shouldHandleInvalidTracks() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/tracks/1/embed"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void embedTrack() throws Exception {
        final MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        mockMvc
                .perform(get("http://biking.michael-simons.eu/tracks/n/embed?width=23&height=42"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("track", is(equalTo(this.trackRepository.findById(23).get()))))
                .andExpect(model().attribute("home", is(equalTo(home))))
                .andExpect(model().attribute("width", is(equalTo(23))))
                .andExpect(model().attribute("height", is(equalTo(42))))
                .andExpect(view().name("oEmbed/embeddedTrack"));

        mockMvc
                .perform(get("http://biking.michael-simons.eu/tracks/n/embed"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("width", is(equalTo(1024))))
                .andExpect(model().attribute("height", is(equalTo(576))));
    }
}
