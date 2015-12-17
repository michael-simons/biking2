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
package ac.simons.biking2.bikes;

import ac.simons.biking2.support.ResourceNotFoundException;
import ac.simons.biking2.bikes.BikeEntity.Link;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
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
@RequestMapping("/api")
class BikesController {

    private final BikeRepository bikeRepository;

    @Autowired
    public BikesController(final BikeRepository bikeRepository) {
	this.bikeRepository = bikeRepository;
    }

    @RequestMapping(value = "/bikes", method = GET)
    public List<BikeEntity> getBikes(final @RequestParam(required = false, defaultValue = "false") boolean all) {
	List<BikeEntity> rv;
	if(all) {
	    rv = bikeRepository.findAll(new Sort(Sort.Direction.ASC, "boughtOn", "decommissionedOn", "name"));
	} else {
	    rv = bikeRepository.findByDecommissionedOnIsNull(new Sort(Sort.Direction.ASC, "name"));
	}
	return rv;
    }
    
    @RequestMapping(value = "/bikes/{id:\\d+}/milages", method = POST)
    @PreAuthorize("isAuthenticated()")
    public MilageEntity createMilage(final @PathVariable Integer id, final @RequestBody @Valid NewMilageCmd cmd, final BindingResult bindingResult) {	
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	final BikeEntity bike = bikeRepository.findOne(id);
	
	MilageEntity rv;	
	if(bike == null) {
	    throw new ResourceNotFoundException();
	} else if(bike.getDecommissionedOn() != null) { 
	    throw new IllegalArgumentException("Bike has already been decommissioned.");
	} else {
	    rv = bike.addMilage(cmd.recordedOnAsLocalDate(), cmd.getAmount());
	    this.bikeRepository.save(bike);
	}
	
	return rv;
    }
    
    @RequestMapping(value = "/bikes", method = POST) 
    @PreAuthorize("isAuthenticated()")
    public BikeEntity createBike(final @RequestBody @Valid BikeCmd newBike, final BindingResult bindingResult) {
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	final BikeEntity bike = new BikeEntity(newBike.getName(), newBike.boughtOnAsLocalDate());
	bike.setColor(newBike.getColor());
	bike.addMilage(newBike.boughtOnAsLocalDate().withDayOfMonth(1), 0);
	
	return this.bikeRepository.save(bike);	
    }
    
    @RequestMapping(value = "/bikes/{id:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public BikeEntity updateBike(final @PathVariable Integer id, final @RequestBody @Valid BikeCmd updatedBike, final BindingResult bindingResult) {
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	final BikeEntity bike = bikeRepository.findOne(id);
		
	if(bike == null) {
	    throw new ResourceNotFoundException();
	} else if(bike.getDecommissionedOn() != null) { 
	    throw new IllegalArgumentException("Bike has already been decommissioned.");
	} else {
	    bike.setColor(updatedBike.getColor());
	    bike.decommission(updatedBike.decommissionedOnAsLocalDate());	  	    
	}	
	return bike;
    }
    
    @RequestMapping(value = "/bikes/{id:\\d+}/story", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public BikeEntity updateBikeStory(final @PathVariable Integer id, final @RequestBody(required = false) @Valid StoryCmd newStory, final BindingResult bindingResult) {
	if(bindingResult.hasErrors()) {
	    throw new IllegalArgumentException("Invalid arguments.");
	}
	
	final BikeEntity bike = bikeRepository.findOne(id);
		
	if(bike == null) {
	    throw new ResourceNotFoundException();
	} else if(bike.getDecommissionedOn() != null) { 
	    throw new IllegalArgumentException("Bike has already been decommissioned.");
	} else {
	    bike.setStory(Optional.ofNullable(newStory).map(c -> new Link(c.getUrl(), c.getLabel())).orElse(null));	    
	}
	return bike;
    }     
}
