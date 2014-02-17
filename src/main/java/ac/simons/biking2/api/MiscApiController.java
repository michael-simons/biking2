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

import ac.simons.biking2.model.Coordinate;
import ac.simons.biking2.model.Summary;
import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.AssortedTripRepository;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@RestController
@RequestMapping("/api")
public class MiscApiController {

    private final BikeRepository bikeRepository;
    private final AssortedTripRepository assortedTripRepository;
    private final Coordinate home;

    @Autowired
    public MiscApiController(final BikeRepository bikeRepository, final AssortedTripRepository assortedTripRepository, final Coordinate home) {
	this.bikeRepository = bikeRepository;
	this.assortedTripRepository = assortedTripRepository;
	this.home = home;
    }

    @RequestMapping("/summary")
    public Summary getSummary() {
	final List<Bike> allBikes = this.bikeRepository.findAll();

	final Summary summary = new Summary();
	summary.setDateOfFirstRecord(this.bikeRepository.getDateOfFirstRecord());
	summary.setTotal(
		allBikes.stream().mapToInt(Bike::getMilage).sum()
		+ this.assortedTripRepository.getTotalDistance().doubleValue()
	);
	return summary;
    }
    
    @RequestMapping("/home")
    public Coordinate getHome() {
	return this.home;
    }
}
