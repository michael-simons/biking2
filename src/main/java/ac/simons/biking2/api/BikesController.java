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
import ac.simons.biking2.persistence.entities.Milage;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * @author Michael J. Simons, 2014-02-19
 */
@RestController
public class BikesController {

    private final BikeRepository bikeRepository;

    @Autowired
    public BikesController(final BikeRepository bikeRepository) {
	this.bikeRepository = bikeRepository;
    }

    @RequestMapping(value = "/api/bikes", method = GET)
    public List<Bike> getBikes(final @RequestParam(required = false, defaultValue = "false") boolean all) {
	List<Bike> rv;
	if(all) {
	    rv = bikeRepository.findAll(new Sort(Sort.Direction.ASC, "boughtOn", "decommissionedOn", "name"));
	} else {
	    rv = bikeRepository.findByDecommissionedOnIsNull(new Sort(Sort.Direction.ASC, "name"));
	}
	return rv;
    }
    
    @RequestMapping(value = "/api/bikes/{id:\\d+}/milages", method = POST)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Milage> createMilage(final @PathVariable Integer id, final @RequestBody @Valid NewMilageCmd cmd, final BindingResult bindingResult) {	
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	final Bike bike = bikeRepository.findOne(id);
	
	ResponseEntity<Milage> rv;	
	if(bike == null) {
	    rv = new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} else if(bike.getDecommissionedOn() != null) { 
	    throw new IllegalArgumentException("Bike has already been decommissioned.");
	} else {
	    final Milage milage = bike.addMilage(cmd.recordedOnAsLocalDate(), cmd.getAmount());
	    this.bikeRepository.save(bike);

	    rv = new ResponseEntity<>(milage, HttpStatus.OK);
	}
	
	return rv;
    }
    
    @RequestMapping(value = "/api/bikes", method = POST) 
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Bike> createBike(final @RequestBody @Valid BikeCmd newBike, final BindingResult bindingResult) {
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	ResponseEntity<Bike> rv;
	
	final Bike bike = new Bike(newBike.getName(), newBike.boughtOnAsLocalDate());
	bike.setColor(newBike.getColor());
	bike.addMilage(newBike.boughtOnAsLocalDate().withDayOfMonth(1), 0);
	
	try {
	    this.bikeRepository.save(bike);
	    rv = new ResponseEntity<>(bike, HttpStatus.OK);
	} catch(DataIntegrityViolationException e) {	    
	    rv = new ResponseEntity<>(HttpStatus.CONFLICT);
	}
	
	return rv;
    }
    
    @RequestMapping(value = "/api/bikes/{id:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Bike> updateBike(final @PathVariable Integer id, final @RequestBody @Valid BikeCmd updatedBike, final BindingResult bindingResult) {
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	final Bike bike = bikeRepository.findOne(id);
	
	ResponseEntity<Bike> rv;	
	if(bike == null) {
	    rv = new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} else if(bike.getDecommissionedOn() != null) { 
	    throw new IllegalArgumentException("Bike has already been decommissioned.");
	} else {
	    bike.setColor(updatedBike.getColor());
	    bike.decommission(updatedBike.decommissionedOnAsLocalDate());	  
	    rv = new ResponseEntity<>(bike, HttpStatus.OK);
	}
	return rv;
    }
    
    @ExceptionHandler(IllegalArgumentException.class)    
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) throws Exception {	
	return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
