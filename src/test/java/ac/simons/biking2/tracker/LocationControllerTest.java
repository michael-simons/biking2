/*
 * Copyright 2015-2017 michael-simons.eu.
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.rules.ExpectedException.none;
import static java.util.Calendar.getInstance;
import org.joor.Reflect;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Michael J. Simons, 2015-12-16
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationControllerTest {
    @Rule
    public final ExpectedException expectedException = none();

    @Mock
    private LocationService locationService;

    @Test
    public void shouldRetrieveLastLocation() {
        ZonedDateTime now = ZonedDateTime.now();

        final LocationEntity l1 = new LocationEntity(BigDecimal.ZERO, BigDecimal.ZERO, GregorianCalendar.from(now));
        final LocationEntity l2 = new LocationEntity(BigDecimal.ZERO, BigDecimal.ZERO, GregorianCalendar.from(now.plusMinutes(1)));
        when(locationService.getLocationsForTheLastNHours(1)).thenReturn(Arrays.asList(l1, l2));
        final LocationController locationController = new LocationController(locationService);

        final List<LocationEntity> locations = locationController.getLocations();

        Assert.assertEquals(2, locations.size());
        Assert.assertTrue(locations.contains(l1));
        Assert.assertTrue(locations.contains(l2));

        Mockito.verify(locationService).getLocationsForTheLastNHours(1);
    }

    @Test
    public void shouldCreateLocation() {
        final NewLocationCmd newLocationCmd = new NewLocationCmd();
        final LocationEntity l = new LocationEntity(BigDecimal.ZERO, BigDecimal.ZERO, getInstance());
        when(locationService.createAndSendNewLocation(newLocationCmd)).thenReturn(l);
        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        final LocationController locationController = new LocationController(locationService);

        final ResponseEntity<LocationEntity> r = locationController.createLocation(newLocationCmd, bindingResult);

        Assert.assertEquals(HttpStatus.CREATED, r.getStatusCode());
        Assert.assertEquals(l, r.getBody());
    }

    @Test
    public void shouldNotCreateInvalidLocation() {
        final NewLocationCmd newLocationCmd = new NewLocationCmd();
        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        final LocationController locationController = new LocationController(locationService);

        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage("Invalid arguments.");

        locationController.createLocation(newLocationCmd, bindingResult);
    }

    @Test
    public void shouldNotCreateDuplicateLocation() {
        final NewLocationCmd newLocationCmd = Reflect
                .on(new NewLocationCmd())
                .set("latitude", new BigDecimal("1"))
                .set("longitude", new BigDecimal("2")).get();
        
        when(locationService.createAndSendNewLocation(newLocationCmd)).thenThrow(new DataIntegrityViolationException("foobar"));
        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        final LocationController locationController = new LocationController(locationService);

        final ResponseEntity<LocationEntity> r = locationController.createLocation(newLocationCmd, bindingResult);
        Assert.assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        Assert.assertNull(r.getBody());
    }

    @Test
    public void getLocationCountShouldWork() {
        when(locationService.getLocationCount()).thenReturn(4711l);

        final LocationController locationController = new LocationController(locationService);

        Assert.assertEquals(4711l, locationController.getLocationCount());
    }
}
