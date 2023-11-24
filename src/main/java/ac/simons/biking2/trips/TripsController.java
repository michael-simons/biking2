/*
 * Copyright 2015-2023 michael-simons.eu.
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

import java.math.BigDecimal;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Api for creating new {@link AssortedTripEntity assorted trips}.
 *
 * @author Michael J. Simons
 * @since 2015-06-09
 */
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@RequestMapping("/api/trips")
@Slf4j
class TripsController {

    private final AssortedTripRepository assortedTripRepository;

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> createTrip(@RequestBody @Valid final NewTripCmd newTrip, final BindingResult bindingResult) {
        ResponseEntity<Object> rv;

        if (bindingResult.hasErrors()) {
            rv = new ResponseEntity<>("Invalid arguments.", HttpStatus.BAD_REQUEST);
        } else {
            try {
                final AssortedTripEntity trip = this.assortedTripRepository.save(new AssortedTripEntity(newTrip.coveredOn(), BigDecimal.valueOf(newTrip.distance())));
                rv = new ResponseEntity<>(trip, HttpStatus.OK);
            } catch (DataIntegrityViolationException e) {
                log.debug("Data integrity violation while uploading a new trip", e);
                rv = new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        return rv;
    }
}
