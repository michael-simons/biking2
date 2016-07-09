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
package ac.simons.biking2.bikes;

import ac.simons.biking2.bikes.highcharts.HighchartsNgConfig;
import ac.simons.biking2.bikes.highcharts.Series;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.generate;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 *
 * @author msimons
 */
public class ChartsControllerTest {
    private final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);
    private final List<BikeEntity> defaultTestData;

    public static List<BikeEntity> generateTestData() {
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	final Map<String, Integer[]> testData = new TreeMap<>();
	testData.put("bike1", new Integer[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 150});
	testData.put("bike2", new Integer[]{0, 0, 30, 40, 50, 60, 70, 80, 90, 135, 135, 135});
	testData.put("bike3", new Integer[]{null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null});

	return testData.entrySet().stream().map(entry -> {
	    final BikeEntity bike = new BikeEntity(entry.getKey(), LocalDate.now());
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

    public ChartsControllerTest() {
	this.defaultTestData = generateTestData();
    }

    @Test
    public void testGetCurrentYearDataAvailable() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(defaultTestData);

	final ChartsController controller = new ChartsController(bikeRepository, "000000");
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
    public void testGetCurrentYearNoData() {
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(new ArrayList<>());

	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig highchartDefinition = controller.getCurrentYear();

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.get(0).getName(), is(equalTo("Sum")));
	assertThat(hlp.get(0).getData(), is(equalTo(generate(() -> 0).limit(12).collect(ArrayList::new, ArrayList::add, ArrayList::addAll))));
    }

    @Test
    public void testGetCurrentYearNoMilages() {
	final LocalDate january1st = LocalDate.now().withMonth(1).withDayOfMonth(1);

	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(Arrays.asList(new BikeEntity("bike1", LocalDate.now()), new BikeEntity("bike2", LocalDate.now())));

	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig highchartDefinition = controller.getCurrentYear();

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.get(2).getName(), is(equalTo("Sum")));
	assertThat(hlp.get(2).getData(), is(equalTo(generate(() -> 0).limit(12).collect(ArrayList::new, ArrayList::add, ArrayList::addAll))));
    }

    @Test
    public void testGetHistoryDataAvailable() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	// Default Testdata has no historical data
	stub(bikeRepository.findAll()).toReturn(defaultTestData);

	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig highchartDefinition = controller.getHistory(Optional.empty(), Optional.empty());

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.size(), is(equalTo(0)));
    }

    @Test
    public void testCompleteData() {
	// Arange
	final LocalDate startDate = january1st.minusYears(2);
	final Calendar _startDate = GregorianCalendar.from(startDate.atStartOfDay(ZoneId.systemDefault()));

	final Map<String, Integer[]> testData = new TreeMap<>();
	testData.put("bike1", new Integer[]{
	     10,  20,  30,  40,  50,  60,  70,  80,  90, 100, 110, 120,
	    150, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300,
	    310, 310, 310, 320, 330, 340, 350, 360, 370, 380, 395, 400
	 //
	});
	testData.put("bike2", new Integer[]{
	    0,   0,  30,  40,  50,  60,  70,  80,  90,  135,  135,  135,
	  140, 140, 200, 300, 400, 500, 600, 700, 800,  900, 1500, 1500
	});
	testData.put("bike3", new Integer[]{
	    null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null
	});

	final List<BikeEntity> bikes = testData.entrySet().stream().map(entry -> {
	    final BikeEntity bike = new BikeEntity(entry.getKey(), LocalDate.now());
	    final Integer[] amounts = entry.getValue();
	    for (int i = 0; i < amounts.length; ++i) {
		if (amounts[i] == null) {
		    continue;
		}
		bike.addMilage(startDate.plusMonths(i), amounts[i]);
	    }
	    return bike;
	}).collect(toList());
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(Arrays.asList(bikes.get(0)));
	stub(bikeRepository.findAll()).toReturn(bikes);
	stub(bikeRepository.getDateOfFirstRecord()).toReturn(_startDate);

	// Act
	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig currentYear = controller.getCurrentYear();
	final HighchartsNgConfig history = controller.getHistory(Optional.empty(), Optional.empty());
	final HighchartsNgConfig monthlyAverage = controller.getMonthlyAverage();

	// Assert
	assertThat(currentYear.getSeries().size(), is(equalTo(2)));
	List<Series> hlp = new ArrayList<>(currentYear.getSeries());
	assertThat(hlp.get(0).getName(), is(equalTo("bike1")));
	assertThat(hlp.get(0).getData(), is(equalTo(Arrays.asList(0, 0, 10, 10, 10, 10, 10, 10, 10, 15, 5, 0))));
	assertThat(hlp.get(1).getName(), is(equalTo("Sum")));
	assertThat(hlp.get(1).getData(), is(equalTo(Arrays.asList(0, 0, 10, 10, 10, 10, 10, 10, 10, 15, 5, 0))));
	AccumulatedPeriod accumulatedPeriod = (AccumulatedPeriod) ((Map<String, Object>)currentYear.getUserData()).get("worstPeriod");
	assertThat(accumulatedPeriod.getValue(), is(equalTo(0)));
	accumulatedPeriod = (AccumulatedPeriod) ((Map<String, Object>)currentYear.getUserData()).get("bestPeriod");
	assertThat(accumulatedPeriod.getValue(), is(equalTo(15)));

	hlp = new ArrayList<>(history.getSeries());
	assertThat(hlp.size(), is(equalTo(2)));
	Series series = hlp.get(0);
	assertThat(series.getName(), is(equalTo(Integer.toString(startDate.getYear()))));
	assertThat(series.getData(), is(equalTo(Arrays.asList(10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35))));
	series = hlp.get(1);
	assertThat(series.getName(), is(equalTo(Integer.toString(startDate.getYear()+1))));
	assertThat(series.getData(), is(equalTo(Arrays.asList(50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10))));
	assertThat(((Map.Entry<Integer, Integer>)((Map<String, Object>)history.getUserData()).get("worstYear")).getKey(), is(equalTo(startDate.getYear())));
	assertThat(((Map.Entry<Integer, Integer>)((Map<String, Object>)history.getUserData()).get("bestYear")).getKey(), is(equalTo(startDate.getYear() + 1)));
	final Map<Integer, BikeEntity> preferredBikes = (Map<Integer, BikeEntity>) ((Map<String, Object>)history.getUserData()).get("preferredBikes");
	assertThat(preferredBikes.get(startDate.getYear()).getName(), is(equalTo("bike1")));
	assertThat(preferredBikes.get(startDate.getYear()+1).getName(), is(equalTo("bike2")));

	hlp = new ArrayList<>(monthlyAverage.getSeries());
	assertThat(hlp.size(), is(equalTo(2)));
	assertThat(hlp.get(0).getData(), is(equalTo(Arrays.asList(
	    ( 0 + 10 +  50)/3.0,
	    ( 0 + 40 +  70)/3.0,
	    (10 + 20 + 110)/3.0,
	    (10 + 30 + 110)/3.0,
	    (10 + 30 + 110)/3.0,
	    (10 + 30 + 110)/3.0,
	    (10 + 30 + 110)/3.0,
	    (10 + 30 + 110)/3.0,
	    (10 + 65 + 110)/3.0,
	    (15 + 10 + 610)/3.0,
	    ( 5 + 10 +  10)/3.0,
	    ( 0 + 35 +  10)/2.0
	))));
	final List<Integer[]> ranges = new ArrayList(hlp.get(1).getData());
	assertThat(Arrays.equals(ranges.get(0),  new Integer[]{  0,  50}), is(true));
	assertThat(Arrays.equals(ranges.get(1),  new Integer[]{  0,  70}), is(true));
	assertThat(Arrays.equals(ranges.get(2),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(3),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(4),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(5),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(6),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(7),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(8),  new Integer[]{ 10, 110}), is(true));
	assertThat(Arrays.equals(ranges.get(9),  new Integer[]{ 10, 610}), is(true));
	assertThat(Arrays.equals(ranges.get(10), new Integer[]{  5,  10}), is(true));
	assertThat(Arrays.equals(ranges.get(11), new Integer[]{ 10,  35}), is(true));
    }

    @Test
    public void testCompleteDataFiltered() {
	// Arange
	final LocalDate startDate = january1st.minusYears(2);
	final Calendar _startDate = GregorianCalendar.from(startDate.atStartOfDay(ZoneId.systemDefault()));

	final Map<String, Integer[]> testData = new TreeMap<>();
	testData.put("bike1", new Integer[]{
	     10,  20,  30,  40,  50,  60,  70,  80,  90, 100, 110, 120,
	    150, 200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300,
	    310, 310, 310, 320, 330, 340, 350, 360, 370, 380, 395, 400
	 //
	});
	testData.put("bike2", new Integer[]{
	    0,   0,  30,  40,  50,  60,  70,  80,  90,  135,  135,  135,
	  140, 140, 200, 300, 400, 500, 600, 700, 800,  900, 1500, 1500
	});
	testData.put("bike3", new Integer[]{
	    null, null, null, 40, 50, 60, 70, 80, 90, 100, null, null
	});

	final List<BikeEntity> bikes = testData.entrySet().stream().map(entry -> {
	    final BikeEntity bike = new BikeEntity(entry.getKey(), LocalDate.now());
	    final Integer[] amounts = entry.getValue();
	    for (int i = 0; i < amounts.length; ++i) {
		if (amounts[i] == null) {
		    continue;
		}
		bike.addMilage(startDate.plusMonths(i), amounts[i]);
	    }
	    return bike;
	}).collect(toList());
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findActive(GregorianCalendar.from(january1st.atStartOfDay(ZoneId.systemDefault())))).toReturn(Arrays.asList(bikes.get(0)));
	stub(bikeRepository.findAll()).toReturn(bikes);
	stub(bikeRepository.getDateOfFirstRecord()).toReturn(_startDate);

	// Act
	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig history1 = controller.getHistory(Optional.empty(), Optional.of(startDate.getYear() + 1));
	final HighchartsNgConfig history2 = controller.getHistory(Optional.of(startDate.getYear() + 1), Optional.empty());
	final HighchartsNgConfig history3 = controller.getHistory(Optional.of(startDate.getYear() - 4), Optional.of(startDate.getYear() - 2));

	List<Series> hlp = new ArrayList<>(history1.getSeries());
	assertThat(hlp.size(), is(equalTo(1)));
	Series series = hlp.get(0);
	assertThat(series.getName(), is(equalTo(Integer.toString(startDate.getYear()))));
	assertThat(series.getData(), is(equalTo(Arrays.asList(10, 40, 20, 30, 30, 30, 30, 30, 65, 10, 10, 35))));
	assertThat(((Map<String, Object>)history1.getUserData()).get("worstYear"), is(nullValue()));
	assertThat(((Map<String, Object>)history1.getUserData()).get("bestYear"), is(nullValue()));
	Map<Integer, BikeEntity> preferredBikes = (Map<Integer, BikeEntity>) ((Map<String, Object>)history1.getUserData()).get("preferredBikes");
	assertThat(preferredBikes.get(startDate.getYear()).getName(), is(equalTo("bike1")));

	hlp = new ArrayList<>(history2.getSeries());
	assertThat(hlp.size(), is(equalTo(1)));
	series = hlp.get(0);
	assertThat(series.getName(), is(equalTo(Integer.toString(startDate.getYear()+1))));
	assertThat(series.getData(), is(equalTo(Arrays.asList(50, 70, 110, 110, 110, 110, 110, 110, 110, 610, 10, 10))));
	assertThat(((Map<String, Object>)history2.getUserData()).get("worstYear"), is(nullValue()));
	assertThat(((Map<String, Object>)history2.getUserData()).get("bestYear"), is(nullValue()));
	preferredBikes = (Map<Integer, BikeEntity>) ((Map<String, Object>)history2.getUserData()).get("preferredBikes");
	assertThat(preferredBikes.get(startDate.getYear()+1).getName(), is(equalTo("bike2")));

	hlp = new ArrayList<>(history3.getSeries());
	assertThat(hlp.size(), is(equalTo(0)));
	assertThat(((Map<String, Object>)history3.getUserData()).get("worstYear"), is(nullValue()));
	assertThat(((Map<String, Object>)history3.getUserData()).get("bestYear"), is(nullValue()));
	preferredBikes = (Map<Integer, BikeEntity>) ((Map<String, Object>)history3.getUserData()).get("preferredBikes");
	assertThat(preferredBikes, is(nullValue()));
    }

    @Test
    public void testGetHistoryNoData() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(new ArrayList<>());

	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig highchartDefinition = controller.getHistory(Optional.empty(), Optional.empty());

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.size(), is(equalTo(0)));
    }

    @Test
    public void testGetHistoryNoMilages() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(Arrays.asList(new BikeEntity("bike1", LocalDate.now()), new BikeEntity("bike2", LocalDate.now())));

	final ChartsController controller = new ChartsController(bikeRepository, "000000");
	final HighchartsNgConfig highchartDefinition = controller.getHistory(Optional.empty(), Optional.empty());

	final List<Series> hlp = new ArrayList<>(highchartDefinition.getSeries());
	assertThat(hlp.size(), is(equalTo(0)));
    }

    @Test
    public void testGetMonthlyAverage() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(defaultTestData);

	final ChartsController controller = new ChartsController(bikeRepository, "000000");

	controller.getMonthlyAverage();
    }

    @Test
    public void testGetMonthlyAverageNoData() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(new ArrayList<>());

	final ChartsController controller = new ChartsController(bikeRepository, "000000");

	final HighchartsNgConfig monthlyAverage = controller.getMonthlyAverage();
	final List<Series> hlp = new ArrayList<>(monthlyAverage.getSeries());
	assertThat(hlp.size(), is(equalTo(2)));
	final Series average = hlp.get(0);
	assertThat(average.getData(), is(equalTo(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))));
	final Series range = hlp.get(1);
	range.getData().forEach(item -> {
	    assertThat(Arrays.equals((Object[]) item, new Integer[]{0,0}), is(true));
	});
    }

    @Test
    public void testGetMonthlyAverageNoMilages() {
	final BikeRepository bikeRepository = mock(BikeRepository.class);
	stub(bikeRepository.findAll()).toReturn(Arrays.asList(new BikeEntity("bike1", LocalDate.now()), new BikeEntity("bike2", LocalDate.now())));

	final ChartsController controller = new ChartsController(bikeRepository, "000000");

	final HighchartsNgConfig monthlyAverage = controller.getMonthlyAverage();
	final List<Series> hlp = new ArrayList<>(monthlyAverage.getSeries());
	assertThat(hlp.size(), is(equalTo(2)));
	final Series average = hlp.get(0);
	assertThat(average.getData(), is(equalTo(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))));
	final Series range = hlp.get(1);
	range.getData().forEach(item -> {
	    assertThat(Arrays.equals((Object[]) item, new Integer[]{0,0}), is(true));
	});
    }
}
