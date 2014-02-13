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

import ac.simons.biking2.highcharts.HighchartsNgConfig;
import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static java.util.stream.IntStream.generate;
import static java.util.stream.IntStream.rangeClosed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons, 2014-02-09
 */
@RestController
@RequestMapping("/api")
public class ChartDataController {

    private final BikeRepository bikeRepository;

    @Autowired
    public ChartDataController(final BikeRepository bikeRepository) {
	this.bikeRepository = bikeRepository;
    }
    
    @RequestMapping("/summary")
    public Summary getSummary() {
	final List<Bike> allBikes = this.bikeRepository.findAll();
	
	final Summary summary = new Summary();
	summary.setDateOfFirstRecord(this.bikeRepository.getDateOfFirstRecord());
	summary.setTotal((double)allBikes.stream().mapToInt(Bike::getMilage).sum());
	return summary;
    }

    @RequestMapping("/currentYear")
    public HighchartsNgConfig getCurrentYear() {
	// Start of current year
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	// All active bikes
	final List<Bike> bikes = this.bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())));

	final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define();
	
	// Add the bike charts as columns
	final int[] sums = bikes.stream().sequential().map(bike -> {
	    final int[] milagesInYear = bike.getMilagesInYear(january1st.getYear());
	    builder.series()
		    .withName(bike.getName())
		    .withType("column")
		    .withData(milagesInYear)
		    .build();	
	    return milagesInYear;
	}).reduce((a, b) -> {
	    int[] result = Arrays.copyOf(a, a.length);
	    for (int i = 0; i < result.length; ++i) {
		result[i] += b[i];
	    }
	    return result;
	}).orElse(generate(() -> 0).limit(12).toArray());
	
	// Add sum as spline and compute maximum y value
	final int currentMaxYValue  = 
		builder.series()
		    .withName("Sum")
		    .withType("spline")
		    .withData(sums)
		    .build()
		.computeCurrentMaxYValue().intValue();
	
	final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
	return builder		
		.options()
		    .chart()
			.withBorderWidth(1)
			.build()
		    .credits()
			.disable()
			.build()
		    .title()
			.withText(String.format("Michis milage in %d", january1st.getYear()))
			.build()
		    .xAxis()
			.withCategories(
			    rangeClosed(1, 12).mapToObj(i -> january1st.withMonth(i).format(dateTimeFormat)).toArray(size -> new String[size])
			)
			.build()
		    .yAxis()
			.withMin(0)
			.withMax(currentMaxYValue)
			.withTickInterval(100)
			.enableEndOnTick()
			.title()
			    .withText("Milage (km)")
			    .build()
			.build()
		    .tooltip()
			.withHeaderFormat("{point.key}<table>")
			.withPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td><td style=\"padding:0\"><b>{point.y:.1f} km</b></td></tr>")
			.withFooterFormat("</table>")
			.share()
			.useHTML()
			.build()	
		    .plotOptions()
			.column()
			    .withPointPadding(0.2)
			    .withBorderWidth(0)
			    .build()
			.series()
			    .disableAnimation()
			    .build()				    
			.build()
		    .build()
		.build();	
    }
}
