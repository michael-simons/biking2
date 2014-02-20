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

package ac.simons.biking2.api;

import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Date;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static java.time.LocalDate.now;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2014-02-20
 */
public class BikesControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testCreateMilage() throws Exception {
	LocalDate now = now();
	
	final BikeRepository repository = mock(BikeRepository.class);
				
	final Bike bike = new Bike("testBike", now);
	stub(repository.findOne(2)).toReturn(bike);
		
	final NewMilageCmd newMilageCmd = new NewMilageCmd();
	newMilageCmd.setAmount(23.0);
	newMilageCmd.setRecordedOn(new Date());
		
	final BikesController controller = new BikesController(repository);	
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	
	// Empty content
	mockMvc
		.perform(post("http://biking.michael-simons.eu/api/bikes/1/milages").contentType(APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string(""))
		;
	
	// Invalid content
	mockMvc
		.perform(
			post("http://biking.michael-simons.eu/api/bikes/1/milages")
			    .contentType(APPLICATION_JSON)
			    .content("{}")
		)
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string("Invalid arguments."))
		;		
	
	// Invalid bike
	mockMvc
		.perform(
			post("http://biking.michael-simons.eu/api/bikes/1/milages")
			    .contentType(APPLICATION_JSON)
			    .content(objectMapper.writeValueAsString(newMilageCmd))
		)
		.andExpect(status().isNotFound())
		.andExpect(MockMvcResultMatchers.content().string(""))
		;
	
	// Valid request
	mockMvc
		.perform(
			post("http://biking.michael-simons.eu/api/bikes/2/milages")
			    .contentType(APPLICATION_JSON)
			    .content(objectMapper.writeValueAsString(newMilageCmd))
		)
		.andExpect(status().isOk())
		.andExpect(content().string(
			objectMapper.writeValueAsString(new Bike("testBike", now).addMilage(now, 23.0)))
		)
		;
	
	verify(repository, times(1)).findOne(1);
	verify(repository, times(1)).findOne(2);
	verify(repository, times(1)).save(any(Bike.class));
    }    
}
