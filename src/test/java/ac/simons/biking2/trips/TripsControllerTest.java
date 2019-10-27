/*
 * Copyright 2015-2019 Michael J. Simons.
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
package ac.simons.biking2.trips;

import ac.simons.biking2.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import org.joor.Reflect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2015-06-09
 */
@RunWith(SpringRunner.class)
@WebMvcTest(
        includeFilters = @Filter(type = ASSIGNABLE_TYPE, value = SecurityConfig.class),
        controllers = TripsController.class
)
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "biking.michael-simons.eu",
        uriPort = 80
)
public class TripsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssortedTripRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testCreateTrip() throws Exception {
        final AssortedTripEntity trip = Reflect
                .on(new AssortedTripEntity(LocalDate.now(), BigDecimal.valueOf(23.42)))
                .set("id", 42)
                .get();

        final NewTripCmd newTripCmd1 = new NewTripCmd();
        newTripCmd1.setCoveredOn(trip.getCoveredOn());
        newTripCmd1.setDistance(23.42);
        final NewTripCmd newTripCmd2 = new NewTripCmd();
        newTripCmd2.setCoveredOn(trip.getCoveredOn());
        newTripCmd2.setDistance(666.0);

        when(repository.save(any(AssortedTripEntity.class))).then(invocation -> {
            final AssortedTripEntity arg = invocation.getArgument(0);
            return arg == null ? arg : Reflect.on(arg).set("id", 42).get();
        });
        when(repository.save(argThat(a -> a.getDistance().equals(BigDecimal.valueOf(666.0)))))
                .thenThrow(new DataIntegrityViolationException(""));

        // Empty content
        mockMvc
                .perform(post("/api/trips").contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(""));

        // Invalid content
        mockMvc
                .perform(
                        post("/api/trips")
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

        final FieldDescriptor coveredOnDescriptor = fieldWithPath("coveredOn").description("The date of the trip");
        final FieldDescriptor distanceDescriptor = fieldWithPath("distance").description("Distance covered on the trip");

        // Valid request
        mockMvc
                .perform(
                        post("/api/trips")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTripCmd1))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                                objectMapper.writeValueAsString(trip))
                )
                .andDo(document("api/trips/post",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(coveredOnDescriptor, distanceDescriptor),
                                responseFields(
                                        fieldWithPath("id").description("The unique Id of the trip"),
                                        coveredOnDescriptor, distanceDescriptor
                                )
                        )
                );

        // Valid request, duplicate content
        mockMvc
                .perform(
                        post("/api/trips")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTripCmd2))
                )
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string(""));

        verify(repository, times(2)).save(any(AssortedTripEntity.class));
    }
}
