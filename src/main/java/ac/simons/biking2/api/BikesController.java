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
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/api/bikes")
    public List<Bike> getBikes() {	
	return bikeRepository.findByDecommissionedOnIsNull(new Sort(Sort.Direction.ASC, "name"));
    }
}
