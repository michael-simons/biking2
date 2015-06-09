/*
 * Copyright 2015 michael-simons.eu.
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

package ac.simons.biking2.api;

import ac.simons.biking2.persistence.entities.AssortedTrip;
import ac.simons.biking2.persistence.repositories.AssortedTripRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Calendar;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author Michael J. Simons, 2015-06-09
 */
public class TripsControllerTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCreateBike1() throws Exception {
	final AssortedTripRepository repository = mock(AssortedTripRepository.class);

	final TripsController controller = new TripsController(repository);
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

	final AssortedTrip trip = new AssortedTrip(Calendar.getInstance(), BigDecimal.valueOf(23.42));
	
	final NewTripCmd newTripCmd1 = new NewTripCmd();
	newTripCmd1.setCoveredOn(trip.getCoveredOn().getTime());
	newTripCmd1.setDistance(23.42);	
	final NewTripCmd newTripCmd2 = new NewTripCmd();
	newTripCmd2.setCoveredOn(trip.getCoveredOn().getTime());
	newTripCmd2.setDistance(666.0);	
		
 	
	when(repository.save(any(AssortedTrip.class))).then(returnsFirstArg());	
	// Using hamcrest to check for properties of the passed object
	when(repository.save(argThat(Matchers.<AssortedTrip>hasProperty("distance", is(BigDecimal.valueOf(666.0)))))).thenThrow(new DataIntegrityViolationException(""));
	
	// Empty content
	mockMvc
		.perform(post("http://biking.michael-simons.eu/api/trips").contentType(APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string(""));

	// Invalid content
	mockMvc
		.perform(
			post("http://biking.michael-simons.eu/api/trips")
			.contentType(APPLICATION_JSON)
			.content("{}")
		)
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

	// Valid request
	mockMvc
		.perform(
			post("http://biking.michael-simons.eu/api/trips")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(newTripCmd1))
		)
		.andExpect(status().isOk())
		.andExpect(content().string(
				objectMapper.writeValueAsString(trip))
		);
	
	// Valid request, duplicate content
	mockMvc
		.perform(
			post("http://biking.michael-simons.eu/api/trips")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(newTripCmd2))
		)
		.andExpect(status().isConflict())
		.andExpect(MockMvcResultMatchers.content().string(""));

	verify(repository, times(2)).save(any(AssortedTrip.class));
    }
}