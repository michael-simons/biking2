/*
 * Copyright 2014 michael-simons.eu.
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
package ac.simons.biking2.tracker;

import ac.simons.biking2.persistence.entities.Location;
import ac.simons.biking2.persistence.repositories.LocationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 * @author Michael J. Simons, 2014-05-20
 */
public class LocationServiceTest {
    @Test
    public void shouldGetLocationsForTheLastNHours() {
	final LocationRepository locationRepository = Mockito.mock(LocationRepository.class);	
	stub(locationRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(Mockito.any(Calendar.class))).toReturn(new ArrayList<>());	
	final SimpMessagingTemplate simpMessagingTemplate = mock(SimpMessagingTemplate.class);
	
	final LocationService locationService = new LocationService(locationRepository, simpMessagingTemplate);
	
	final List<Location> locations = locationService.getLocationsForTheLastNHours(3);	
	Assert.assertNotNull(locations);
	Assert.assertEquals(0, locations.size());
	
	ArgumentCaptor<GregorianCalendar> argument = ArgumentCaptor.forClass(GregorianCalendar.class);
	Mockito.verify(locationRepository).findByCreatedAtGreaterThanOrderByCreatedAtAsc(argument.capture());	  
	
	Assert.assertThat(ChronoUnit.HOURS.between(argument.getValue().toInstant(), Instant.now()), is(equalTo(3l)));    
    }
}
