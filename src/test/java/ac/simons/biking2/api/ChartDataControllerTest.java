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

import ac.simons.biking2.api.model.Summary;
import ac.simons.biking2.api.model.highcharts.HighchartsNgConfig;
import ac.simons.biking2.api.model.highcharts.Series;
import ac.simons.biking2.persistence.entities.Bike;
import ac.simons.biking2.persistence.repositories.BikeRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.generate;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 *
 * @author msimons
 */
public class ChartDataControllerTest {
    private final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);
    private final List<Bike> defaultTestData;
    
    public ChartDataControllerTest() {
	final Map<String, Integer[]> testData = new TreeMap<>();
	testData.put("bike1", new Integer[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 150});
	testData.put("bike2", new Integer[]{0, 0, 30, 40, 50, 60, 70, 80, 90, 135, 135, 135});
	testData.put("bike3", new Integer[]{null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null});

	this.defaultTestData = testData.entrySet().stream().map(entry -> {
	    final Bike bike = new Bike(entry.getKey());
	    final Integer[] amounts = entry.getValue();
	    for (int i = 0; i < amounts.length; ++i) {
		if (amounts[i] == null) {
		    continue;
		}
		bike.addMilage(january1st.plusMonths(i), amounts[i]);
	    }
	    return bike;
	}).collect(toList());
    }
    
    @Test
    public void testGetSummary() {
	final Calendar now = Calendar.getInstance();
	
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(defaultTestData);
	stub(bikeRepository.getDateOfFirstRecord()).toReturn(now);
	
	final ChartDataController controller = new ChartDataController(bikeRepository);
	
	final Summary summary = controller.getSummary();
	
	assertThat(summary.getDateOfFirstRecord(), is(equalTo(now)));
	assertThat(summary.getTotal(), is(equalTo(335.0)));
    }
    
    @Test
    public void testGetCurrentDataDataAvailable() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(defaultTestData);

	final ChartDataController controller = new ChartDataController(bikeRepository);
	final HighchartsNgConfig highchartDefinition = controller.getCurrentYear();

	assertThat(highchartDefinition.getSeries().size(), is(equalTo(4)));
	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.get(0).getName(), is(equalTo("bike1")));
	assertThat(hlp.get(0).getData(), is(equalTo(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 30))));

	assertThat(hlp.get(1).getName(), is(equalTo("bike2")));
	assertThat(hlp.get(1).getData(), is(equalTo(Arrays.asList(0, 30, 10, 10, 10, 10, 10, 10, 45, 0, 0, 0))));

	assertThat(hlp.get(2).getName(), is(equalTo("bike3")));
	assertThat(hlp.get(2).getData(), is(equalTo(Arrays.asList(0, 0, 0, 10, 10, 10, 10, 10, 10, 0, 0, 0))));

	assertThat(hlp.get(3).getName(), is(equalTo("Sum")));
	assertThat(hlp.get(3).getData(), is(equalTo(Arrays.asList(10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 30))));

	assertThat(highchartDefinition.getOptions().getyAxis().getMax(), is(equalTo(65)));
    }

    @Test
    public void testGetCurrentDataNoData() {
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(new ArrayList<>());

	final ChartDataController controller = new ChartDataController(bikeRepository);
	final HighchartsNgConfig highchartDefinition = controller.getCurrentYear();

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.get(0).getName(), is(equalTo("Sum")));
	assertThat(hlp.get(0).getData(), is(equalTo(generate(() -> 0).limit(12).collect(ArrayList::new, ArrayList::add, ArrayList::addAll))));
    }

    @Test
    public void testGetCurrentDataNoMilages() {
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(Arrays.asList(new Bike("bike1"), new Bike("bike2")));

	final ChartDataController controller = new ChartDataController(bikeRepository);
	final HighchartsNgConfig highchartDefinition = controller.getCurrentYear();

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.get(2).getName(), is(equalTo("Sum")));
	assertThat(hlp.get(2).getData(), is(equalTo(generate(() -> 0).limit(12).collect(ArrayList::new, ArrayList::add, ArrayList::addAll))));
    }
}
