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
import ac.simons.biking2.persistence.entities.Bike.Link;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static java.time.LocalDate.now;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2014-02-20
 */
public class BikesControllerTest {

    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("target/generated-snippets");
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void shouldGetBikes() throws Exception {			
	final List<Bike> allbikes = Arrays.asList(
		Reflect.on(Bike.class).create()
		    .set("id", 4711)
		    .set("name", "Bike 1")
		    .set("color", "FF0000")
		    .set("boughtOn", GregorianCalendar.from(LocalDate.of(2015, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault())))
		    .set("decommissionedOn", GregorianCalendar.from(LocalDate.of(2015, Month.DECEMBER, 31).atStartOfDay(ZoneId.systemDefault())))
		    .set("story", new Link("http://test.com/test", "Test Story"))
		    .call("addMilage", LocalDate.of(2015, Month.JANUARY, 1), 0.0)
		    .call("getBike")
		    .call("addMilage", LocalDate.of(2015, Month.FEBRUARY, 1), 100.0)
		    .call("getBike")
		    .call("addMilage", LocalDate.of(2015, Month.MARCH, 1), 200.0)
		    .call("getBike")
		    .get(),
		Reflect.on(Bike.class).create()
		    .set("id", 23)
		    .set("name", "Bike 2")
		    .set("color", "CCCCCC")
		    .set("boughtOn", GregorianCalendar.from(LocalDate.of(2014, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault())))
		    .call("addMilage", LocalDate.of(2014, Month.JANUARY, 1), 0.0)
		    .call("getBike")		    
		    .get()
	);
	final List<Bike> activeBikes = Arrays.asList(allbikes.get(0));
	
	final BikeRepository repository = mock(BikeRepository.class);
	stub(repository.findAll(any(Sort.class))).toReturn(allbikes);
	stub(repository.findByDecommissionedOnIsNull(any(Sort.class))).toReturn(activeBikes);
	
	final BikesController controller = new BikesController(repository);
	
	final MockMvc mockMvc = MockMvcBuilders
		.standaloneSetup(controller)
		.apply(documentationConfiguration(this.restDocumentation))
		.build();
		
	mockMvc
		.perform(
			get("http://biking.michael-simons.eu/api/bikes")    
			.param("all", "true")
		)
		.andExpect(status().isOk())
		.andExpect(content().string(objectMapper.writeValueAsString(allbikes)))
		.andDo(document("api/bikes/get",
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
					fieldWithPath("[].milage").description("The total milage of the bike"),
					fieldWithPath("[].lastMilage").description("The last recorded milage of the bike")				
				)
			)
		);
		;
	
	mockMvc
		.perform(
			get("http://biking.michael-simons.eu/api/bikes")    			
		)
		.andExpect(status().isOk())
		.andExpect(content().string(objectMapper.writeValueAsString(activeBikes)))		
		;						
	
	Mockito.verify(repository).findAll(Mockito.any(Sort.class));
	Mockito.verify(repository).findByDecommissionedOnIsNull(Mockito.any(Sort.class));
	Mockito.verifyNoMoreInteractions(repository);		
    }
    
    @Test
    public void testCreateMilage() throws Exception {
	LocalDate now = now();

	final BikeRepository repository = mock(BikeRepository.class);

	final Bike bike = new Bike("testBike", now);
	stub(repository.findOne(2)).toReturn(bike);
	final Bike decommissionedBike = new Bike("decommissioned", now.minusMonths(2).withDayOfMonth(1));	
	decommissionedBike.decommission(now.minusMonths(1));
	stub(repository.findOne(3)).toReturn(decommissionedBike);

	final NewMilageCmd newMilageCmd = new NewMilageCmd();
	newMilageCmd.setAmount(23.0);
	newMilageCmd.setRecordedOn(new Date());

	final BikesController controller = new BikesController(repository);
	final MockMvc mockMvc = MockMvcBuilders
		.standaloneSetup(controller)
		.apply(documentationConfiguration(this.restDocumentation))
		.build();

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
		.andExpect(content().string(
				objectMapper.writeValueAsString(new Bike("testBike", now).addMilage(now, 23.0)))
		)
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
					fieldWithPath("bike").description("The bike object the new milage belongs to")
				)
			)
		)
		;
	
	// Decommisioned bike
	mockMvc
		.perform(
			post("/api/bikes/3/milages")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(newMilageCmd))
		)
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string("Bike has already been decommissioned."));
	
	verify(repository, times(1)).findOne(1);
	verify(repository, times(1)).findOne(2);
	verify(repository, times(1)).findOne(3);
	verify(repository, times(1)).save(any(Bike.class));
    }

    @Test
    public void testCreateBike1() throws Exception {
	LocalDate now = now();

	final BikeRepository repository = mock(BikeRepository.class);
	when(repository.save(any(Bike.class))).then(returnsFirstArg());	
		
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new BikesController(repository)).apply(documentationConfiguration(this.restDocumentation)).build();

	final BikeCmd newBikeCmd = new BikeCmd();
	newBikeCmd.setBoughtOn(new Date());
	newBikeCmd.setColor("cccccc");
	newBikeCmd.setName("test");

	final Bike bike = new Bike("test", now);
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
				requestFields(
					fieldWithPath("name").description("The name of the new bike"),					
					fieldWithPath("boughtOn").description("The date the new bike was bought"),
					fieldWithPath("color").description("The color of the new bike"),
					fieldWithPath("decommissionedOn").ignored()
				)
			)
		)
		;

	verify(repository, times(1)).save(any(Bike.class));
    }

    @Test
    public void testCreateBike2() throws Exception {
	LocalDate now = now();

	final BikeRepository repository = mock(BikeRepository.class);
	stub(repository.save(any(Bike.class))).toThrow(new DataIntegrityViolationException(""));

	final BikesController controller = new BikesController(repository);
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

	final BikeCmd newBikeCmd = new BikeCmd();
	newBikeCmd.setBoughtOn(new Date());
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

	verify(repository, times(1)).save(any(Bike.class));
    }
    
    @Test
    public void testUpdateBike() throws Exception {
	LocalDate now = now();

	final BikeRepository repository = mock(BikeRepository.class);
	final Bike decommissionedBike = new Bike("decommissioned", now.minusMonths(2).withDayOfMonth(1));	
	decommissionedBike.decommission(now.minusMonths(1));
	stub(repository.findOne(3)).toReturn(decommissionedBike);
	
	Bike bike = new Bike("test", now.minusMonths(1));
	bike.setColor("000000");
	Calendar boughtOn = bike.getBoughtOn();
	stub(repository.findOne(2)).toReturn(bike);
	
	final BikesController controller = new BikesController(repository);
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

	final BikeCmd updatedBikeCmd = new BikeCmd();
	updatedBikeCmd.setBoughtOn(new Date());
	updatedBikeCmd.setDecommissionedOn(new Date());	
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

	verify(repository, times(1)).findOne(1);
	verify(repository, times(1)).findOne(2);
	verify(repository, times(1)).findOne(3);

	Assert.assertEquals("test", bike.getName());
	Assert.assertEquals(boughtOn, bike.getBoughtOn());
	Assert.assertEquals("FFFCCC", bike.getColor());
	Assert.assertEquals(GregorianCalendar.from(now.atStartOfDay(ZoneId.systemDefault())), bike.getDecommissionedOn());

	verifyNoMoreInteractions(repository);
    }
    
    @Test
    public void testUpdateBikeStory() throws Exception {
	LocalDate now = now();

	final BikeRepository repository = mock(BikeRepository.class);
	final Bike decommissionedBike = new Bike("decommissioned", now.minusMonths(2).withDayOfMonth(1));	
	decommissionedBike.decommission(now.minusMonths(1));
	stub(repository.findOne(3)).toReturn(decommissionedBike);
	
	Bike bike = new Bike("test", now.minusMonths(1));
	bike.setColor("000000");
	Calendar boughtOn = bike.getBoughtOn();
	stub(repository.findOne(2)).toReturn(bike);
	
	final BikesController controller = new BikesController(repository);
	final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

	final StoryCmd validNewStoryCmd = new StoryCmd();
	validNewStoryCmd.setLabel("foobar");
	validNewStoryCmd.setUrl("http://heise.de");
	final StoryCmd invalidNewStoryCmd = new StoryCmd();
	invalidNewStoryCmd.setUrl("asdasd");

	// Invalid content
	mockMvc
		.perform(
			put("http://biking.michael-simons.eu/api/bikes/2/story")
			.contentType(APPLICATION_JSON)
			.content("{}")
		)
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));
	
	// Invalid content
	mockMvc
		.perform(
			put("http://biking.michael-simons.eu/api/bikes/2/story")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(invalidNewStoryCmd))
		)
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string("Invalid arguments."));

	// Valid request, invalid bike
	mockMvc
		.perform(
			put("http://biking.michael-simons.eu/api/bikes/1/story")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(validNewStoryCmd))
		)
		.andExpect(status().isNotFound())
		.andExpect(MockMvcResultMatchers.content().string(""));
	
	mockMvc
		.perform(
			put("http://biking.michael-simons.eu/api/bikes/2/story")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(validNewStoryCmd))
		)
		.andExpect(status().isOk());
	
	Assert.assertEquals("test", bike.getName());
	Assert.assertEquals(boughtOn, bike.getBoughtOn());
	Assert.assertEquals("000000", bike.getColor());
	Assert.assertNotNull(bike.getStory());
	Assert.assertEquals("foobar", bike.getStory().getLabel());
	Assert.assertEquals("http://heise.de", bike.getStory().getUrl());
			
	// Empty content
	mockMvc
		.perform(put("http://biking.michael-simons.eu/api/bikes/2/story").contentType(APPLICATION_JSON))
		.andExpect(status().isOk());

	Assert.assertNull(bike.getStory());
	
	// Decommisioned bike
	mockMvc
		.perform(
			put("http://biking.michael-simons.eu/api/bikes/3/story")
			.contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(validNewStoryCmd))
		)
		.andExpect(status().isBadRequest())
		.andExpect(MockMvcResultMatchers.content().string("Bike has already been decommissioned."));

	verify(repository, times(1)).findOne(1);
	verify(repository, times(2)).findOne(2);
	verify(repository, times(1)).findOne(3);

	verifyNoMoreInteractions(repository);
    }
}
