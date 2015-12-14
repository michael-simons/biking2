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
package ac.simons.biking2.trips;

import ac.simons.biking2.persistence.entities.AssortedTrip;
import ac.simons.biking2.persistence.repositories.AssortedTripRepository;
import java.math.BigDecimal;
import java.util.Calendar;
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
 * Api for creating new {@link AssortedTrip assorted trips}.
 *
 * @author Michael J. Simons, 2015-06-09
 */
@RestController
@RequestMapping("/api/trips")
public class TripsController {

    private final AssortedTripRepository assortedTripRepository;

    @Autowired
    public TripsController(AssortedTripRepository assortedTripRepository) {
	this.assortedTripRepository = assortedTripRepository;
    }

    @RequestMapping(value = "", method = POST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createTrip(final @RequestBody @Valid NewTripCmd newTrip, final BindingResult bindingResult) {
	ResponseEntity<?> rv;

	if (bindingResult.hasErrors()) {
	    rv = new ResponseEntity<>("Invalid arguments.", HttpStatus.BAD_REQUEST);
	} else {
	    final Calendar coveredOn = Calendar.getInstance();
	    coveredOn.setTime(newTrip.getCoveredOn());
	    try {
		final AssortedTrip trip = this.assortedTripRepository.save(new AssortedTrip(coveredOn, BigDecimal.valueOf(newTrip.getDistance())));
		rv = new ResponseEntity<>(trip, HttpStatus.OK);
	    } catch (DataIntegrityViolationException e) {
		rv = new ResponseEntity<>(HttpStatus.CONFLICT);
	    }
	}

	return rv;
    }
}
