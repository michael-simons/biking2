/*
 * Copyright 2015-2019 michael-simons.eu.
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import org.joor.Reflect;
import static org.mockito.Mockito.when;

/**
 * @author Michael J. Simons
 * 
 * @since 2015-12-16
 */
@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @Test
    void shouldRetrieveLastLocation() {
        OffsetDateTime now = OffsetDateTime.now();

        final LocationEntity l1 = new LocationEntity(BigDecimal.ZERO, BigDecimal.ZERO, now);
        final LocationEntity l2 = new LocationEntity(BigDecimal.ZERO, BigDecimal.ZERO, now.plusMinutes(1));
        when(locationService.getLocationsForTheLastNHours(1)).thenReturn(Arrays.asList(l1, l2));
        final LocationController locationController = new LocationController(locationService);

        final List<LocationEntity> locations = locationController.getLocations();

        Assertions.assertEquals(2, locations.size());
        Assertions.assertTrue(locations.contains(l1));
        Assertions.assertTrue(locations.contains(l2));

        Mockito.verify(locationService).getLocationsForTheLastNHours(1);
    }

    @Test
    void shouldCreateLocation() {
        final NewLocationCmd newLocationCmd = new NewLocationCmd(null, null, null, null);
        final LocationEntity l = new LocationEntity(BigDecimal.ZERO, BigDecimal.ZERO, OffsetDateTime.now());
        when(locationService.createAndSendNewLocation(newLocationCmd)).thenReturn(l);
        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        final LocationController locationController = new LocationController(locationService);

        final ResponseEntity<LocationEntity> r = locationController.createLocation(newLocationCmd, bindingResult);

        Assertions.assertEquals(HttpStatus.CREATED, r.getStatusCode());
        Assertions.assertEquals(l, r.getBody());
    }

    @Test
    void shouldNotCreateInvalidLocation() {
        final NewLocationCmd newLocationCmd = new NewLocationCmd(null, null, null, null);
        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        final LocationController locationController = new LocationController(locationService);

        var msg = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> locationController.createLocation(newLocationCmd, bindingResult)).getMessage();
        Assertions.assertEquals("Invalid arguments.", msg);
    }

    @Test
    void shouldNotCreateDuplicateLocation() {
        final NewLocationCmd newLocationCmd = new NewLocationCmd(new BigDecimal("1"), new BigDecimal("2"), null, System.currentTimeMillis());
        
        when(locationService.createAndSendNewLocation(newLocationCmd)).thenThrow(new DataIntegrityViolationException("foobar"));
        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        final LocationController locationController = new LocationController(locationService);

        final ResponseEntity<LocationEntity> r = locationController.createLocation(newLocationCmd, bindingResult);
        Assertions.assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        Assertions.assertNull(r.getBody());
    }

    @Test
    void getLocationCountShouldWork() {
        when(locationService.getLocationCount()).thenReturn(4711l);

        final LocationController locationController = new LocationController(locationService);

        Assertions.assertEquals(4711l, locationController.getLocationCount());
    }
}
