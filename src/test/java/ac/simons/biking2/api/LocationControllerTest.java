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

package ac.simons.biking2.api;

import ac.simons.biking2.persistence.entities.Location;
import ac.simons.biking2.tracker.LocationService;
import ac.simons.biking2.tracker.NewLocationCmd;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static java.util.Calendar.getInstance;
import static org.junit.rules.ExpectedException.none;

/**
 * @author Michael J. Simons
 */
public class LocationControllerTest {
    @Rule
    public final ExpectedException expectedException = none();
    
    @Test
    public void shouldRetrieveLastLocation() {	
	ZonedDateTime now = ZonedDateTime.now();
	
	final Location l1 = new Location(BigDecimal.ZERO, BigDecimal.ZERO, GregorianCalendar.from(now));
	final Location l2 = new Location(BigDecimal.ZERO, BigDecimal.ZERO, GregorianCalendar.from(now.plusMinutes(1)));
	final LocationService locationService = Mockito.mock(LocationService.class);	
	Mockito.stub(locationService.getLocationsForTheLastNHours(1)).toReturn(Arrays.asList(l1, l2));	
	final LocationController locationController = new LocationController(locationService);
	
	final List<Location> locations = locationController.getLocations();
	
	Assert.assertEquals(2, locations.size());
	Assert.assertTrue(locations.contains(l1));
	Assert.assertTrue(locations.contains(l2));
	
	Mockito.verify(locationService).getLocationsForTheLastNHours(1);	
    }    
    
    @Test
    public void shouldCreateLocation() {
	final NewLocationCmd newLocationCmd = Mockito.mock(NewLocationCmd.class);	
	final LocationService locationService = Mockito.mock(LocationService.class);	
	final Location l = new Location(BigDecimal.ZERO, BigDecimal.ZERO, getInstance());
	Mockito.stub(locationService.createAndSendNewLocation(newLocationCmd)).toReturn(l);
	final BindingResult bindingResult = Mockito.mock(BindingResult.class);
	Mockito.stub(bindingResult.hasErrors()).toReturn(false);
	
	final LocationController locationController = new LocationController(locationService);
	
	final ResponseEntity<Location> r = locationController.createLocation(newLocationCmd, bindingResult);
	
	Assert.assertEquals(HttpStatus.CREATED, r.getStatusCode());
	Assert.assertEquals(l, r.getBody());
    }
    
    @Test
    public void shouldNotCreateInvalidLocation() {
	final NewLocationCmd newLocationCmd = Mockito.mock(NewLocationCmd.class);	
	final LocationService locationService = Mockito.mock(LocationService.class);	
	final BindingResult bindingResult = Mockito.mock(BindingResult.class);
	Mockito.stub(bindingResult.hasErrors()).toReturn(true);
	
	final LocationController locationController = new LocationController(locationService);
	
	this.expectedException.expect(IllegalArgumentException.class);
	this.expectedException.expectMessage("Invalid arguments.");
	
	locationController.createLocation(newLocationCmd, bindingResult);	
    }
    
    @Test
    public void shouldNotCreateDuplicateLocation() {
	final NewLocationCmd newLocationCmd = Mockito.mock(NewLocationCmd.class);	
	final LocationService locationService = Mockito.mock(LocationService.class);
	Mockito.stub(locationService.createAndSendNewLocation(newLocationCmd)).toThrow(new DataIntegrityViolationException("foobar"));
	final BindingResult bindingResult = Mockito.mock(BindingResult.class);
	Mockito.stub(bindingResult.hasErrors()).toReturn(false);
	
	final LocationController locationController = new LocationController(locationService);
	
	final ResponseEntity<Location> r = locationController.createLocation(newLocationCmd, bindingResult);	
	Assert.assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
	Assert.assertNull(r.getBody());
    }
}
