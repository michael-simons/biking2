/*
 * Copyright 2014 msimons.
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

import ac.simons.biking2.highcharts.HighchartDefinition;
import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael J. Simons, 2014-02-09
 */
public class ChartDataController {

    private final BikeRepository bikeRepository;

    @Autowired
    public ChartDataController(final BikeRepository bikeRepository) {
	this.bikeRepository = bikeRepository;
    }

    public HighchartDefinition getCurrentData() {
	// Start of current year
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	// All active bikes
	final List<Bike> bikes = this.bikeRepository.findActive(Date.from(january1st.atStartOfDay(ZoneId.systemDefault()).toInstant()));

	final HighchartDefinition.Builder builder = HighchartDefinition.define();
	
	final Integer[] sums = bikes.stream().sequential().map(bike -> {
	    final Integer[] milagesInYear = bike.getMilagesInYear(january1st.getYear());
	    builder.series()
		    .withName(bike.getName())
		    .withType("column")
		    .withData(milagesInYear)
		    .build();
	    return milagesInYear;
	}).reduce((a, b) -> {
	    Integer[] result = Arrays.copyOf(a, a.length);
	    for (int i = 0; i < result.length; ++i) {
		result[i] += b[i];
	    }
	    return result;
	}).get();
	
	builder.series()
		.withName("Sum")
		.withType("spline")
		.withData(sums)
		.build();
	
	return builder.build();
    }
}
