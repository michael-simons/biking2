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

import ac.simons.biking2.tracker.NewLocationCmd;
import ac.simons.biking2.persistence.entities.Location;
import ac.simons.biking2.tracker.LocationService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Michael J. Simons, 2014-03-20
 */
@RestController
@RequestMapping("/api")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
	this.locationService = locationService;
    }

    @RequestMapping(value = "/locations", method = POST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Location> createLocation(final @RequestBody @Valid NewLocationCmd newLocationCmd, final BindingResult bindingResult) {
	if (bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}

	ResponseEntity<Location> rv;

	try {
	    rv = new ResponseEntity<>(this.locationService.createAndSendNewLocation(newLocationCmd), HttpStatus.OK);
	} catch (DataIntegrityViolationException e) {
	    rv = new ResponseEntity<>(HttpStatus.CONFLICT);
	}

	return rv;
    }
}
