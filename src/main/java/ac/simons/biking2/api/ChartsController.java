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
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.IntStream.generate;
import static java.util.stream.IntStream.rangeClosed;

/**
 * @author Michael J. Simons, 2014-02-09
 */
@RestController
@RequestMapping("/api")
public class ChartsController {

    private final BikeRepository bikeRepository;
    private final String colorOfCumulativeGraph;
    
    @Autowired
    public ChartsController(final BikeRepository bikeRepository, final @Value("${biking2.color-of-cumulative-graph:000000}") String colorOfCumulativeGraph) {
	this.bikeRepository = bikeRepository;	
	this.colorOfCumulativeGraph = colorOfCumulativeGraph;
    }

    @RequestMapping("/charts/currentYear")
    public HighchartsNgConfig getCurrentYear() {
	// Start of current year
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	// All active bikes
	final List<Bike> bikes = this.bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())));
	final Map<LocalDate, Integer> summarizedPeriods = Bike.summarizePeriods(bikes, entry -> !entry.getKey().isBefore(january1st));
	
	final Map<String, Object> userData = new HashMap<>();
	userData.put("worstPeriod", Bike.getWorstPeriod(summarizedPeriods));
	userData.put("bestPeriod", Bike.getBestPeriod(summarizedPeriods));
	userData.put("average", summarizedPeriods.entrySet().stream().mapToInt(entry -> entry.getValue()).average().orElseGet(() -> 0.0));
	userData.put("preferredBike", bikes.stream().max(new Bike.BikeByMilageInYearComparator(january1st.getYear())).orElse(null));
	userData.put("currentYear", january1st.getYear());
	
	final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define();
	builder.withUserData(userData);
	
	// Add the bike charts as columns
	final int[] sums = bikes.stream().sequential().map(bike -> {
	    final int[] milagesInYear = bike.getMilagesInYear(january1st.getYear());
	    builder.series()
		    .withName(bike.getName())
		    .withColor("#" + bike.getColor())
		    .withType("column")
		    .withData(milagesInYear)
		    .build();	
	    return milagesInYear;
	}).reduce(ChartsController::addArrays).orElse(generate(() -> 0).limit(12).toArray());
	
	userData.put("currentYearSum", Arrays.stream(sums).sum());
	
	// Add sum as spline and compute maximum y value
	final int currentMaxYValue  = 
		builder.series()
		    .withName("Sum")
		    .withColor("#" + colorOfCumulativeGraph)
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
			.withHeaderFormat("{point.key}<table class=\"table table-condensed table-striped\">")
			.withPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td><td style=\"padding:0\"><b>{point.y:.1f} km</b></td></tr>")
			.withFooterFormat("</table>")
			.enableCrosshairs()
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
    
    @RequestMapping("/charts/history")
    public HighchartsNgConfig getHistory() {
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);
	
	final List<Bike> bikes = this.bikeRepository.findAll();	
	
	final Map<String, Object> userData = new HashMap<>();	
	final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define();
	
	final Map<Integer, int[]> data = bikes
	    // Stream the bikes 
	    .stream()
	    // and flatMap (concat) their periods into a new stream
	    .flatMap(bike -> bike.getPeriods().entrySet().stream())
	    // we're only interested in periods before 1.1 of the current year
	    .filter(entry -> entry.getKey().isBefore(january1st))
	    // Collect those periods in
	    .collect(
		    // a tree map 
		    TreeMap::new, 
		    // map each period into an array
		    (map, period) -> {
			// create the array if necessary			    
			int[] year = map.computeIfAbsent(period.getKey().getYear(), key -> generate(() -> 0).limit(12).toArray());
			// add to the array
			year[period.getKey().getMonthValue()-1] += period.getValue();
		    }, 
		    // Merge the array (necessary if the stream runs in parallel)
		    (map1, map2) -> {			    
			map2.forEach((k, v) -> {
			    map1.merge(k, v, ChartsController::addArrays);
			});
		    }
	    );
	// Create series in builder
	data.forEach((k,v) -> builder.series().withName(Integer.toString(k)).withData(v).build());

	final StringBuilder title = new StringBuilder();
	if(data.isEmpty()) {
	    title.append("No historical data available.");
	} else {
	    // Compute summed years
	    final Map<Integer, Integer> summedYears = data.entrySet().stream()
		    .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.stream(entry.getValue()).sum()));
	    
	    // Computed worst and best year from summed years
	    final Optional<Map.Entry<Integer, Integer>> worstYear = summedYears.entrySet().stream().min(Map.Entry.comparingByValue());
	    final Optional<Map.Entry<Integer, Integer>> bestYear = summedYears.entrySet().stream().max(Map.Entry.comparingByValue());
	    // Only add them if they differ
	    if(!worstYear.equals(bestYear)) {
		userData.put("worstYear", worstYear.orElse(null));
		userData.put("bestYear", bestYear.orElse(null));
	    }
	   
	    // Preferred bikes...
	    userData.put("preferredBikes",
		data.keySet().stream().collect(
			TreeMap::new, 
			(map, year) -> map.put(year, bikes.stream().max(Comparator.comparingInt(bike -> bike.getMilageInYear(year))).get()), 
			TreeMap::putAll
		)
	    );
	    
	    title.append("Milage ")
		    .append(data.keySet().stream().min(Integer::compare).get())
		    .append("-")
		    .append(data.keySet().stream().max(Integer::compare).get());
	}
	
	final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
	return builder
		.withUserData(userData)
		.options()
		    .chart()
			.withBorderWidth(1)
			.withType("line")
			.build()
		    .credits()
			.disable()
			.build()
		    .title()
			.withText(title.toString())
			.build()
		    .xAxis()
			.withCategories(
			    rangeClosed(1, 12).mapToObj(i -> january1st.withMonth(i).format(dateTimeFormat)).toArray(size -> new String[size])
			)
			.build()
		    .yAxis()
			.withMin(0)			
			.withTickInterval(100)
			.enableEndOnTick()
			.title()
			    .withText("Milage (km)")
			    .build()
			.build()
		    .tooltip()
			.withHeaderFormat("{point.key}<table class=\"table table-condensed table-striped\">")
			.withPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td><td style=\"padding:0\"><b>{point.y:.1f} km</b></td></tr>")
			.withFooterFormat("</table>")
			.enableCrosshairs()
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
    
    @RequestMapping("/charts/monthlyAverage")
    public HighchartsNgConfig getMonthlyAverage() {
	// Get all bikes
	final List<Bike> bikes = this.bikeRepository.findAll();		

	// Compute Integer Statistics by grouping periods by month
	final Map<Integer, IntSummaryStatistics> statistics = bikes.stream()
		.flatMap(bike -> bike.getPeriods().entrySet().stream())		
		// Sum all bikes periods per year/month together
		.collect(groupingBy(e -> e.getKey(), summingInt(Entry::getValue)))
		.entrySet().stream()
		// create monthly statistics
		.collect(groupingBy(e -> e.getKey().getMonthValue(), summarizingInt(Entry::getValue)));	
	
	// Create stupid arrays from statistics...
	final Double[] averages = new Double[12];
	final Integer[][] ranges = new Integer[12][];	
	for(int i=0; i<12; ++i) {
	    final IntSummaryStatistics s = statistics.get(i+1);
	    if(s == null) {
		averages[i] = 0.0;
		ranges[i] = new Integer[]{0,0};	
	    } else {
		averages[i] = s.getAverage();
		ranges[i] = new Integer[]{s.getMin(), s.getMax()};
	    }
	}
	
	// Total average as plotline
	final Map<LocalDate, Integer> summarizedPeriods = Bike.summarizePeriods(bikes, null);
	
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);	
	final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
	final HighchartsNgConfig.Builder builder = HighchartsNgConfig.define()		
		.options()
		    .chart()
			.withBorderWidth(1)
			.withType("line")
			.build()
		    .credits()
			.disable()
			.build()
		    .title()
			.withText("Monthly average")
			.build()
		    .xAxis()
			.withCategories(
			    rangeClosed(1, 12).mapToObj(i -> january1st.withMonth(i).format(dateTimeFormat)).toArray(size -> new String[size])
			)
			.build()				    
		    .yAxis()
			.withMin(0)			
			.withTickInterval(100)
			.withPlotLine()
			    .at(summarizedPeriods.entrySet().stream().mapToInt(entry -> entry.getValue()).average().orElseGet(() -> 0.0))
			    .withWidth(2.0)
			    .withColor("#F04124")
			    .build()
			.enableEndOnTick()
			.title()
			    .withText("Milage (km)")
			    .build()
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
		    .tooltip()
			.share()
			.enableCrosshairs()
			.withValueSuffix("km")
			.build()
		    .build()
		.<Number>series()
		    .withName("Average")
		    .withColor("#7CB5EB")
		    .withZIndex(1)		
		    .marker()
			.withLineWidth(2.0)
			.withFillColor("#FFFFFF")
			.withLineColor("#7CB5EB")
			.build()
		    .withData(averages)	
		    .build()
		.<Number[]>series()
		    .withName("Range")
		    .withType("arearange")		    
		    .withLineWidth(0.0)		    
		    .withFillOpacity(0.3)
		    .withColor("#7CB5EB")
		    .withZIndex(0)
		    .linkTo("previous")
		    .withData(ranges)
		    .build();
	
	return builder.build();
    }
    
    private static int[] addArrays(final int[] a, final int[] b) {
	int[] result = Arrays.copyOf(a, a.length);
	for (int i = 0; i < result.length; ++i) {
	    result[i] += b[i];
	}
	return result;
    }
}
