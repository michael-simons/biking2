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
package ac.simons.biking2.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Michael J. Simons, 2014-05-20
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    public void shouldGetLocationsForTheLastNHours() {
        when(locationRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(Mockito.any(Calendar.class))).thenReturn(new ArrayList<>());

        final LocationService locationService = new LocationService(locationRepository, simpMessagingTemplate);

        final List<LocationEntity> locations = locationService.getLocationsForTheLastNHours(3);
        Assert.assertNotNull(locations);
        Assert.assertEquals(0, locations.size());

        ArgumentCaptor<GregorianCalendar> argument = ArgumentCaptor.forClass(GregorianCalendar.class);
        Mockito.verify(locationRepository).findByCreatedAtGreaterThanOrderByCreatedAtAsc(argument.capture());

        Assert.assertThat(ChronoUnit.HOURS.between(argument.getValue().toInstant(), Instant.now()), is(equalTo(3l)));
    }

    @Test
    public void shouldCreateAndSendNewLocation() throws IOException {
        when(locationRepository.save(any(LocationEntity.class))).then(returnsFirstArg());

        final LocationService locationService = new LocationService(locationRepository, simpMessagingTemplate);

        final LocationEntity location = locationService.createAndSendNewLocation(objectMapper.readValue("{\"lon\":\"5\", \"lat\":\"50\"}", NewLocationCmd.class));
        Assert.assertEquals(new BigDecimal(50), location.getLatitude());
        Assert.assertEquals(new BigDecimal(5), location.getLongitude());

        Mockito.verify(locationRepository).save(any(LocationEntity.class));
        final ArgumentCaptor<String> destinationNameArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<LocationEntity> locationArg = ArgumentCaptor.forClass(LocationEntity.class);
        Mockito.verify(simpMessagingTemplate).convertAndSend(destinationNameArg.capture(), locationArg.capture());
        Mockito.verifyNoMoreInteractions(locationRepository, simpMessagingTemplate);

        Assert.assertEquals("/topic/currentLocation", destinationNameArg.getValue());
        Assert.assertEquals(location, locationArg.getValue());
    }

    @Test
    public void getLocationCountShouldWork() {
        when(locationRepository.count()).thenReturn(4711l);

        final LocationService locationService = new LocationService(locationRepository, simpMessagingTemplate);
        Assert.assertEquals(4711l, locationService.getLocationCount());
    }
}
