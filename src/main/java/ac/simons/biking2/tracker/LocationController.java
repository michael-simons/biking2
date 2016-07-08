/*
 * Copyright 2014-2016 Michael J. Simons.
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

import java.util.List;
import javax.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Michael J. Simons, 2014-03-20
 */
@RestController
@RequestMapping("/api")
class LocationController {

    private final LocationService locationService;
   
    public LocationController(LocationService locationService) {
	this.locationService = locationService;
    }
    
    @RequestMapping(value = "/locations", method = GET)
    public List<LocationEntity> getLocations() {
	return this.locationService.getLocationsForTheLastNHours(1);
    }
    
    @RequestMapping(value = "/locations/count", method = GET)
    public long getLocationCount() {
	return this.locationService.getLocationCount();
    }

    @RequestMapping(value = "/locations", method = POST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationEntity> createLocation(@RequestBody @Valid final NewLocationCmd newLocationCmd, final BindingResult bindingResult) {
	if (bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}

	ResponseEntity<LocationEntity> rv;

	try {
	    rv = new ResponseEntity<>(this.locationService.createAndSendNewLocation(newLocationCmd), HttpStatus.CREATED);
	} catch (DataIntegrityViolationException e) {
	    rv = new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	return rv;
    }
}
