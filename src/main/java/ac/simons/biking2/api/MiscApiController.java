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

import ac.simons.biking2.misc.About;
import ac.simons.biking2.misc.About.VMProperties;
import ac.simons.biking2.misc.AccumulatedPeriod;
import ac.simons.biking2.misc.Summary;
import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.AssortedTripRepository;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.Runtime.getRuntime;

/**
 * @author Michael J. Simons, 2014-02-17
 */
@RestController
@RequestMapping("/api")
@EnableConfigurationProperties(BuildProperties.class)
public class MiscApiController {

    private final BikeRepository bikeRepository;
    private final AssortedTripRepository assortedTripRepository;
    private final Coordinate home;
    private final BuildProperties builtProperties;

    @Autowired
    public MiscApiController(final BikeRepository bikeRepository, final AssortedTripRepository assortedTripRepository, final Coordinate home, final BuildProperties builtProperties) {
	this.bikeRepository = bikeRepository;
	this.assortedTripRepository = assortedTripRepository;
	this.home = home;
	this.builtProperties = builtProperties;
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
	
	final Map<LocalDate, Integer> summarizedPeriods = Bike.summarizePeriods(allBikes);
	
	summary.setMinimumPeriod(
		    summarizedPeriods
			.entrySet()
			.stream()
			.min(Bike::comparePeriodsByValue)
			.map(entry -> new AccumulatedPeriod(entry.getKey(), entry.getValue()))
			.orElse(null)
	);
	
	summary.setMaximumPeriod(
		    summarizedPeriods
			.entrySet()
			.stream()
			.max(Bike::comparePeriodsByValue)
			.map(entry -> new AccumulatedPeriod(entry.getKey(), entry.getValue()))
			.orElse(null)
	);
		
	return summary;
    }

    @RequestMapping("/home")
    public Coordinate getHome() {
	return this.home;
    }

    @RequestMapping("/about")
    public About about() {
	final About about = new About();
	final VMProperties vMProperties = new VMProperties();

	final Runtime runtime = getRuntime();	
	final long freeMemory = runtime.freeMemory();
	vMProperties.setAvailableMemory(runtime.totalMemory());
	vMProperties.setUsedMemory(runtime.totalMemory() - freeMemory);
	vMProperties.setFreeMemory(freeMemory);
	vMProperties.setMaxMemory(runtime.maxMemory());
	vMProperties.setUptime(Duration.ofSeconds(ManagementFactory.getRuntimeMXBean().getUptime() / 1000));

	about.setVm(vMProperties);
	about.setBuild(builtProperties);

	return about;
    }
}
