/*
 * Copyright 2014-2019 michael-simons.eu.
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
package ac.simons.biking2.bikes;

import ac.simons.biking2.bikes.BikeEntity.Link;
import ac.simons.biking2.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.joor.Reflect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static java.time.LocalDate.now;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;

/**
 * @author Michael J. Simons
 * @since 2014-02-20
 */
@WebMvcTest(
        // I made my life simple by including the service in the test slice after I introduced it
        // Thus, the mocked repository bean below is passed to the service and the service
        // acts accordingly. In the end, the controller and it's tests was somewhat broken anyway.
        // The controller is now in a much better shape, when time permits, I shall tests both service
        // and controller on its own.
        includeFilters = @Filter(type = ASSIGNABLE_TYPE, value = {SecurityConfig.class, BikeService.class}),
        controllers = BikesController.class
)
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "biking.michael-simons.eu",
        uriPort = 80
)
class BikesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BikeRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetBikes() throws Exception {
        final List<BikeEntity> allbikes = Arrays.asList(Reflect.onClass(BikeEntity.class).create()
                .set("id", 4711)
                .set("name", "Bike 1")
                .set("color", "FF0000")
                .set("boughtOn", LocalDate.of(2015, Month.JANUARY, 1))
                .set("decommissionedOn", LocalDate.of(2015, Month.DECEMBER, 31))
                .set("story", new Link("http://test.com/test", "Test Story"))
                .call("addMilage", LocalDate.of(2015, Month.JANUARY, 1), 0.0)
                .call("getBike")
                .call("addMilage", LocalDate.of(2015, Month.FEBRUARY, 1), 100.0)
                .call("getBike")
                .call("addMilage", LocalDate.of(2015, Month.MARCH, 1), 200.0)
                .call("getBike")
                .get(),
                Reflect.onClass(BikeEntity.class).create()
                .set("id", 23)
                .set("name", "Bike 2")
                .set("color", "CCCCCC")
                .set("boughtOn", LocalDate.of(2014, Month.JANUARY, 1))
                .call("addMilage", LocalDate.of(2014, Month.JANUARY, 1), 0.0)
                .call("getBike")
                .get()
        );
        final List<BikeEntity> activeBikes = Arrays.asList(allbikes.get(0));

        when(repository.findAll(any(Sort.class))).thenReturn(allbikes);
        when(repository.findByDecommissionedOnIsNull(any(Sort.class))).thenReturn(activeBikes);

        mockMvc
                .perform(
                        get("http://biking.michael-simons.eu/api/bikes")
                        .param("all", "true")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(allbikes)))
                .andDo(document("api/bikes/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("all").description("Flag, if all bikes, including decommissioned bikes, should be returned.")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("An array of bikes"),
                                fieldWithPath("[].id").description("The unique Id of the bike"),
                                fieldWithPath("[].name").description("The name of the bike"),
                                fieldWithPath("[].color").description("The color of the bike (used in charts etc.)"),
                                fieldWithPath("[].boughtOn").description("The date the bike was bought"),
                                fieldWithPath("[].decommissionedOn").optional().description("The date the bike was decommissioned"),
                                fieldWithPath("[].story").optional().description("The story of the bike"),
                                fieldWithPath("[].story.url").description("Link to the story"),
                                fieldWithPath("[].story.label").description("A title for the story"),
                                fieldWithPath("[].lastMilage").description("The last recorded milage of the bike")
                        )
                )
                );

        mockMvc
                .perform(
                        get("http://biking.michael-simons.eu/api/bikes")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(activeBikes)));

        Mockito.verify(repository).findAll(Mockito.any(Sort.class));
        Mockito.verify(repository).findByDecommissionedOnIsNull(Mockito.any(Sort.class));
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    @WithMockUser
    void testCreateMilage() throws Exception {
        LocalDate now = now();

        final BikeEntity bike = new BikeEntity("testBike", now);
        when(repository.findById(2)).thenReturn(Optional.of(bike));
        final BikeEntity decommissionedBike = new BikeEntity("decommissioned", now.minusMonths(2).withDayOfMonth(1));
        decommissionedBike.decommission(now.minusMonths(1));
        when(repository.findById(3)).thenReturn(Optional.of(decommissionedBike));

        final NewMilageCmd newMilageCmd = new NewMilageCmd();
        newMilageCmd.setAmount(23.0);
        newMilageCmd.setRecordedOn(LocalDate.now());

        // Empty content
        mockMvc
                .perform(post("/api/bikes/1/milages").contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(""));

        // Invalid content
        mockMvc
                .perform(
                        post("/api/bikes/1/milages")
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

        // Invalid bike
        mockMvc
                .perform(
                        post("/api/bikes/1/milages")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMilageCmd))
                )
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(""));

        // Valid request
        mockMvc
                .perform(
                        RestDocumentationRequestBuilders.post("/api/bikes/{id}/milages", 2)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMilageCmd))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(23.0)))
                .andExpect(jsonPath("$.bike.name", is("testBike")))
                .andExpect(jsonPath("$.bike.lastMilage", is(23)))
                .andDo(
                        document(
                                "api/bikes/milages/post",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("The id of the bike to which a milage should be added")
                                ),
                                requestFields(
                                        fieldWithPath("recordedOn").description("The date the new milage was recorded"),
                                        fieldWithPath("amount").description("The total milage of the bike on the given date")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("The unique id of the newly recorded milage"),
                                        fieldWithPath("recordedOn").description("The date the new milage was recorded"),
                                        fieldWithPath("amount").description("The total milage of the bike on the given date"),
                                        fieldWithPath("createdAt").description("The date the new milage record was created"),
                                        subsectionWithPath("bike").description("The bike object the new milage belongs to")
                                )
                        )
                );

        // Decommisioned bike
        mockMvc
                .perform(
                        post("/api/bikes/3/milages")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMilageCmd))
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Bike has already been decommissioned."));

        verify(repository, times(1)).findById(1);
        verify(repository, times(1)).findById(2);
        verify(repository, times(1)).findById(3);
        verify(repository, times(1)).save(any(BikeEntity.class));
    }

    @Test
    @WithMockUser
    void testCreateBike1() throws Exception {
        LocalDate now = now();

        when(repository.save(any(BikeEntity.class))).then(returnsFirstArg());

        final BikeCmd newBikeCmd = new BikeCmd();
        newBikeCmd.setBoughtOn(ZonedDateTime.now());
        newBikeCmd.setColor("cccccc");
        newBikeCmd.setName("test");

        final BikeEntity bike = new BikeEntity("test", now);
        bike.setColor("cccccc");

        // Empty content
        mockMvc
                .perform(post("http://biking.michael-simons.eu/api/bikes").contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(""));

        // Invalid content
        mockMvc
                .perform(
                        post("http://biking.michael-simons.eu/api/bikes")
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

        // Valid request
        mockMvc
                .perform(
                        post("http://biking.michael-simons.eu/api/bikes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBikeCmd))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(bike))
                )
                .andDo(document("api/bikes/post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("The name of the new bike"),
                                fieldWithPath("boughtOn").description("The date the new bike was bought"),
                                fieldWithPath("color").description("The color of the new bike"),
                                fieldWithPath("miscellaneous").description("Whether it's a miscellaneous bike or not"),
                                fieldWithPath("decommissionedOn").ignored()
                        )
                )
                );

        verify(repository, times(1)).save(any(BikeEntity.class));
    }

    @Test
    @WithMockUser
    void testCreateBike2() throws Exception {
        when(repository.save(any(BikeEntity.class))).thenThrow(new DataIntegrityViolationException(""));

        final BikeCmd newBikeCmd = new BikeCmd();
        newBikeCmd.setBoughtOn(ZonedDateTime.now());
        newBikeCmd.setColor("cccccc");
        newBikeCmd.setName("test");

        mockMvc
                .perform(
                        post("http://biking.michael-simons.eu/api/bikes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBikeCmd))
                )
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(""));

        verify(repository, times(1)).save(any(BikeEntity.class));
    }

    @Test
    @WithMockUser
    void testUpdateBike() throws Exception {
        LocalDate now = now();

        final BikeEntity decommissionedBike = new BikeEntity("decommissioned", now.minusMonths(2).withDayOfMonth(1));
        decommissionedBike.decommission(now.minusMonths(1));
        when(repository.findById(3)).thenReturn(Optional.of(decommissionedBike));

        BikeEntity bike = new BikeEntity("test", now.minusMonths(1));
        bike.setColor("000000");
        LocalDate boughtOn = bike.getBoughtOn();
        when(repository.findById(2)).thenReturn(Optional.of(bike));

        final BikeCmd updatedBikeCmd = new BikeCmd();
        updatedBikeCmd.setBoughtOn(ZonedDateTime.now());
        updatedBikeCmd.setDecommissionedOn(ZonedDateTime.now());
        updatedBikeCmd.setColor("FFFCCC");
        updatedBikeCmd.setName("neuer name");

        // Empty content
        mockMvc
                .perform(put("http://biking.michael-simons.eu/api/bikes/2").contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(""));

        // Invalid content
        mockMvc
                .perform(
                        put("http://biking.michael-simons.eu/api/bikes/2")
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

        // Valid request, invalid bike
        mockMvc
                .perform(
                        put("http://biking.michael-simons.eu/api/bikes/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBikeCmd))
                )
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(""));

        mockMvc
                .perform(
                        put("http://biking.michael-simons.eu/api/bikes/2")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBikeCmd))
                )
                .andExpect(status().isOk());

        // Decommisioned bike
        mockMvc
                .perform(
                        put("http://biking.michael-simons.eu/api/bikes/3")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBikeCmd))
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Bike has already been decommissioned."));

        verify(repository, times(1)).findById(1);
        verify(repository, times(1)).findById(2);
        verify(repository, times(1)).findById(3);

        Assertions.assertEquals("test", bike.getName());
        Assertions.assertEquals(boughtOn, bike.getBoughtOn());
        Assertions.assertEquals("FFFCCC", bike.getColor());
        Assertions.assertEquals(now, bike.getDecommissionedOn());

        verifyNoMoreInteractions(repository);
    }

    @Test
    @WithMockUser
    void testUpdateBikeStory() throws Exception {
        LocalDate now = now();

        final BikeEntity decommissionedBike = new BikeEntity("decommissioned", now.minusMonths(2).withDayOfMonth(1));
        decommissionedBike.decommission(now.minusMonths(1));
        when(repository.findById(3)).thenReturn(Optional.of(decommissionedBike));

        BikeEntity bike = new BikeEntity("test", now.minusMonths(1));
        bike.setColor("000000");
        LocalDate boughtOn = bike.getBoughtOn();
        when(repository.findById(2)).thenReturn(Optional.of(bike));

        final StoryCmd validNewStoryCmd = new StoryCmd();
        validNewStoryCmd.setLabel("Nie wieder Stadtschlampe");
        validNewStoryCmd.setUrl("http://planet-punk.de/2015/08/11/nie-wieder-stadtschlampe/");
        final StoryCmd invalidNewStoryCmd = new StoryCmd();
        invalidNewStoryCmd.setUrl("asdasd");

        // Invalid content
        mockMvc
                .perform(
                        put("/api/bikes/2/story")
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

        // Invalid content
        mockMvc
                .perform(
                        put("/api/bikes/2/story")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNewStoryCmd))
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

        // Valid request, invalid bike
        mockMvc
                .perform(
                        put("/api/bikes/1/story")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNewStoryCmd))
                )
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(""));

        mockMvc
                .perform(
                        RestDocumentationRequestBuilders.put("/api/bikes/{id}/story", 2)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNewStoryCmd))
                )
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "api/bikes/story/put",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("id").description("The id of the bike whose story should be updated")
                                ),
                                requestFields(
                                        fieldWithPath("url").description("Link to the story"),
                                        fieldWithPath("label").description("A title for the story")
                                )
                        )
                );

        Assertions.assertEquals("test", bike.getName());
        Assertions.assertEquals(boughtOn, bike.getBoughtOn());
        Assertions.assertEquals("000000", bike.getColor());
        Assertions.assertNotNull(bike.getStory());
        Assertions.assertEquals(validNewStoryCmd.getLabel(), bike.getStory().getLabel());
        Assertions.assertEquals(validNewStoryCmd.getUrl(), bike.getStory().getUrl());

        // Empty content
        mockMvc
                .perform(put("/api/bikes/2/story").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "api/bikes/story/put-empty",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );

        Assertions.assertNull(bike.getStory());

        // Decommisioned bike
        mockMvc
                .perform(
                        put("/api/bikes/3/story")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNewStoryCmd))
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Bike has already been decommissioned."));

        verify(repository, times(1)).findById(1);
        verify(repository, times(2)).findById(2);
        verify(repository, times(1)).findById(3);

        verifyNoMoreInteractions(repository);
    }
}
